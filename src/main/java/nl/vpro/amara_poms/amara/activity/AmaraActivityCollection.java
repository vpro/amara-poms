package nl.vpro.amara_poms.amara.activity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import nl.vpro.amara_poms.Config;
import nl.vpro.amara_poms.amara.AmaraMeta;
import nl.vpro.amara_poms.amara.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * Created by joost on 09/04/16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AmaraActivityCollection {
    final static Logger logger = LoggerFactory.getLogger(AmaraActivityCollection.class);

    public AmaraMeta meta;

    @JsonProperty("objects")
    List<AmaraActivity> amaraActivities;

    public static List<AmaraActivity> getList() {

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<AmaraActivityCollection> request = new HttpEntity<>(Utils.getGetHeaders());
        ResponseEntity<AmaraActivityCollection> response = restTemplate.exchange(Utils.getUriForPath("api/activity"), HttpMethod.GET, request, AmaraActivityCollection.class);
        AmaraActivityCollection amaryActivityCollection = response.getBody();

        HttpStatus httpStatus = response.getStatusCode();

        logger.info(String.valueOf(response));

        return  amaryActivityCollection.amaraActivities;
    }

    public static List<AmaraActivity> getListForType(int activityType, long afterTimestampInSeconds) {

        // build url
        String url = Config.getRequiredConfig("amara.api.url") + "api/activity/";
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("team", Config.getRequiredConfig("amara.api.team"))
                .queryParam("type", activityType)
                .queryParam("after", afterTimestampInSeconds);
        URI uri = builder.build().encode().toUri();

        // do request
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<AmaraActivityCollection> request = new HttpEntity<>(Utils.getGetHeaders());
        ResponseEntity<AmaraActivityCollection> response = restTemplate.exchange(uri, HttpMethod.GET, request, AmaraActivityCollection.class);
        AmaraActivityCollection amaryActivityCollection = response.getBody();

        HttpStatus httpStatus = response.getStatusCode();
        logger.info(String.valueOf(response));

        return  amaryActivityCollection.amaraActivities;
    }


}
