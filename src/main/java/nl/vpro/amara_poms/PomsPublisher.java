package nl.vpro.amara_poms;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import nl.vpro.amara.domain.Subtitles;
import nl.vpro.amara.domain.Task;
import nl.vpro.amara.domain.TaskType;
import nl.vpro.amara.domain.Video;
import nl.vpro.amara_poms.database.Manager;
import nl.vpro.amara_poms.database.task.DatabaseTask;
import nl.vpro.amara_poms.poms.PomsClip;
import nl.vpro.domain.subtitles.SubtitlesContent;
import nl.vpro.domain.subtitles.SubtitlesFormat;
import nl.vpro.domain.subtitles.SubtitlesType;
import nl.vpro.rs.media.MediaRestClient;
import nl.vpro.util.TimeUtils;

/**
 * @author joost
 */
@Slf4j
public class PomsPublisher {
    private static final Manager dbManager = Config.getDbManager();
    private static final MediaRestClient backend = Config.getPomsClient();
    private final List<String> targetLanguages = Arrays.asList(Config.getRequiredConfigAsArray("amara.task.target.languages"));



    public void processAmaraTasks() throws IOException {
        Instant after = Instant.now()
            .minus(TimeUtils.parseDuration(Config.getRequiredConfig("amara.task.fetchlastperiod"))
                .orElseThrow(IllegalArgumentException::new));
        log.info("Searching for Amara tasks after {}", after);
        List<Task> amaraTasks = (List)(Config.getAmaraClient().teams().getTasks(TaskType.Approve, after));
        for (Task task: amaraTasks) {
            process(task);
        }
    }

    public void process(Task amaraTask) throws IOException {
        //find existing task or create one
        DatabaseTask task = identifyTaskinDatabase(amaraTask);
        //valid amaraTask? Then start processing
        if (isValid(amaraTask) && getSubtitles(amaraTask) != null) {
            log.info("Start processing video_id {}  for language {} (assigned to {})", amaraTask.getVideo_id(), amaraTask.getLanguage(), amaraTask.getAssignee().getUsername());
            //add subtitles to pomsSourceMid
            if (getPomsSourceMid(amaraTask) != null) {
                addSubtitlesToPoms(getPomsSourceMid(amaraTask), getSubtitles(amaraTask));
            }

            //if subtitles are new and pomsclip does not yet exist, then create new clip and add subtitles

            if (task.getSubtitlesVersionNo() == null || task.isNewer(getSubtitles(amaraTask).getVersion_no())) {
                log.info("New subtitle version detected:" + getSubtitles(amaraTask).getVersion_no());
                String pomsTargetId = identifyPomsTargetId(task, amaraTask, getSubtitles(amaraTask));

                if (pomsTargetId != null){
                    task.setPomsTargetId(pomsTargetId);
                    task.setStatus(DatabaseTask.STATUS_UPLOADED_TO_POMS);
                    task.setSubtitlesVersionNo(getSubtitles(amaraTask).getVersion_no());
                    task.setStatus(DatabaseTask.STATUS_NEW_AMARA_SUBTITLES_WRITTEN);
                    dbManager.addOrUpdateTask(task);
                    log.info("Poms clip created with poms id " + pomsTargetId);
                    addSubtitlesToPoms(pomsTargetId, getSubtitles(amaraTask));
                }

                //write subtitles to file
                File file = getSubtitleFile(pomsTargetId, getSubtitles(amaraTask));
                try (PrintWriter out = new PrintWriter(file)) {
                    log.info("Writing subtitles to {}", file);
                    out.println(getSubtitles(amaraTask).getSubtitles());
                }
            }

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

    public String getPomsSourceMid(Task amaraTask) {
        final Video amaraVideo = Config.getAmaraClient().videos().get(amaraTask.getVideo_id());
        if (amaraVideo.getMetadata().getLocation() != null && isMid(amaraVideo.getMetadata().getLocation())) {
            return amaraVideo.getMetadata().getLocation();
        } else if (amaraVideo.getPomsMidFromVideoUrl() != null && isMid(amaraVideo.getPomsMidFromVideoUrl())) {
            return amaraVideo.getPomsMidFromVideoUrl();
        } else if (identifyTaskinDatabase(amaraTask).getPomsSourceMid() != null && isMid(identifyTaskinDatabase(amaraTask).getPomsSourceMid())) {
            return identifyTaskinDatabase(amaraTask).getPomsSourceMid();
        } else {
            log.info("no poms source mid found");
            return null;

        }
    }


    public String identifyPomsTargetId(DatabaseTask task, Task amaraTask, Subtitles amaraSubtitles) {
        if (isMid(task.getPomsTargetId())) {
            return null;
        } else {
            return PomsClip.create(backend, getPomsSourceMid(amaraTask), amaraTask.getLanguage(), amaraSubtitles.getTitle(), amaraSubtitles.getDescription());
        }
    }


    public boolean isMid(String mid) {
        return mid != "" && mid != null;
    }


    public void addSubtitlesToPoms(String mid, Subtitles subs) throws IOException {
        backend.setSubtitles(amaraToPomsSubtitles(subs, mid));

    }


    protected nl.vpro.domain.subtitles.Subtitles amaraToPomsSubtitles(Subtitles subtitles, String mid) throws IOException {
        nl.vpro.domain.subtitles.Subtitles pomsSubtitles = new nl.vpro.domain.subtitles.Subtitles();
        pomsSubtitles.setType(SubtitlesType.TRANSLATION);
        if (isMid(mid)) {
            pomsSubtitles.setMid(mid);
        }
        if (subtitles.getLanguage().toLocale() != null) {
            pomsSubtitles.setLanguage(subtitles.getLanguage().toLocale());
        }
        if (subtitles.getSubtitles() != null) {
            pomsSubtitles.setContent(new SubtitlesContent(SubtitlesFormat.WEBVTT, subtitles.getSubtitles()));
        }
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

}