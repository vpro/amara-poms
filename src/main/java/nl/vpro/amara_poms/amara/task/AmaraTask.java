package nl.vpro.amara_poms.amara.task;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import nl.vpro.amara_poms.Config;
import nl.vpro.amara_poms.amara.Utils;

import org.joda.time.DateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

import static com.sun.xml.bind.v2.util.XmlFactory.logger;

/**
 * Created by joost on 06/04/16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AmaraTask {


    final static Logger logger = LoggerFactory.getLogger(AmaraTask.class);

    public final static String TYPE_TRANSLATE = "Translate";

    public final static String TASK_APPROVED = "Approve";

    public String video_id;
    public String language;
    public String type;
    public String assignee;

    public int priority;
    public DateTime completed;
    public String approved;
    public String resource_uri;

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

    public AmaraTask() {}

    public AmaraTask(String video_id, String language, String type, String assignee) {
        this.video_id = video_id;
        this.language = language;
        this.type = type;
        this.assignee = assignee;
    }

    private static URI getPostUri() {
        String team = Config.getRequiredConfig("amara.api.team");
        String url = Config.getRequiredConfig("amara.api.url") + "api/teams/" + team + "/tasks/";

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);

        return (builder.build().encode().toUri());
    }


    public static AmaraTask post(AmaraTask amaraTaskIn) {

        AmaraTask amaraTaskOut = null;

        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpEntity<AmaraTask> request = new HttpEntity<>(amaraTaskIn, Utils.getPostHeaders());
            ResponseEntity<AmaraTask> response = restTemplate.exchange(getPostUri(),
                    HttpMethod.POST, request, AmaraTask.class);

            if (response.getStatusCode() == HttpStatus.CREATED) {
                amaraTaskOut = response.getBody();
            }
        } catch (HttpClientErrorException e) {
            logger.info(e.toString());
            String responseBody = new String(e.getResponseBodyAsString());
            logger.info(responseBody);
        }

        return amaraTaskOut;
    }



}
