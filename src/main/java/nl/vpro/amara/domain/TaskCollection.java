package nl.vpro.amara.domain;

import java.net.URI;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import nl.vpro.amara_poms.Config;
import nl.vpro.amara.Utils;

/**
 * @author joost
 * @todo static methods should not be in domain
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TaskCollection {

    private final static Logger LOG = LoggerFactory.getLogger(TaskCollection.class);

    public Meta meta;

    @JsonProperty("objects")
    List<Task> amaraTasks;

    public static List<Task> getList() {

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<TaskCollection> request = new HttpEntity<>(Utils.getGetHeaders());
        ResponseEntity<TaskCollection> response = restTemplate.exchange(Utils.getUriForPath("api/task"), HttpMethod.GET, request, TaskCollection.class);
        TaskCollection amaryActivityCollection = response.getBody();

        HttpStatus httpStatus = response.getStatusCode();

        LOG.info(String.valueOf(response));

        return  amaryActivityCollection.amaraTasks;
    }

    public static List<Task> getListForType(String taskType, long afterTimestampInSeconds) {

        // build url
        String url = Config.getRequiredConfig("amara.api.url") + "api/teams/" + Config.getRequiredConfig("amara.api.team") + "/tasks";
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("type", taskType)
                .queryParam("limit", 300)
                .queryParam("completed")
                .queryParam("completed-after", afterTimestampInSeconds);
        URI uri = builder.build().encode().toUri();

        // do request
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<TaskCollection> request = new HttpEntity<>(Utils.getGetHeaders());
        ResponseEntity<TaskCollection> response = restTemplate.exchange(uri, HttpMethod.GET, request, TaskCollection.class);
        TaskCollection amaryActivityCollection = response.getBody();

        HttpStatus httpStatus = response.getStatusCode();
//        logger.info(String.valueOf(response));

        return  amaryActivityCollection.amaraTasks;
    }

}
