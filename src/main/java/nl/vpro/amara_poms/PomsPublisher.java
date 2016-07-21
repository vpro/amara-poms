package nl.vpro.amara_poms;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.time.Instant;
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

    private Manager dbManager = Config.getDbManager();


    public void processAmaraTasks() {
        Instant after = Instant.now().minusSeconds(Config.getRequiredConfigAsLong("amara.task.fetchlastperiod.seconds"));
        LOG.info("Searching for Amara tasks after {}", after);
        List<Task> amaraTasks = Config.getAmaraClient().teams().getTasks(Config.getRequiredConfig("amara.task.type.out"), after).getTasks();
        for (Task amaraTask : amaraTasks) {
            process(amaraTask);

        }
    }
    protected void process(Task amaraTask) {
        LOG.info("Start processing video_id " + amaraTask.getVideo_id() + " for language " + amaraTask.getLanguage());

        // only approved tasks
        if (amaraTask.getApproved() == null || !amaraTask.getApproved().equals(Task.TASK_APPROVED)) {
            LOG.info("Task (" + amaraTask.getResource_uri() + ") not approved yet -> skip");
            return;
        }

        // skip tasks without language
        if (amaraTask.getLanguage() == null) {
            LOG.info("Task (" + amaraTask.getResource_uri() + ") has no language set -> skip");
            return;
        }

        // only target languages or primary language
        List<String> targetLanguages = Arrays.asList(Config.getRequiredConfigAsArray("amara.task.target.languages"));
        if (!targetLanguages.contains(amaraTask.getLanguage()) &&
            !amaraTask.getLanguage().equals(Config.getRequiredConfig("amara.api.primary_audio_language_code"))) {
            LOG.info("Task (" + amaraTask.getResource_uri() + ") has not target language and is not primary language " + amaraTask.getLanguage() + " -> skip");
            return;
        }

        // fetch subtitles from Amara
        Subtitles amaraSubtitles = Config.getAmaraClient().videos().getSubtitles(amaraTask.getVideo_id(), amaraTask.getLanguage(), Config.getRequiredConfig("amara.subtitles.format"));

        if (amaraSubtitles == null) {
            LOG.error("Subtitle for language " + amaraTask.getLanguage() + " and video_id " + amaraTask.getVideo_id() + " not found -> skip");
            return;
        }
        LOG.info("Fetched subtitle " + StringUtils.abbreviate(amaraSubtitles.getSubtitles().replaceAll("(\\r|\\n)", ""), 80));

        // check video_id/language/version in local db
        nl.vpro.amara_poms.database.task.Task task = dbManager.findTask(amaraTask.getVideo_id(), amaraTask.getLanguage());

        // task not found -> error
        if (task == null) {
            LOG.error("Task for videoId " + amaraTask.getVideo_id() + " and language " + amaraTask.getLanguage() + " not found in local db");

            // but continue anyhow (not fatal) -> create task already
            task = new nl.vpro.amara_poms.database.task.Task(amaraTask.getVideo_id(), amaraTask.getLanguage(), nl.vpro.amara_poms.database.task.Task.STATUS_NEW_AMARA_SUBTITLES_FOUND);
            dbManager.addOrUpdateTask(task);
        }

        // find pomsId in local db or from video metadata in Amara
        final Video amaraVideo = Config.getAmaraClient().videos().get(amaraTask.getVideo_id());
        String pomsMid = amaraVideo.getMetadata().getLocation();
        if (pomsMid != null) {
            LOG.info("Poms mid found in video meta data:" + pomsMid);
        } else {
            LOG.info("No poms mid found in video meta data {}", amaraVideo.getMetadata());
            // try local db
            nl.vpro.amara_poms.database.task.Task originTask = dbManager.findTask(amaraTask.getVideo_id(), Config.getRequiredConfig("amara.api.primary_audio_language_code"));
            if (originTask != null) {
                pomsMid = originTask.getPomsSourceMid();
            }
            if (pomsMid == null || pomsMid.equals("")) {
                LOG.info("No original Poms broadcast found in local db for video_id " + amaraTask.getVideo_id());
                pomsMid = amaraVideo.getPomsMidFromVideoUrl();
                if (pomsMid == null) {
                    LOG.error(("Also no Poms id found in video url(" + amaraVideo.getVideoUrlFromAllUrls() + ") for video id" + amaraTask.getVideo_id() + " -> skip record"));
                    return;
                } else {
                    LOG.info("Poms mid " + pomsMid + " found in video url " + amaraVideo.getVideoUrlFromAllUrls());
                }
            } else {
                LOG.info("Poms mid found in local db:" + pomsMid);
            }
        }

        // compare version of Amara and local db
        if (task.getSubtitlesVersionNo() == null || task.isNewer(amaraSubtitles.getVersion_no())) {
            LOG.info("New subtitle version detected:" + amaraSubtitles.getVersion_no());

            String pomsTargetId = task.getPomsTargetId();
            if (pomsTargetId == null || pomsTargetId.equals("")) {
                // no poms target id, so create new Poms Clip
                try {
                    pomsTargetId = PomsClip.create(Config.getPomsClient(), pomsMid, amaraTask.getLanguage(), amaraSubtitles.getTitle(), amaraSubtitles.getDescription());
                } catch (Exception exception) {
                    LOG.error("Error creating clip for poms mid " + pomsMid + ", language " + amaraTask.getLanguage());
                    LOG.error(exception.toString());
                    return;
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
                File file = getSubtitleFile(pomsTargetId, amaraSubtitles);
                try (PrintWriter out = new PrintWriter(file)) {
                    LOG.info("Writing subtitles to {}", file);
                    out.println(amaraSubtitles.getSubtitles());
                }
                // update version no in local db
                task.setSubtitlesVersionNo(amaraSubtitles.getVersion_no());
                task.setStatus(nl.vpro.amara_poms.database.task.Task.STATUS_NEW_AMARA_SUBTITLES_WRITTEN);
                dbManager.addOrUpdateTask(task);
            } catch (FileNotFoundException e) {
                LOG.error(e.getMessage(), e);
            }
        } else {
            LOG.info("Subtitle version " + amaraSubtitles.getVersion_no() + " already exists in Poms for video_id " + amaraSubtitles.getVideo());
        }
        LOG.info("Finished processing video_id " + amaraTask.getVideo_id() + " for language " + amaraTask.getLanguage());
    }

    private File getSubtitleFile(String filename, Subtitles subtitles) {
        String basePath = Config.getRequiredConfig("subtitle.basepath");
        File dir = new File(basePath, subtitles.getLanguage().getCode());
        if (dir.mkdirs()) {
            LOG.info("Made {}", dir);
        }
        return new File(dir, filename);
    }

}
