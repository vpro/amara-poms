package nl.vpro.amara_poms;

import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.vpro.amara.domain.SubtitleAction;
import nl.vpro.amara.domain.Subtitles;
import nl.vpro.amara.domain.Task;
import nl.vpro.amara.domain.Video;
import nl.vpro.amara.domain.VideoMetadata;
import nl.vpro.amara_poms.database.Manager;
import nl.vpro.amara_poms.poms.PomsBroadcast;
import nl.vpro.amara_poms.poms.PomsCollection;
import nl.vpro.domain.media.update.MemberUpdate;

/**
 * Download broadcasts from Poms and send them to Amara
 *
 * @author joost
 */
public class AmaraPublisher {

    private static final Logger LOG = LoggerFactory.getLogger(AmaraPublisher.class);

    /**
     * Get collection 'Net in Nederland - te vertalen
     * and loop through all broadcasts
     */
    public void processPomsCollection() {
        // init db
        Manager dbManager = Manager.getInstance();
        dbManager.setFilenameTasks(Config.getRequiredConfig("db.filepath"));
        dbManager.readFile();

        // Get collection from POMS
        String inputCollectionName = Config.getRequiredConfig("poms.input.collections_mid");
        PomsCollection collectionToBeTranslated = new PomsCollection(inputCollectionName);
        collectionToBeTranslated.getBroadcastsFromPOMS();

        // Iterate over collection
        LOG.info("Search for POMS broadcasts to be translated...");
        Iterator<MemberUpdate> pomsBroadcastIterator = collectionToBeTranslated.getBroadcastsIterator();
        while (pomsBroadcastIterator.hasNext()) {

            PomsBroadcast pomsBroadcast = new PomsBroadcast(pomsBroadcastIterator.next());

            String pomsMidBroadcast = pomsBroadcast.getProgramUpdate().getMid();
            LOG.info("Start processing broadcast with Mid:" + pomsMidBroadcast);

            // check if broadcast has already been sent to Amara
            nl.vpro.amara_poms.database.task.Task task = dbManager.findTaskByPomsSourceId(pomsMidBroadcast);
            if (task != null) {
                // task exists, so at least video is uploaded
                LOG.info("Poms broadcast with poms mid " + pomsMidBroadcast + " already sent to Amara -> skip");
                continue;
            }

            //
            // Publish to Amara
            //
            if (pomsBroadcast.downloadFileToDownloadServer() != Config.NO_ERROR) {
                LOG.error("Downloading subtitles to server failed");
                continue;
            }

            // construct title, etc.
            final String videoTitel;
            final String speakerName;
            if (pomsBroadcast.getSubTitle() == null || pomsBroadcast.getSubTitle().equals("")) {
                videoTitel = pomsBroadcast.getTitle();
                speakerName = "";
            } else {
                videoTitel = pomsBroadcast.getTitle() + " // " + pomsBroadcast.getSubTitle();
                speakerName = pomsBroadcast.getTitle();
            }

            // construct image thumbnail
            String thumbnailUrl = null;
            String imageId = pomsBroadcast.getImageId();
            if (imageId != null) {
                thumbnailUrl = Config.getRequiredConfig("poms.image_url") + imageId + ".jpg";
            }

            // really send
            VideoMetadata amaraVideoMetadata = new VideoMetadata(speakerName, pomsMidBroadcast);
            Video amaraVideo = new Video(pomsBroadcast.getExternalUrl(),
                                                   Config.getRequiredConfig("amara.api.primary_audio_language_code"),
                                                   videoTitel,
                                                   pomsBroadcast.getDescription(),
                                                   Config.getRequiredConfig("amara.api.team"),
                                                   amaraVideoMetadata);
            amaraVideo.setThumbnail(thumbnailUrl);
            amaraVideo.setProject(Config.getRequiredConfig("amara.api.video.default.project"));
            Video uploadedAmaraVideo = Config.getAmaraClient().post(amaraVideo);
            if (uploadedAmaraVideo == null) {
                continue;
            } else {
                LOG.info("Video uploaded to Amara with id " + uploadedAmaraVideo.getId());
                dbManager.addOrUpdateTask(new nl.vpro.amara_poms.database.task.Task(uploadedAmaraVideo.getId(),
                                                   Config.getRequiredConfig("amara.api.primary_audio_language_code"),
                                                   nl.vpro.amara_poms.database.task.Task.STATUS_UPLOADED_VIDEO_TO_AMARA,
                                                   pomsMidBroadcast));
            }

            //
            // Download ondertitels (probeer 2 locaties)
            //
            if (pomsBroadcast.downloadSubtitles() == Config.NO_ERROR) {
                //
                // Upload subtitles to Amara
                //
                Subtitles amaraSubtitles = new Subtitles(videoTitel,
                        Config.getRequiredConfig("amara.subtitles.format"),
                        pomsBroadcast.getSubtitles(),
                        pomsBroadcast.getDescription(),
                        Config.getRequiredConfig("amara.subtitles.action.default"));

                Subtitles uploadedAmaraSubtitles = Config.getAmaraClient().post(amaraSubtitles, uploadedAmaraVideo.getId(),
                        Config.getRequiredConfig("amara.api.primary_audio_language_code"));

                if (uploadedAmaraSubtitles != null) {
                    dbManager.addOrUpdateTask(new nl.vpro.amara_poms.database.task.Task(uploadedAmaraVideo.getId(),
                            Config.getRequiredConfig("amara.api.primary_audio_language_code"),
                            nl.vpro.amara_poms.database.task.Task.STATUS_UPLOADED_SUBTITLE_TO_AMARA, pomsMidBroadcast));
                    LOG.info("Subtitle uploaded to Amara with id " + uploadedAmaraVideo.getId());

                    // nl subtitles status is now complete, has to be aproved (can only be done in 2 steps)
                    SubtitleAction amaraSubtitleAction = new SubtitleAction(SubtitleAction.ACTION_APPROVE);

                    SubtitleAction amaraSubtitleActionOut = Config.getAmaraClient().post(amaraSubtitleAction,
                            uploadedAmaraVideo.getId(),
                            Config.getRequiredConfig("amara.api.primary_audio_language_code"));

                    if (amaraSubtitleActionOut != null) {
                        dbManager.addOrUpdateTask(new nl.vpro.amara_poms.database.task.Task(uploadedAmaraVideo.getId(),
                                Config.getRequiredConfig("amara.api.primary_audio_language_code"),
                                nl.vpro.amara_poms.database.task.Task.STATUS_APPROVED_SUBTITLE_IN_AMARA, pomsMidBroadcast));
                        LOG.info("Subtitle nl approved in Amara with id " + uploadedAmaraVideo.getId());
                    }
                }
            }

            //
            // Create tasks
            //
            String[] targetLanguages = Config.getRequiredConfigAsArray("amara.task.target.languages");
            for (String targetLanguage : targetLanguages) {
                Task amaraTask = new Task(uploadedAmaraVideo.getId(), targetLanguage,
                        Config.getRequiredConfig("amara.task.type.in"),
                        Config.getRequiredConfig("amara.task.user.default"));
                Task uploadedAmaraTask = Config.getAmaraClient().post(amaraTask);

                if (uploadedAmaraTask != null) {
                    LOG.info("Task (" + uploadedAmaraTask.getResource_uri() + ") created for language " + targetLanguage + " to Amara with video id " + uploadedAmaraVideo.getId());
                    dbManager.addOrUpdateTask(new nl.vpro.amara_poms.database.task.Task(uploadedAmaraVideo.getId(),
                            targetLanguage,
                            nl.vpro.amara_poms.database.task.Task.STATUS_CREATE_AMARA_TASK_FOR_TRANSLATION, pomsMidBroadcast));
                } else {
                    LOG.error("No uploaded amara task received for {}", amaraTask);
                }
            }

            //
            // verwijder uit POMS collectie 'Net in Nederland' en plaats in POMS collectie 'Net in Nederland'
            pomsBroadcast.removeFromCollection(inputCollectionName);
        }

        dbManager.writeFile();
    }

}
