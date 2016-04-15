package nl.vpro.amara_poms.amara.subtitles;

import nl.vpro.amara_poms.Config;
import nl.vpro.amara_poms.amara.Utils;
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

/**
 * Created by joost on 15/04/16.
 */
public class AmaraSubtitleAction {
    final static Logger logger = LoggerFactory.getLogger(AmaraSubtitleAction.class);

    final public static String ACTION_COMPLETE = "complete";
    final public static String ACTION_APPROVE = "approve";

    public String action; // only used to approve nl subtitle

    public AmaraSubtitleAction() {
    }

    public AmaraSubtitleAction(String action) {
        this.action = action;
    }

    public static AmaraSubtitleAction post(AmaraSubtitleAction amaraSubtitleAction, String video_id, String language_code) {

        AmaraSubtitleAction amaraSubtitleActionOut = null;

        try {
            // construct uri
            String url = Config.getRequiredConfig("amara.api.url") + "api/videos/" + video_id + "/languages/" +
                    language_code + "/subtitles/actions/";
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
            URI uri = builder.build().encode().toUri();

            // do request
            RestTemplate restTemplate = new RestTemplate();
            HttpEntity<AmaraSubtitleAction> request = new HttpEntity<>(amaraSubtitleAction, Utils.getPostHeaders());
            ResponseEntity<AmaraSubtitleAction> response = restTemplate.exchange(uri, HttpMethod.POST, request, AmaraSubtitleAction.class);

            // status code is not set in this case
//            if (response.getStatusCode() == HttpStatus.CREATED) {
//                amaraSubtitleActionOut = response.getBody();
//            }
        } catch (HttpClientErrorException e) {
            logger.info(e.toString());
            String responseBody = new String(e.getResponseBodyAsString());
            logger.info(responseBody);
        }

        return  amaraSubtitleActionOut;
    }

}
