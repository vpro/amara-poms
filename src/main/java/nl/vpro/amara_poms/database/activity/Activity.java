package nl.vpro.amara_poms.database.activity;

import org.apache.commons.csv.CSVRecord;

import java.time.ZonedDateTime;
import java.util.ArrayList;

/**
 * Created by joost on 09/04/16.
 */
public class Activity {

    public static final String STATUS_INIT = "0";
    
    String id;
    ZonedDateTime createDateTime;
    ZonedDateTime updateDateTime;
    String status;
    
    public Activity() {
    }

    public Activity(String id) {
        this.id = id;
    }

    public static String getStatusInit() {
        return STATUS_INIT;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ArrayList get() {
        ArrayList list = new ArrayList();
        list.add(id);
        list.add(status);
        list.add(createDateTime);
        list.add(updateDateTime);

        return  list;
    }


    static public Activity Factory(CSVRecord csvRecord) {
        Activity activity = new Activity();

        activity.id = csvRecord.get("id");
        
        activity.status = csvRecord.get("status");
        activity.createDateTime = ZonedDateTime.parse(csvRecord.get("createDateTime"));
        activity.updateDateTime = ZonedDateTime.parse(csvRecord.get("updateDateTime"));

        return activity;
    }

    // CSV file header
    private static final Object[] FILE_HEADER = {"id","status","createDateTime","updateDateTime"};

    // for writing
    public static Object[] getFileHeader() {
        return FILE_HEADER;
    }

    // CSV file header for reading
    private static final String[] FILE_HEADER_MAPPING = {"id","status","createDateTime","updateDateTime"};

    public static String[] getFileHeaderForReading() {
        return  FILE_HEADER_MAPPING;
    }


}
