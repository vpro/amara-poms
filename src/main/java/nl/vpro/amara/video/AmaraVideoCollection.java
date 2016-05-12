package nl.vpro.amara.video;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import nl.vpro.amara.Utils;

/**
 * @author joost
 */
public class AmaraVideoCollection {

    private final static Logger LOG = LoggerFactory.getLogger(AmaraVideoCollection.class);

    public static void getAllVideos() {

        HttpEntity<?> request = new HttpEntity<>(Utils.getGetHeaders());

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<String> response = restTemplate.exchange(Utils.getUriForGetAndPostVideos(), HttpMethod.GET, request, String.class);
        LOG.info(String.valueOf(response));

//        RestTemplate restTemplate = new RestTemplate();
//        HttpEntity<String> request = new HttpEntity<String>(headers);
//        ResponseEntity<AmaraVideo[]> response = restTemplate.exchange(builder.build().encode().toUri(), HttpMethod.GET, request, AmaraVideo[].class);
//        AmaraVideo[] amaraVideos = response.getBody();

//        AmaraVideo amaraVideo = restTemplate.getForObject(url, AmaraVideo.class);
//        logger.info(amaraVideo.toString());
    }
}
