package nl.vpro.amara_poms;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import nl.vpro.amara.domain.TaskType;
import nl.vpro.amara.domain.Subtitles;
import nl.vpro.amara.domain.Task;
import nl.vpro.amara.domain.User;
import nl.vpro.amara.domain.Video;
import nl.vpro.amara_poms.database.Manager;
import nl.vpro.amara_poms.database.task.DatabaseTask;
import nl.vpro.amara_poms.poms.PomsClip;
import nl.vpro.util.TimeUtils;

/**
 * @author joost
 */
@Slf4j
public class PomsPublisher {
    private Manager dbManager = Config.getDbManager();

    public void processAmaraTasks() {
        Instant after = Instant.now()
            .minus(TimeUtils.parseDuration(Config.getRequiredConfig("amara.task.fetchlastperiod"))
                .orElseThrow(IllegalArgumentException::new));
        log.info("Searching for Amara tasks after {}", after);
        Config.getAmaraClient().teams().getTasks(TaskType.Approve, after)
            .forEachRemaining(this::process);
    }
    
    protected void process(Task amaraTask) {
        try {
            log.info("Start processing video_id {}  for language {} (assigned to {})", amaraTask.getVideo_id(), amaraTask.getLanguage(), amaraTask.getAssignee().getUsername());

            // only approved tasks
            if (amaraTask.getApproved() == null || !amaraTask.getApproved().equals(Task.TASK_APPROVED)) {
                log.info("Task (" + amaraTask.getResource_uri() + ") not approved yet -> skip");
                return;
            }

            // skip tasks without language
            if (amaraTask.getLanguage() == null) {
                log.info("Task (" + amaraTask.getResource_uri() + ") has no language set -> skip");
                return;
            }

            final User user = amaraTask.getAssignee();


            // only target languages or primary language
            List<String> targetLanguages = Arrays.asList(Config.getRequiredConfigAsArray("amara.task.target.languages"));
            if (!targetLanguages.contains(amaraTask.getLanguage()) &&
                !amaraTask.getLanguage().equals(Config.getRequiredConfig("amara.api.primary_audio_language_code"))) {
                log.info("Task (" + amaraTask.getResource_uri() + ") has not target language and is not primary language " + amaraTask.getLanguage() + " -> skip");
                return;
            }

            // fetch subtitles from Amara
            Subtitles amaraSubtitles = Config.getAmaraClient().videos().getSubtitles(amaraTask.getVideo_id(), amaraTask.getLanguage(), Config.getRequiredConfig("amara.subtitles.format"));

            if (amaraSubtitles == null) {
                log.error("Subtitle for language " + amaraTask.getLanguage() + " and video_id " + amaraTask.getVideo_id() + " not found -> skip");
                return;
            }
            log.info("Fetched subtitle " + StringUtils.abbreviate(amaraSubtitles.getSubtitles().replaceAll("(\\r|\\n)", ""), 80));

            // check video_id/language/version in local db
            DatabaseTask task = dbManager.findTask(amaraTask.getVideo_id(), amaraTask.getLanguage());

            // task not found -> error
            if (task == null) {
                log.error("Task for videoId " + amaraTask.getVideo_id() + " and language " + amaraTask.getLanguage() + " not found in local db");

                // but continue anyhow (not fatal) -> create task already
                task = new DatabaseTask(amaraTask.getVideo_id(), amaraTask.getLanguage(), DatabaseTask.STATUS_NEW_AMARA_SUBTITLES_FOUND);
                dbManager.addOrUpdateTask(task);
            }

            // find pomsId in local db or from video metadata in Amara
            final Video amaraVideo = Config.getAmaraClient().videos().get(amaraTask.getVideo_id());
            String pomsMid = amaraVideo.getMetadata().getLocation();

            if (pomsMid != null) {
                log.info("Poms mid found in video meta data {}", pomsMid);
            } else {
                log.info("No poms mid found in video meta data {}", amaraVideo.getMetadata());
                // try local db
                DatabaseTask originTask = dbManager.findTask(amaraTask.getVideo_id(), Config.getRequiredConfig("amara.api.primary_audio_language_code"));
                if (originTask != null) {
                    pomsMid = originTask.getPomsSourceMid();
                }
                if (pomsMid == null || pomsMid.equals("")) {
                    log.info("No original Poms broadcast found in local db for video_id " + amaraTask.getVideo_id());
                    pomsMid = amaraVideo.getPomsMidFromVideoUrl();
                    if (pomsMid == null) {
                        log.error(("Also no Poms id found in video url(" + amaraVideo.getVideoUrlFromAllUrls() + ") for video id" + amaraTask.getVideo_id() + " -> skip record"));
                        return;
                    } else {
                        log.info("Poms mid " + pomsMid + " found in video url " + amaraVideo.getVideoUrlFromAllUrls());
                    }
                } else {
                    log.info("Poms mid found in local db:" + pomsMid);
                }
            }

            // compare version of Amara and local db
            if (task.getSubtitlesVersionNo() == null || task.isNewer(amaraSubtitles.getVersion_no())) {
                log.info("New subtitle version detected:" + amaraSubtitles.getVersion_no());

                String pomsTargetId = task.getPomsTargetId();
                if (pomsTargetId == null || pomsTargetId.equals("")) {
                    // no poms target id, so create new Poms Clip
                    try {
                        pomsTargetId = PomsClip.create(Config.getPomsClient(), pomsMid, amaraTask.getLanguage(), amaraSubtitles.getTitle(), amaraSubtitles.getDescription());
                    } catch (Exception exception) {
                        log.error("Error creating clip for poms mid " + pomsMid + ", language " + amaraTask.getLanguage());
                        log.error(exception.toString());
                        return;
                    }
                    task.setPomsTargetId(pomsTargetId);
                    task.setStatus(DatabaseTask.STATUS_UPLOADED_TO_POMS);
                    dbManager.addOrUpdateTask(task);
                    log.info("Poms clip created with poms id " + pomsTargetId);
                } else {
                    // Poms clip already exists, do nothing
                    log.info("Poms clip already exists -> do nothing " + task.toString());
                }

                // write subtitles to file
                try {
                    File file = getSubtitleFile(pomsTargetId, amaraSubtitles);
                    try (PrintWriter out = new PrintWriter(file)) {
                        log.info("Writing subtitles to {}", file);
                        out.println(amaraSubtitles.getSubtitles());
                    }
                    // update version no in local db
                    task.setSubtitlesVersionNo(amaraSubtitles.getVersion_no());
                    task.setStatus(DatabaseTask.STATUS_NEW_AMARA_SUBTITLES_WRITTEN);
                    dbManager.addOrUpdateTask(task);
                } catch (FileNotFoundException e) {
                    log.error(e.getMessage(), e);
                }
            } else {
                log.info("Subtitle version " + amaraSubtitles.getVersion_no() + " already exists in Poms for video_id " + amaraSubtitles.getVideo());
            }
            log.info("Finished processing video_id " + amaraTask.getVideo_id() + " for language " + amaraTask.getLanguage());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
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
