package nl.vpro.amara_poms.amara;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import nl.vpro.amara_poms.Config;
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

    private String video_id;
    private String language;
    private String type;
    private String assignee;

    public AmaraTask() {}

    public AmaraTask(String video_id, String language, String type, String assignee) {
        this.video_id = video_id;
        this.language = language;
        this.type = type;
        this.assignee = assignee;
    }

    public String getVideo_id() {
        return video_id;
    }
    public void setVideo_id(String video_id) {
        this.video_id = video_id;
    }

    public String getLanguage() {
        return language;
    }
    public void setLanguage(String language) {
        this.language = language;
    }

    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    public String getAssignee() {
        return assignee;
    }
    public void setAssignee(String assignee) {
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
