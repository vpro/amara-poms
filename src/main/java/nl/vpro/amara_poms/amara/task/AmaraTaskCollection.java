package nl.vpro.amara_poms.amara.task;

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
import nl.vpro.amara_poms.amara.AmaraMeta;
import nl.vpro.amara_poms.amara.Utils;

/**
 * @author joost
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AmaraTaskCollection {

    private final static Logger LOG = LoggerFactory.getLogger(AmaraTaskCollection.class);

    public AmaraMeta meta;

    @JsonProperty("objects")
    List<AmaraTask> amaraTasks;

    public static List<AmaraTask> getList() {

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<AmaraTaskCollection> request = new HttpEntity<>(Utils.getGetHeaders());
        ResponseEntity<AmaraTaskCollection> response = restTemplate.exchange(Utils.getUriForPath("api/task"), HttpMethod.GET, request, AmaraTaskCollection.class);
        AmaraTaskCollection amaryActivityCollection = response.getBody();

        HttpStatus httpStatus = response.getStatusCode();

        LOG.info(String.valueOf(response));

        return  amaryActivityCollection.amaraTasks;
    }

    public static List<AmaraTask> getListForType(String taskType, long afterTimestampInSeconds) {

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
        HttpEntity<AmaraTaskCollection> request = new HttpEntity<>(Utils.getGetHeaders());
        ResponseEntity<AmaraTaskCollection> response = restTemplate.exchange(uri, HttpMethod.GET, request, AmaraTaskCollection.class);
        AmaraTaskCollection amaryActivityCollection = response.getBody();

        HttpStatus httpStatus = response.getStatusCode();
//        logger.info(String.valueOf(response));

        return  amaryActivityCollection.amaraTasks;
    }

}
