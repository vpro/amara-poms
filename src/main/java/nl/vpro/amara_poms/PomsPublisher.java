package nl.vpro.amara_poms;

import nl.vpro.amara_poms.amara.activity.AmaraActivity;
import nl.vpro.amara_poms.amara.activity.AmaraActivityCollection;
import nl.vpro.amara_poms.amara.subtitles.AmaraSubtitles;
import nl.vpro.amara_poms.amara.task.AmaraTask;
import nl.vpro.amara_poms.amara.task.AmaraTaskCollection;
import nl.vpro.amara_poms.amara.video.AmaraVideo;
import nl.vpro.amara_poms.database.Manager;
import nl.vpro.amara_poms.database.task.Task;
import nl.vpro.amara_poms.poms.PomsClip;
import nl.vpro.amara_poms.poms.Utils;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by joost on 04/04/16.
 */
public class PomsPublisher {

    final Logger logger = LoggerFactory.getLogger(AmaraPublisher.class);

    public void processAmaraTasks() {

        // init db
        Manager dbManager = Manager.getInstance();
        dbManager.setFilenameTasks(Config.getRequiredConfig("db.filepath"));
        dbManager.readFile();

        // get tasks for some period
        long afterTimestampInSeconds = Config.getRequiredConfigAsLong("amara.task.fetchlastperiod.seconds");
        long now = System.currentTimeMillis() / 1000;
        List<AmaraTask> amaraTasks = AmaraTaskCollection.getListForType(Config.getRequiredConfig("amara.task.type.out"),
                                                                        now - afterTimestampInSeconds);

        for (AmaraTask amaraTask : amaraTasks) {
            logger.info("Start processing video_id " + amaraTask.video_id + " for language " + amaraTask.language);

            logger.info("Approved:" + amaraTask.getApproved());
            logger.info("Type:" + amaraTask.type);

            // only approved tasks
            if (amaraTask.getApproved() == null || !amaraTask.getApproved().equals(AmaraTask.TASK_APPROVED)) {
                logger.info("Task (" + amaraTask.resource_uri + ") not approved yet -> skip");
                continue;
            }

            // skip nl tasks
            if (amaraTask.language == null || amaraTask.language.equals(Config.getRequiredConfig("amara.api.primary_audio_language_code"))) {
                logger.info("Task (" + amaraTask.resource_uri + ") is base language " +
                            Config.getRequiredConfig("amara.api.primary_audio_language_code") + " -> skip");
                continue;
            }

            // fetch subtitles from Amara
            AmaraSubtitles amaraSubtitles = AmaraSubtitles.get(amaraTask.video_id, amaraTask.language);

            if (amaraSubtitles == null) {
                logger.error("Subtitle for language " + amaraTask.language + " and video_id " + amaraTask.video_id + " not found -> skip");
                continue;
            }
            logger.info("Fetched subtitle " + StringUtils.abbreviate(amaraSubtitles.subtitles.replaceAll("(\\r|\\n)", ""), 80));

            // check video_id/language/version in local db
            Task task = dbManager.findTask(amaraTask.video_id, amaraTask.language);

            // task not found -> error
            if (task == null) {
                logger.error("Task for videoId " + amaraTask.video_id + " and language " + amaraTask.language + " not found in local db");

                // but continue anyhow (not fatal) -> create task already
                task = new Task(amaraTask.video_id, amaraTask.language, Task.STATUS_NEW_AMARA_SUBTITLES_FOUND);
                dbManager.addOrUpdateTask(task);
            }

            // find pomsId in local db or from video metadata in Amara
            String pomsMid = null;
            AmaraVideo amaraVideo = AmaraVideo.get(amaraTask.video_id);
            pomsMid = amaraVideo.getMetadata().location;
            if (pomsMid != null) {
                logger.info("Poms mid found in video meta data:" + pomsMid);
            } else {
                logger.info("No poms mid found in video meta data");
                // try local db
                Task originTask = dbManager.findTask(amaraTask.video_id, Config.getRequiredConfig("amara.api.primary_audio_language_code"));
                if (originTask != null) {
                    pomsMid = originTask.getPomsSourceMid();
                }
                if (pomsMid == null) {
                    logger.error("No original Poms broadcast found in local db for video_id " + amaraTask.video_id + " so no Poms id found -> skip");
                    continue;
                } else {
                    logger.info("Poms mid found in local db:" + pomsMid);
                }
            }

            // compare version of Amara and local db
            if (task.getSubtitlesVersionNo() == null || task.isNewer(amaraSubtitles.version_no)) {
                logger.info("New subtitle version detected:" + amaraSubtitles.version_no);

                // first write subtitles to file
                if (amaraSubtitles.writeSubtitlesToFiles(pomsMid) != Config.NO_ERROR) {
                    continue;
                }

                // update version no in local db
                task = new Task(amaraTask.video_id, amaraTask.language, Task.STATUS_NEW_AMARA_SUBTITLES_WRITTEN);
                task.setSubtitlesVersionNo(amaraSubtitles.version_no);
                dbManager.addOrUpdateTask(task);

                if (task.getPomsTargetId() == null) {
                    // no poms target id, so create new Poms Clip

                    String newPomsTargetId = PomsClip.create(Utils.getClient(), pomsMid, amaraTask.language);
                    task.setPomsTargetId(newPomsTargetId);
                    task.setStatus(Task.STATUS_UPLOADED_TO_POMS);
                    dbManager.addOrUpdateTask(task);
                } else {
                    // Poms clip already exists, do nothing
                    logger.info("Poms clip already exists -> do nothing " + task.toString());
                }
            } else {
                logger.info("Subtitle version " + amaraSubtitles.version_no + " already exists in Poms for video_id " + amaraSubtitles.video);
            }
            logger.info("Finished processing video_id " + amaraTask.video_id + " for language " + amaraTask.language);
        }
    }

}
