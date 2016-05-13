package nl.vpro.amara_poms;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.vpro.amara.domain.Subtitles;
import nl.vpro.amara.domain.Task;
import nl.vpro.amara.domain.Video;
import nl.vpro.amara_poms.database.Manager;
import nl.vpro.amara_poms.poms.PomsClip;

/**
 * @author joost
 */
public class PomsPublisher {

    private final static Logger LOG = LoggerFactory.getLogger(PomsPublisher.class);

    public void processAmaraTasks() {

        // init db
        Manager dbManager = Manager.getInstance();
        dbManager.setFilenameTasks(Config.getRequiredConfig("db.filepath"));
        dbManager.readFile();

        // get tasks for some period
        long afterTimestampInSeconds = Config.getRequiredConfigAsLong("amara.task.fetchlastperiod.seconds");
        long now = System.currentTimeMillis() / 1000;
        List<Task> amaraTasks = Config.getAmaraClient().getTasks(Config.getRequiredConfig("amara.task.type.out"),
                                                                        now - afterTimestampInSeconds).getTasks();
        LOG.info("Search for Amara tasks...");
        for (Task amaraTask : amaraTasks) {
            LOG.info("Start processing video_id " + amaraTask.video_id + " for language " + amaraTask.language);

            // only approved tasks
            if (amaraTask.getApproved() == null || !amaraTask.getApproved().equals(Task.TASK_APPROVED)) {
                LOG.info("Task (" + amaraTask.resource_uri + ") not approved yet -> skip");
                continue;
            }

            // skip tasks without language
            if (amaraTask.language == null) {
                LOG.info("Task (" + amaraTask.resource_uri + ") has no language set -> skip");
                continue;
            }

            // only target languages or primary language
            List<String> targetLanguages = Arrays.asList(Config.getRequiredConfigAsArray("amara.task.target.languages"));
            if (!targetLanguages.contains(amaraTask.language) &&
                !amaraTask.language.equals(Config.getRequiredConfig("amara.api.primary_audio_language_code"))) {
                LOG.info("Task (" + amaraTask.resource_uri + ") has not target language and is not primary language " +
                        amaraTask.language + " -> skip");
                continue;
            }

            // fetch subtitles from Amara
            Subtitles amaraSubtitles = Config.getAmaraClient().getSubtitles(amaraTask.video_id, amaraTask.language, Config.getRequiredConfig("amara.subtitles.format"));

            if (amaraSubtitles == null) {
                LOG.error("Subtitle for language " + amaraTask.language + " and video_id " + amaraTask.video_id + " not found -> skip");
                continue;
            }
            LOG.info("Fetched subtitle " + StringUtils.abbreviate(amaraSubtitles.subtitles.replaceAll("(\\r|\\n)", ""), 80));

            // check video_id/language/version in local db
            nl.vpro.amara_poms.database.task.Task task = dbManager.findTask(amaraTask.video_id, amaraTask.language);

            // task not found -> error
            if (task == null) {
                LOG.error("Task for videoId " + amaraTask.video_id + " and language " + amaraTask.language + " not found in local db");

                // but continue anyhow (not fatal) -> create task already
                task = new nl.vpro.amara_poms.database.task.Task(amaraTask.video_id, amaraTask.language, nl.vpro.amara_poms.database.task.Task.STATUS_NEW_AMARA_SUBTITLES_FOUND);
                dbManager.addOrUpdateTask(task);
            }

            // find pomsId in local db or from video metadata in Amara
            String pomsMid;
            Video amaraVideo = Config.getAmaraClient().getVideo(amaraTask.video_id);
            pomsMid = amaraVideo.getMetadata().getLocation();
            if (pomsMid != null) {
                LOG.info("Poms mid found in video meta data:" + pomsMid);
            } else {
                LOG.info("No poms mid found in video meta data {}", amaraVideo.getMetadata());
                // try local db
                nl.vpro.amara_poms.database.task.Task originTask = dbManager.findTask(amaraTask.video_id, Config.getRequiredConfig("amara.api.primary_audio_language_code"));
                if (originTask != null) {
                    pomsMid = originTask.getPomsSourceMid();
                }
                if (pomsMid == null || pomsMid.equals("")) {
                    LOG.info("No original Poms broadcast found in local db for video_id " + amaraTask.video_id);
                    pomsMid = amaraVideo.getPomsMidFromVideoUrl();
                    if (pomsMid == null) {
                        LOG.error(("Also no Poms id found in video url(" + amaraVideo.getVideoUrlFromAllUrls() + ") for video id" + amaraTask.video_id + " -> skip record"));
                        continue;
                    } else {
                        LOG.info("Poms mid " + pomsMid + " found in video url "+ amaraVideo.getVideoUrlFromAllUrls());
                    }
                } else {
                    LOG.info("Poms mid found in local db:" + pomsMid);
                }
            }

            // compare version of Amara and local db
            if (task.getSubtitlesVersionNo() == null || task.isNewer(amaraSubtitles.version_no)) {
                LOG.info("New subtitle version detected:" + amaraSubtitles.version_no);

                String pomsTargetId = task.getPomsTargetId();
                if (pomsTargetId == null || pomsTargetId.equals("")) {
                    // no poms target id, so create new Poms Clip
                    try {
                        pomsTargetId = PomsClip.create(Config.getPomsClient(), pomsMid, amaraTask.language, amaraSubtitles.title, amaraSubtitles.description);
                    } catch (Exception exception) {
                        LOG.error("Error creating clip for poms mid " + pomsMid + ", language " + amaraTask.language);
                        LOG.error(exception.toString());
                        continue;
                    }
                    task.setPomsTargetId(pomsTargetId);
                    task.setStatus(nl.vpro.amara_poms.database.task.Task.STATUS_UPLOADED_TO_POMS);
                    dbManager.addOrUpdateTask(task);
                    LOG.info("Poms clip created with poms id " + pomsTargetId);
                } else {
                    // Poms clip already exists, do nothing
                    LOG.info("Poms clip already exists -> do nothing " + task.toString());
                }

                // write subtitles to file
                try {
                    writeSubtitlesToFiles(pomsTargetId, amaraSubtitles);
                    // update version no in local db
                    task.setSubtitlesVersionNo(amaraSubtitles.version_no);
                    task.setStatus(nl.vpro.amara_poms.database.task.Task.STATUS_NEW_AMARA_SUBTITLES_WRITTEN);
                    dbManager.addOrUpdateTask(task);
                } catch (FileNotFoundException e) {
                    LOG.error(e.getMessage(), e);
                }
            } else {
                LOG.info("Subtitle version " + amaraSubtitles.version_no + " already exists in Poms for video_id " + amaraSubtitles.video);
            }
            LOG.info("Finished processing video_id " + amaraTask.video_id + " for language " + amaraTask.language);
        }
    }

    public String getSubtitleFilepath(String filename, Subtitles subtitles) {
        String basePath = Config.getRequiredConfig("subtitle.basepath");

        basePath += subtitles.language.getCode() + "/" + filename;

        return basePath;
    }

    public void writeSubtitlesToFiles(String pomsMid,  Subtitles subtitles) throws FileNotFoundException {
        try (PrintWriter out = new PrintWriter(getSubtitleFilepath(pomsMid, subtitles))) {
            out.println(subtitles);
        }
    }

}
