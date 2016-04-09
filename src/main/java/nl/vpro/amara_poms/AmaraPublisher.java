package nl.vpro.amara_poms;

import nl.vpro.amara_poms.amara.subtitles.AmaraSubtitles;
import nl.vpro.amara_poms.amara.task.AmaraTask;
import nl.vpro.amara_poms.amara.video.AmaraVideo;
import nl.vpro.amara_poms.database.Manager;
import nl.vpro.amara_poms.database.task.Task;
import nl.vpro.amara_poms.poms.PomsBroadcast;
import nl.vpro.amara_poms.poms.PomsCollection;
import nl.vpro.domain.media.update.MemberUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

/**
 * Download broadcasts from Poms and send them to Amara
 *
 * Created by joost on 04/04/16.
 */
public class AmaraPublisher {

    final Logger logger = LoggerFactory.getLogger(AmaraPublisher.class);

    /**
     * Get collection 'Net in Nederland - te vertalen
     * and loop through all broadcasts
     */
    public void processPomsCollection()
    {
        // init db
        Manager dbManager = Manager.getInstance();
        dbManager.setFilenameTasks(Config.getRequiredConfig("db.filepath"));
        dbManager.readFile();

        // Get collection from POMS
        String inputCollectionName = Config.getRequiredConfig("poms.input.collections_mid");
        String outputCollectionName = Config.getRequiredConfig("poms.output.collections_mid");
        PomsCollection collectionToBeTranslated = new PomsCollection(inputCollectionName);
        collectionToBeTranslated.getBroadcastsFromPOMS();

        // Iterate over collection
        Iterator<MemberUpdate> pomsBroadcastIterator = collectionToBeTranslated.getBroadcastsIterator();
        while (pomsBroadcastIterator.hasNext()) {

            PomsBroadcast pomsBroadcast = new PomsBroadcast(pomsBroadcastIterator.next());

            logger.info("Start processing broadcast with Mid:" + pomsBroadcast.getMediaUpdate().getMid());

            //
            // Download source to download.omroep.nl
            //
            if (pomsBroadcast.downloadFileToDownloadServer() != Config.NO_ERROR) {
                continue;
            }

            //
            // Download ondertitels (probeer 2 locaties)
            //
            if (pomsBroadcast.downloadSubtitles() != Config.NO_ERROR) {
                continue;
            }

            //
            // Publish to Amara
            //
            AmaraVideo amaraVideo = new AmaraVideo(pomsBroadcast.getExternalUrl(),
                                                   Config.getRequiredConfig("amara.api.primary_audio_language_code"),
                                                   pomsBroadcast.getTitle(),
                                                   pomsBroadcast.getDescription(),
                                                   Config.getRequiredConfig("amara.api.team"));

            AmaraVideo uploadedAmaraVideo = AmaraVideo.post(amaraVideo);
            if (uploadedAmaraVideo == null) {
                continue;
            } else {
                logger.info("Video uploaded to Amara with id " + uploadedAmaraVideo.getId());
                dbManager.addOrUpdateTask(new Task(uploadedAmaraVideo.getId(),
                                                   Config.getRequiredConfig("amara.api.primary_audio_language_code"),
                                                   Task.STATUS_UPLOADED_VIDEO_TO_AMARA));
                dbManager.writeFile();
            }

            //
            // Upload subtitles to Amara
            //
            AmaraSubtitles amaraSubtitles = new AmaraSubtitles(Config.getRequiredConfig("amara.subtitles.title.default"),
                                                               Config.getRequiredConfig("amara.subtitles.format"),
                                                               pomsBroadcast.getSubtitles(),
                                                               Config.getRequiredConfig("amara.subtitles.description.default"),
                                                               Config.getRequiredConfig("amara.subtitles.action.default"));
            AmaraSubtitles uploadedAmaraSubtitles = AmaraSubtitles.post(amaraSubtitles, uploadedAmaraVideo.getId(),
                                                                        Config.getRequiredConfig("amara.api.primary_audio_language_code"));

            if (uploadedAmaraSubtitles == null) {
                continue;
            } else {
                dbManager.addOrUpdateTask(new Task(uploadedAmaraVideo.getId(),
                        Config.getRequiredConfig("amara.api.primary_audio_language_code"),
                        Task.STATUS_UPLOADED_SUBTITLE_TO_AMARA));
                dbManager.writeFile();
                logger.info("Subtitle uploaded to Amara with id " + uploadedAmaraVideo.getId());
            }

            //
            // Create tasks
            //
            String[] targetLanguages = Config.getRequiredConfigAsArray("amara.task.target.languages");
            for (String targetLanguage : targetLanguages) {
                AmaraTask amaraTask = new AmaraTask(uploadedAmaraVideo.getId(), targetLanguage,
                        Config.getRequiredConfig("amara.task.type.default"),
                        Config.getRequiredConfig("amara.task.user.default"));
                AmaraTask uploadedAmaraTask = AmaraTask.post(amaraTask);

                if (uploadedAmaraTask != null) {
                    logger.info("Task created for language " + targetLanguage + " to Amara with video id " + uploadedAmaraVideo.getId());
                    dbManager.addOrUpdateTask(new Task(uploadedAmaraVideo.getId(),
                            targetLanguage,
                            Task.STATUS_CREATE_AMARA_TASK_FOR_TRANSLATION));
                    dbManager.writeFile();
                }
            }

            //
            // verwijder uit POMS collectie 'Net in Nederland' en plaats in POMS collectie 'Net in Nederland'
            //
//            pomsBroadcast.moveFromCollectionToCollection(inputCollectionName, outputCollectionName);
        }

        dbManager.writeFile();
    }

}
