package nl.vpro.amara_poms;

import nl.vpro.amara_poms.amara.activity.AmaraActivity;
import nl.vpro.amara_poms.amara.activity.AmaraActivityCollection;
import nl.vpro.amara_poms.amara.subtitles.AmaraSubtitles;
import nl.vpro.amara_poms.amara.task.AmaraTask;
import nl.vpro.amara_poms.amara.task.AmaraTaskCollection;
import nl.vpro.amara_poms.amara.video.AmaraVideo;
import nl.vpro.amara_poms.database.Manager;
import nl.vpro.amara_poms.database.task.Task;
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
        dbManager.setFilenameTasks(Config.getRequiredConfig("db.filepath.tasks"));
        dbManager.readFile();

        // don't use for the moment
//        Iterator<Task> taskIterator = dbManager.getTaskIterator();
//        logger.info(dbManager.getTasks().size() + " tasks found");
//
//        while (taskIterator.hasNext()) {
//            Task currentTask = taskIterator.next();
//
//
//        }


//        long afterTimestampInSeconds = Config.getRequiredConfigAsLong("amara.task.fetchlastperiod.seconds");
//        long now = System.currentTimeMillis() / 1000;
//
//        List<AmaraActivity> amaraActivities = AmaraActivityCollection.getListForType(AmaraActivity.TYPE_APPROVE_VERSION, now - afterTimestampInSeconds);
//
//        for (AmaraActivity amaraActivity : amaraActivities) {
//
//            // check if activity is already done
//            if (dbManager.findActivityById(amaraActivity.getId()) != null) {
//                logger.info("Activity " + amaraActivity.getId() + " found -> skip");
//                continue;
//            }

        // get tasks for some periodop
        long afterTimestampInSeconds = Config.getRequiredConfigAsLong("amara.task.fetchlastperiod.seconds");
        long now = System.currentTimeMillis() / 1000;
        List<AmaraTask> amaraTasks = AmaraTaskCollection.getListForType(Config.getRequiredConfig("amara.task.type.default"),
                                                                        now - afterTimestampInSeconds);

        for (AmaraTask amaraTask : amaraTasks) {
            logger.info(" Start processing video_id " + amaraTask.video_id + " for language " + amaraTask.language);

            // only approved tasks
            if (amaraTask.approved == null || !amaraTask.approved.equals(AmaraTask.TASK_APPROVED)) {
                logger.info("Task not approved yet -> skip");
                continue;
            }

            // fetch subtitles
            AmaraSubtitles amaraSubtitles = AmaraSubtitles.get(amaraTask.video_id, amaraTask.language);

            if (amaraSubtitles == null) {
                logger.error("Subtitle for language " + amaraTask.language + " and video_id " + amaraTask.video_id + " not found");
                continue;
            }
            logger.info("Fetched subtitle " + StringUtils.abbreviate(amaraSubtitles.subtitles, 20));

            // check video_id/language/version in local db
            Task task = dbManager.findTask(amaraTask.video_id, amaraTask.language);

            // task not found -> error
            if (task == null) {
                logger.error("Task for videoId " + amaraTask.video_id + " and language " + amaraTask.language + " not found in local db");
                // but continue anyhow (not fatal)
                task = new Task(amaraTask.video_id, amaraTask.language, Task.STATUS_NEW_AMARA_SUBTITLES_FOUND);
            }

            if (task.getSubtitlesVersionNo() == null || !task.getSubtitlesVersionNo().equals(amaraSubtitles.version_no)) {
                // new subtitle version detected

                // first write subtitles to file
                amaraSubtitles.writeSubtitlesToFiles();
                // todo always continue or stop if error

                if (task.getPomsTargetId() == null) {
                    // no poms id, so create new Poms Clip

                    task.setStatus(Task.STATUS_UPLOADED_TO_POMS);
                } else {
                    // Poms clip already exists, so update

                    task.setStatus(Task.STATUS_POMS_UPDATED);
                }
                dbManager.writeFile();
            } else {
                logger.info("Subtitle version " + amaraSubtitles.version_no + " already exists in Poms for video_id " + amaraSubtitles.video);
            }
        }
    }

//    Deze maakt nieuwe clip aan voor de desbetreffende taal aan
//    - in een relatie wordt het verband gelegd met bron-uitzending
//    - Ondertiteling bestand wordt neergezet (voorlopig in /home/omroep/vpro_admin/files.vpro.nl/pages/
//            netinnederland/subtitles)
//    - Plaatst clip op Collectie (bijvoorbeeld "NetInNL Arabisch")
//    - Past bron aan zodat deze refereert aan de bron-uitzending: mid://omroep.nl/program/<mid>
//            - let op: ondertitels kunnen later ook nog aangepast worden. In dat geval niet naar de collectie verplaatsen

}
