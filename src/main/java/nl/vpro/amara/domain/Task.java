package nl.vpro.amara.domain;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author joost
 * @todo static methods should not be in domain

 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Task {


    private final static Logger LOG = LoggerFactory.getLogger(Task.class);

    public final static String TYPE_TRANSLATE = "Translate";

    public final static String TASK_APPROVED = "Approved";

    public String video_id;
    public String language;
    public String type;
    public String assignee;

    public int priority;
    public DateTime completed;
    public String resource_uri;


    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String approved;

    public String getApproved() {
        return approved;
    }
    public void setApproved(String approved) {
        this.approved = approved;
    }

    @Override
    public String toString() {
        return "AmaraTask{" +
                "video_id='" + video_id + '\'' +
                ", language='" + language + '\'' +
                ", type='" + type + '\'' +
                ", assignee='" + assignee + '\'' +
                ", priority=" + priority +
                ", completed=" + completed +
                ", approved='" + approved + '\'' +
                ", resource_uri='" + resource_uri + '\'' +
                '}';
    }

    public Task() {}

    public Task(String video_id, String language, String type, String assignee) {
        this.video_id = video_id;
        this.language = language;
        this.type = type;
        this.assignee = assignee;
    }



}
