package nl.vpro.amara_poms;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

import org.apache.commons.lang3.StringUtils;

import nl.vpro.amara.domain.Subtitles;
import nl.vpro.amara.domain.*;
import nl.vpro.amara_poms.database.Manager;
import nl.vpro.amara_poms.database.task.DatabaseTask;
import nl.vpro.amara_poms.poms.PomsClip;
import nl.vpro.api.client.media.MediaRestClient;
import nl.vpro.domain.media.AvailableSubtitles;
import nl.vpro.domain.media.Program;
import nl.vpro.domain.media.update.MediaUpdate;
import nl.vpro.domain.subtitles.*;
import nl.vpro.util.TimeUtils;

/**
 * @author joost
 */
@Slf4j
public class PomsPublisher {
    private static final Manager dbManager = Config.getDbManager();
    private static final MediaRestClient backend = Config.getPomsClient();
    private final List<String> targetLanguages = Arrays.asList(Config.getRequiredConfigAsArray("amara.task.target.languages"));


    public void processAmaraTasks()  {
        Instant after = Instant.now()
            .minus(TimeUtils.parseDuration(Config.getRequiredConfig("amara.task.fetchlastperiod"))
                .orElseThrow(IllegalArgumentException::new));
        log.info("Searching for Amara tasks after {}", after);
        Iterator<Task> amaraTasks = Config.getAmaraClient().teams().getTasks(TaskType.Approve, after);
        while(amaraTasks.hasNext()) {
            Task task = amaraTasks.next();
            try {
                process(task);
            } catch (Exception e) {
                log.error("Failure for {}: {}. Continuing with the next one", task, e.getClass() + ":" + e.getMessage(), e);
            }
        }
    }

    private void process(Task amaraTask) throws IOException, InterruptedException {
        //find existing task or create one
        DatabaseTask databaseTask = identifyTaskinDatabase(amaraTask);

        if (! isValid(amaraTask)) {
            log.debug("amara task {} is invalid, can't be processed", amaraTask);
            return;
        }
        Subtitles subtitles = getSubtitles(amaraTask);
        if (subtitles == null) {
            log.info("No subtitles found for {}. Skipping", amaraTask);
            return;
        }
        //valid amaraTask? Then start processing
        log.info("Start processing video_id {}  for language {} (assigned to {})", amaraTask.getVideo_id(), amaraTask.getLanguage(), amaraTask.getAssignee().getUsername());
        //add subtitles to pomsSourceMid
        Optional<String> sourceMid = getPomsSourceMid(amaraTask);
        sourceMid.ifPresent(s -> {
            addSubtitlesToPomsWithChecks(s, subtitles);
            if (StringUtils.isBlank(databaseTask.getPomsSourceMid())) {
                databaseTask.setPomsSourceMid(s);
                log.info("Corrected poms source mid of database task {}", databaseTask);
                dbManager.addOrUpdateTask(databaseTask);
            }
            }
        );


        //if subtitles are new and pomsclip does not yet exist, then create new clip and add subtitles
        String pomsTargetId = createPomsTargetIdIfNeeded(databaseTask, amaraTask, getSubtitles(amaraTask));

        if (databaseTask.getSubtitlesVersionNo() == null || databaseTask.isNewer(getSubtitles(amaraTask).getVersion_no()) || pomsTargetId != null) {
            log.info("New subtitle version detected: {}", getSubtitles(amaraTask).getVersion_no());

            if (pomsTargetId != null) {
                // This means that it was just created.

                databaseTask.setPomsTargetId(pomsTargetId);
                databaseTask.setStatus(DatabaseTask.STATUS_UPLOADED_TO_POMS);
                databaseTask.setSubtitlesVersionNo(getSubtitles(amaraTask).getVersion_no());
                databaseTask.setStatus(DatabaseTask.STATUS_NEW_AMARA_SUBTITLES_WRITTEN);
                dbManager.addOrUpdateTask(databaseTask);

                //write subtitles to file
                File file = getSubtitleFile(pomsTargetId, getSubtitles(amaraTask));
                try (OutputStream out = new FileOutputStream(file)) {
                    log.info("Writing subtitles to {}", file);
                    out.write(getSubtitles(amaraTask).getSubtitles().getBytes(StandardCharsets.UTF_8));
                } catch (FileNotFoundException e) {
                    log.error(e.getMessage(), e);
                }
            }

            // update version no in local db
            databaseTask.setSubtitlesVersionNo(getSubtitles(amaraTask).getVersion_no());
            databaseTask.setStatus(DatabaseTask.STATUS_NEW_AMARA_SUBTITLES_WRITTEN);

            //remove task if completed
            dbManager.addOrUpdateTask(databaseTask);

        }
    }

    protected void addSubtitlesToPomsWithChecks(String sourceMid, Subtitles subtitles) {
        if ("nl".equals(subtitles.getLanguage().toLocale().getLanguage())) {
            Program program =  backend.getFullProgram(sourceMid);
            if (program == null) {
                log.error("No program found with mid {}", sourceMid);
                return;
            }
            Optional<AvailableSubtitles> nlCaption = program.getAvailableSubtitles()
                .stream()
                .filter(av -> "nl".equals(av.getLanguage().getLanguage()) && av.getType() == SubtitlesType.CAPTION)
                .findFirst();
            if (nlCaption.isPresent()) {
                log.debug("Not adding dutch subtitles since dutchs captions are available");
            } else {
                nl.vpro.domain.subtitles.Subtitles pomsSubtitles = addSubtitlesToPoms(sourceMid, subtitles);
                log.info("Added subtitles {} to poms source mid {}", pomsSubtitles.getId(), sourceMid);
            }
        } else {
            nl.vpro.domain.subtitles.Subtitles pomsSubtitles = addSubtitlesToPoms(sourceMid, subtitles);
            log.info("Added subtitles {} to poms source mid {}", pomsSubtitles.getId(), sourceMid);
        }
    }

    protected DatabaseTask identifyTaskinDatabase (Task amaraTask) {
        DatabaseTask task = dbManager.findTask(amaraTask.getVideo_id(), amaraTask.getLanguage());
        if (task == null) {
            log.error("Task for videoId " + amaraTask.getVideo_id() + " and language " + amaraTask.getLanguage() + " not found in local db");

            // but continue anyhow (not fatal) -> create task already
            task = new DatabaseTask(amaraTask.getVideo_id(), amaraTask.getLanguage(), DatabaseTask.STATUS_NEW_AMARA_SUBTITLES_FOUND);
            dbManager.addOrUpdateTask(task);
            return task;
        }
        return task;
    }

    protected boolean isValid(Task amaraTask) {
        if (amaraTask.getApproved() == null || !amaraTask.getApproved().equals(Task.TASK_APPROVED) || !hasValidLanguage(amaraTask)) {
            log.info("Task (" + amaraTask.getResource_uri() + ") has not been approved or has no language set  -> skip");
            return false;
        }
        return true;
    }

    protected boolean hasValidLanguage(Task amaraTask) {
        if (amaraTask.getLanguage() == null) {
            log.info("Task (" + amaraTask.getResource_uri() + ") has no language set -> skip");
            return false;
        }

        if (!targetLanguages.contains(amaraTask.getLanguage()) &&
            !amaraTask.getLanguage().equals(Config.getRequiredConfig("amara.api.primary_audio_language_code"))) {
            log.info("Task (" + amaraTask.getResource_uri() + ") has not target language and is not primary language " + amaraTask.getLanguage() + " -> skip");
            return false;
        }
        return true;
    }

    protected Subtitles getSubtitles(Task amaraTask) {
        Subtitles subtitles = Config.getAmaraClient().videos().getSubtitles(amaraTask.getVideo_id(), amaraTask.getLanguage(), Config.getRequiredConfig("amara.subtitles.format"));

        if (subtitles == null) {
            log.error("Subtitle for language " + amaraTask.getLanguage() + " and video_id " + amaraTask.getVideo_id() + " not found -> skip");
            return null;
        } else {
            log.info("Fetched subtitle " + StringUtils.abbreviate(subtitles.getSubtitles().replaceAll("([\\r\\n])", ""), 80));
            return subtitles;
        }
    }

    protected Optional<String> getPomsSourceMid(Task amaraTask) {
        final Video amaraVideo = Config.getAmaraClient().videos().get(amaraTask.getVideo_id());
        if (amaraVideo.getMetadata().getLocation() != null) {
            String location = amaraVideo.getMetadata().getLocation();
            log.info("poms source mid (location: {}) found in video metadata. For video id: {}", location, amaraTask.getVideo_id());
            return Optional.of(location);
        } else if (amaraVideo.getPomsMidFromVideoUrl() != null) {
            String pomsMidFromVideoUrl = amaraVideo.getPomsMidFromVideoUrl();
            log.info("poms source mid: {} found in video url {}. For video id:  {}", pomsMidFromVideoUrl, amaraVideo.getVideo_url(), amaraTask.getVideo_id());
            return Optional.of(pomsMidFromVideoUrl);
        } else if (identifyTaskinDatabase(amaraTask).getPomsSourceMid() != null) {
            String pomsSourceMid = identifyTaskinDatabase(amaraTask).getPomsSourceMid();
            log.info("poms source mid {} found in database. For video id: {}", pomsSourceMid, amaraTask.getVideo_id());
            return Optional.of(pomsSourceMid);
        } else {
            log.info("no poms source mid found");
            return Optional.empty();

        }
    }

    protected String createPomsTargetIdIfNeeded(DatabaseTask databaseTask, Task amaraTask, Subtitles amaraSubtitles) throws InterruptedException, IOException {
        if (StringUtils.isNotEmpty(databaseTask.getPomsTargetId())) {
            // already has a target mid. So nothing needs to be done.
            return null;
        } else {
            if (StringUtils.isBlank(databaseTask.getPomsSourceMid())) {
                throw new IllegalStateException("Source mid of " + databaseTask + " is empty");
            }
            return createClipAndAddSubtitles(databaseTask.getPomsSourceMid(), amaraTask, amaraSubtitles, databaseTask);
        }
    }

    protected String createClipAndAddSubtitles(String pomsMid, Task amaraTask, Subtitles amaraSubtitles, DatabaseTask task) throws InterruptedException, IOException {
        // no poms target id, so create new Poms Clip
        String pomsClipId;
        try {
            pomsClipId = PomsClip.create(Config.getPomsClient(), pomsMid, amaraTask.getLanguage(), amaraSubtitles.getTitle(), amaraSubtitles.getDescription(), getCridForTask(amaraTask));
            if (StringUtils.isBlank(pomsClipId)){
                throw new IllegalStateException("Didn't create proper poms clip");
            }
            log.info("Poms clip created with poms id {} (translation for {})", pomsClipId, pomsMid);
        } catch (Exception exception) {
            log.error("Error creating clip for poms mid {}, language {}",
                pomsMid, amaraTask.getLanguage(), exception);
            return null;
        }
        Instant start = Instant.now();
        MediaUpdate<?> found = null;
        while (Duration.between(start, Instant.now()).compareTo(Duration.ofSeconds(30)) < 0) {
            found = backend.get(pomsClipId);
            Thread.sleep(50000);
            if (found != null) {
                break;
            }

        }
        if (found == null) {
            log.warn("Didn't find just created object with id {} (translation for {})", pomsClipId, pomsMid);
        }
        task.setPomsTargetId(pomsClipId);
        task.setStatus(DatabaseTask.STATUS_UPLOADED_TO_POMS);
        dbManager.addOrUpdateTask(task);
        log.info("Poms clip created with poms id {}", pomsClipId);
        addSubtitlesToPoms(pomsClipId, amaraSubtitles);
        log.info("Translation in language '{}' added to POMS clip {} and POMS mid {}", amaraTask.getLanguage(), pomsClipId, pomsMid);
        return pomsClipId;
    }

    protected nl.vpro.domain.subtitles.Subtitles addSubtitlesToPoms(String mid, Subtitles subs) {
        nl.vpro.domain.subtitles.Subtitles subtitles = amaraToPomsSubtitles(subs, mid);
        log.info("Creating subtitles '{}' in {}", subtitles.getId(), backend);
        backend.setSubtitles(subtitles);
        return subtitles;

    }

    protected nl.vpro.domain.subtitles.Subtitles amaraToPomsSubtitles(Subtitles subtitles, String mid) {

        nl.vpro.domain.subtitles.Subtitles pomsSubtitles = new nl.vpro.domain.subtitles.Subtitles();
        pomsSubtitles.setType(SubtitlesType.TRANSLATION);
        pomsSubtitles.setMid(mid);
        if (subtitles.getLanguage().toLocale() != null) {
            pomsSubtitles.setLanguage(subtitles.getLanguage().toLocale());
        }
        if (subtitles.getSubtitles() != null) {
            pomsSubtitles.setContent(new SubtitlesContent(SubtitlesFormat.WEBVTT, subtitles.getSubtitles()));
        }

        // There is a bug in poms 5.5: it can't accept subtitles without cue numbers.
        //nl.vpro.domain.subtitles.Subtitles corrected = nl.vpro.domain.subtitles.Subtitles.from(pomsSubtitles.getId(), SubtitlesUtil.fillCueNumber(SubtitlesUtil.parse(pomsSubtitles, false)).iterator());

        return pomsSubtitles;

    }


    private File getSubtitleFile(String filename, Subtitles subtitles) {
        String basePath = Config.getRequiredConfig("subtitle.basepath");
        File dir = new File(basePath, subtitles.getLanguage().getCode());
        if (dir.mkdirs()) {
            log.info("Made {}", dir);
        }
        return new File(dir, filename);
    }


    private String getCridForTask(Task task) {
        return "crid://amara.translation/"  + task.getVideo_id() + "/" + task.getLanguage();
    }

}
