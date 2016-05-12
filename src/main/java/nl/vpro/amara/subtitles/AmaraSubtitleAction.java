package nl.vpro.amara.subtitles;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import nl.vpro.amara_poms.Config;
import nl.vpro.amara.Utils;

/**
 * @author joost
 */
public class AmaraSubtitleAction {
    private final static Logger LOG = LoggerFactory.getLogger(AmaraSubtitleAction.class);

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
            LOG.info(e.toString());
            String responseBody = e.getResponseBodyAsString();
            LOG.info(responseBody);
        }

        return  amaraSubtitleActionOut;
    }

}
