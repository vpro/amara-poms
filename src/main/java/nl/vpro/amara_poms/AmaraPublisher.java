package nl.vpro.amara_poms;

import lombok.extern.slf4j.Slf4j;

import java.net.URI;

import nl.vpro.amara.domain.*;
import nl.vpro.amara_poms.database.Manager;
import nl.vpro.amara_poms.database.task.DatabaseTask;
import nl.vpro.amara_poms.poms.PomsBroadcast;
import nl.vpro.amara_poms.poms.PomsCollection;
import nl.vpro.amara_poms.poms.SourceFetcher;
import nl.vpro.domain.media.update.MemberUpdate;

/**
 * Download broadcasts from Poms and send them to Amara
 *
 * @author joost
 */
@Slf4j
public class AmaraPublisher {

    private Manager dbManager = Config.getDbManager();

    private final SourceFetcher fetcher;

    public AmaraPublisher(SourceFetcher fetcher) {
        this.fetcher = fetcher;
    }

    /**
     * Get collection 'Net in Nederland - te vertalen
     * and loop through all broadcasts
     */
    public void processPomsCollection() {
        String inputCollectionName = Config.getRequiredConfig("poms.input.collections_mid");

        log.info("Search for POMS broadcasts to be translated, using mid {} on {}", inputCollectionName, Config.getPomsClient());

        PomsCollection collectionToBeTranslated = new PomsCollection(inputCollectionName);
        for (MemberUpdate update : collectionToBeTranslated) {
            String mid = update.getMediaUpdate().getMid();
            try {
                PomsBroadcast pomsBroadcast = new PomsBroadcast(mid, Config.getPomsClient().getFull(mid));
                if (handle(pomsBroadcast)) {
                    // verwijder uit POMS collectie 'Net in Nederland te vertalen'
                    pomsBroadcast.removeFromCollection(inputCollectionName);
                }
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }

        dbManager.writeFile();
    }

    protected boolean handle(PomsBroadcast pomsBroadcast) {
        String mid = pomsBroadcast.getMid();
        log.info("Start processing broadcast {}: {}", mid, pomsBroadcast.getTitle());

        // check if broadcast has already been sent to Amara
        DatabaseTask task = dbManager.findTaskByPomsSourceId(mid);
        if (task != null) {
            // task exists, so at least video is uploaded
            log.info("Poms broadcast with poms mid " + mid + " already sent to Amara -> skip");
            return false;
        }
        //
        SourceFetcher.FetchResult result = fetcher.fetch(pomsBroadcast.getProgram());
        if (result.status != SourceFetcher.Status.SUCCESS) {
            log.error("Downloading subtitles to server failed :{}", result);
            return false;
        }

        Video amaraVideo = constructVideo(pomsBroadcast, result.destination);
        Video uploadedAmaraVideo = Config.getAmaraClient().videos().post(amaraVideo);
        if (uploadedAmaraVideo == null) {
            log.info("No amara video uploaded for {}", amaraVideo);
            return false;
        } else {
            log.info("Video {} for {} uploaded to Amara with id {}", mid, result.destination, uploadedAmaraVideo.getId());
            dbManager.addOrUpdateTask(new DatabaseTask(uploadedAmaraVideo.getId(),
                Config.getRequiredConfig("amara.api.primary_audio_language_code"),
                DatabaseTask.STATUS_UPLOADED_VIDEO_TO_AMARA,
                mid));
        }

        uploadSubtitles(uploadedAmaraVideo, pomsBroadcast);
        createTasks(uploadedAmaraVideo, mid);
        return true;
    }

    protected Video constructVideo(PomsBroadcast pomsBroadcast, URI uri) {
        String pomsMidBroadcast = pomsBroadcast.getMid();
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

        String thumbnailUrl = pomsBroadcast.getThumbNailUrl();

        VideoMetadata amaraVideoMetadata = new VideoMetadata(speakerName, pomsMidBroadcast);
        Video amaraVideo = new Video(uri.toString(),
            Config.getRequiredConfig("amara.api.primary_audio_language_code"),
            videoTitel,
            pomsBroadcast.getDescription(),
            Config.getRequiredConfig("amara.api.team"),
            amaraVideoMetadata);
        amaraVideo.setThumbnail(thumbnailUrl);
        amaraVideo.setProject(Config.getRequiredConfig("amara.api.video.default.project"));
        return amaraVideo;
    }

    protected void uploadSubtitles(Video uploadedAmaraVideo, PomsBroadcast pomsBroadcast) {
        if (pomsBroadcast.downloadSubtitles() != Config.NO_ERROR) {
            log.warn("Could not download subtitles");
            return;
        }
        String pomsMidBroadcast = pomsBroadcast.getMid();
        log.info("Uploading subtitles to amara for " + pomsMidBroadcast);
        Subtitles amaraSubtitles = new Subtitles(
            uploadedAmaraVideo.getTitle(),
            Config.getRequiredConfig("amara.subtitles.format"),
            pomsBroadcast.getSubtitles(),
            pomsBroadcast.getDescription(),
            Config.getRequiredConfig("amara.subtitles.action.default"));

        Subtitles uploadedAmaraSubtitles = Config.getAmaraClient().videos().post(
            amaraSubtitles,
            uploadedAmaraVideo.getId(),
            Config.getRequiredConfig("amara.api.primary_audio_language_code")
        );

        if (uploadedAmaraSubtitles != null) {
            dbManager.addOrUpdateTask(
                new DatabaseTask(
                    uploadedAmaraVideo.getId(),
                    Config.getRequiredConfig("amara.api.primary_audio_language_code"),
                    DatabaseTask.STATUS_UPLOADED_SUBTITLE_TO_AMARA,
                    pomsMidBroadcast)
            );
            log.info("Subtitle uploaded to Amara with id " + uploadedAmaraVideo.getId());

            // nl subtitles status is now complete, has to be aproved (can only be done in 2 steps)
            SubtitleAction amaraSubtitleAction = new SubtitleAction(SubtitleAction.ACTION_APPROVE);

            SubtitleAction amaraSubtitleActionOut = Config.getAmaraClient().videos().post(amaraSubtitleAction,
                uploadedAmaraVideo.getId(),
                Config.getRequiredConfig("amara.api.primary_audio_language_code"));

            if (amaraSubtitleActionOut != null) {
                dbManager.addOrUpdateTask(
                    new DatabaseTask(
                        uploadedAmaraVideo.getId(),
                        Config.getRequiredConfig("amara.api.primary_audio_language_code"),
                    DatabaseTask.STATUS_APPROVED_SUBTITLE_IN_AMARA, pomsMidBroadcast));
                log.info("Subtitle nl approved in Amara with id " + uploadedAmaraVideo.getId());
            }
        } else {
            log.info("Could not post subtitles to amara");
        }
    }


    protected void createTasks(Video uploadedAmaraVideo, String pomsMidBroadcast) {
        String[] targetLanguages = Config.getRequiredConfigAsArray("amara.task.target.languages");
        for (String targetLanguage : targetLanguages) {
            Task amaraTask = new Task(uploadedAmaraVideo.getId(), targetLanguage,
                Config.getRequiredConfig("amara.task.type.in"),
                User.builder().username(Config.getRequiredConfig("amara.task.user.default")).build());

            log.info("Creating amara task for {}: {}", pomsMidBroadcast, amaraTask);
            Task uploadedAmaraTask = Config.getAmaraClient().teams().post(amaraTask);

            if (uploadedAmaraTask != null) {
                log.info("Task (" + uploadedAmaraTask.getResource_uri() + ") created for language " + targetLanguage + " to Amara with video id " + uploadedAmaraVideo.getId());
                dbManager.addOrUpdateTask(new DatabaseTask(uploadedAmaraVideo.getId(),
                    targetLanguage,
                    DatabaseTask.STATUS_CREATE_AMARA_TASK_FOR_TRANSLATION, pomsMidBroadcast));
            } else {
                log.error("No uploaded amara task received for {}", amaraTask);
            }
        }

    }

}
