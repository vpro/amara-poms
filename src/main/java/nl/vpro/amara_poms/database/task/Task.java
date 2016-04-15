package nl.vpro.amara_poms.database.task;

import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;
import java.util.ArrayList;

/**
 * Created by joost on 07/04/16.
 */
public class Task {
    final static Logger logger = LoggerFactory.getLogger(Task.class);

    public static final String STATUS_UPLOADED_VIDEO_TO_AMARA = "0";
    public static final String STATUS_UPLOADED_SUBTITLE_TO_AMARA = "1";
    public static final String STATUS_APPROVED_SUBTITLE_IN_AMARA = "2";
    public static final String STATUS_CREATE_AMARA_TASK_FOR_TRANSLATION = "3";
    public static final String STATUS_NEW_AMARA_SUBTITLES_FOUND = "4";
    public static final String STATUS_UPLOADED_TO_POMS = "5";
    public static final String STATUS_NEW_AMARA_SUBTITLES_WRITTEN = "6";

    private String videoId;
    private String language;
    private String status;
    private String pomsSourceMid;
    private String pomsTargetId;
    private String subtitlesVersionNo;
    private ZonedDateTime createDateTime;
    private ZonedDateTime updateDateTime;

    public Task() {
    }

    public Task(String videoId, String language, String status) {
        this.videoId = videoId;
        this.language = language;
        this.status = status;
    }

    public Task(String videoId, String language, String status, String pomsSourceMid) {
        this.videoId = videoId;
        this.language = language;
        this.status = status;
        this.pomsSourceMid = pomsSourceMid;
    }

    static public Task Factory(CSVRecord csvRecord) {
        Task task = new Task();

        task.videoId = csvRecord.get("videoId");
        task.language = csvRecord.get("language");
        task.status = csvRecord.get("status");
        task.pomsSourceMid = csvRecord.get("pomsSourceMid");
        task.pomsTargetId = csvRecord.get("pomsTargetMid");
        task.subtitlesVersionNo = csvRecord.get("subtitlesVersionNo");
        task.createDateTime = ZonedDateTime.parse(csvRecord.get("createDateTime"));
        task.updateDateTime = ZonedDateTime.parse(csvRecord.get("updateDateTime"));

        return task;
    }

    /**
     * Return true if input version is newer
     * @param subtitleVersionToCompareWith
     * @return
     */
    public boolean isNewer(String subtitleVersionToCompareWith) {
        boolean isNewer = false;

        if (subtitlesVersionNo == null || subtitlesVersionNo.equals("")) {
            isNewer = true;
        } else {
            try {
                int subtitleVersion = Integer.parseInt(subtitlesVersionNo);
                int subtitleVersionToCompareWithInt = Integer.parseInt(subtitleVersionToCompareWith);

                if (subtitleVersionToCompareWithInt > subtitleVersion) {
                    isNewer = true;
                }
            } catch (Exception e) {
                logger.error("Error converting subtitle version number to int (" + subtitleVersionToCompareWith + "/" + subtitlesVersionNo + ")");
            }
        }

        return isNewer;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ZonedDateTime getCreateDateTime() {
        return createDateTime;
    }

    public void setCreateDateTime(ZonedDateTime createDateTime) {
        this.createDateTime = createDateTime;
    }

    public ZonedDateTime getUpdateDateTime() {
        return updateDateTime;
    }

    public void setUpdateDateTime(ZonedDateTime updateDateTime) {
        this.updateDateTime = updateDateTime;
    }

    public String getPomsSourceMid() {
        return pomsSourceMid;
    }

    public void setPomsSourceMid(String pomsSourceMid) {
        this.pomsSourceMid = pomsSourceMid;
    }

    public String getPomsTargetId() {
        return pomsTargetId;
    }

    public void setPomsTargetId(String pomsTargetId) {
        this.pomsTargetId = pomsTargetId;
    }

    public String getSubtitlesVersionNo() {
        return subtitlesVersionNo;
    }

    public void setSubtitlesVersionNo(String subtitlesVersionNo) {
        this.subtitlesVersionNo = subtitlesVersionNo;
    }

    @Override
    public String toString() {
        return "Task{" +
                "videoId='" + videoId + '\'' +
                ", language='" + language + '\'' +
                ", status='" + status + '\'' +
                ", pomsSourceMid='" + pomsSourceMid + '\'' +
                ", pomsTargetId='" + pomsTargetId + '\'' +
                ", subtitlesVersionNo'" + subtitlesVersionNo + '\'' +
                ", createDateTime=" + createDateTime +
                ", updateDateTime=" + updateDateTime +
                '}';
    }

    public ArrayList get() {
        ArrayList list = new ArrayList();
        list.add(videoId);
        list.add(language);
        list.add(status);
        list.add(pomsSourceMid);
        list.add(pomsTargetId);
        list.add(subtitlesVersionNo);
        list.add(createDateTime);
        list.add(updateDateTime);

        return  list;
    }

    // CSV file header
    private static final Object[] FILE_HEADER = {"videoId","language","status", "pomsSourceMid", "pomsTargetMid", "subtitlesVersionNo", "createDateTime","updateDateTime"};

    // for writing
    public static Object[] getFileHeader() {
        return FILE_HEADER;
    }

    // CSV file header for reading
    private static final String[] FILE_HEADER_MAPPING = {"videoId","language","status","pomsSourceMid", "pomsTargetMid", "subtitlesVersionNo", "createDateTime","updateDateTime"};

    public static String[] getFileHeaderForReading() {
        return  FILE_HEADER_MAPPING;
    }
}
