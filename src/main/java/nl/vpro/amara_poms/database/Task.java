package nl.vpro.amara_poms.database;

import org.apache.commons.csv.CSVRecord;
import org.apache.james.mime4j.field.datetime.DateTime;

import java.time.ZonedDateTime;
import java.util.ArrayList;

/**
 * Created by joost on 07/04/16.
 */
public class Task {

    static final String STATUS_INITTIAL = "0";

    private String videoId;
    private String language;
    private String status;
    private ZonedDateTime createDateTime;
    private ZonedDateTime updateDateTime;

    public Task() {
    }

    public Task(String videoId, String language, String status) {
        this.videoId = videoId;
        this.language = language;
        this.status = status;
    }

    static public Task Factory(CSVRecord csvRecord) {
        Task task = new Task();

        task.videoId = csvRecord.get("videoId");
        task.language = csvRecord.get("language");
        task.status = csvRecord.get("status");
        String dt = csvRecord.get("createDateTime");
        task.createDateTime = ZonedDateTime.parse(csvRecord.get("createDateTime"));
        task.updateDateTime = ZonedDateTime.parse(csvRecord.get("updateDateTime"));

        return task;
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

    @Override
    public String toString() {
        return "task{" +
                "videoId='" + videoId + '\'' +
                ", language='" + language + '\'' +
                ", status='" + status + '\'' +
                ", createDateTime=" + createDateTime +
                ", updateDateTime=" + updateDateTime +
                '}';
    }

    public ArrayList get() {
        ArrayList list = new ArrayList();
        list.add(videoId);
        list.add(language);
        list.add(status);
        list.add(createDateTime);
        list.add(updateDateTime);

        return  list;
    }

    // CSV file header
    private static final Object[] FILE_HEADER = {"videoId","language","status","createDateTime","updateDateTime"};

    // for writing
    public static Object[] getFileHeader() {
        return FILE_HEADER;
    }

    // CSV file header for reading
    private static final String[] FILE_HEADER_MAPPING = {"videoId","language","status","createDateTime","updateDateTime"};

    public static String[] getFileHeaderForReading() {
        return  FILE_HEADER_MAPPING;
    }
}
