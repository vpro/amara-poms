package nl.vpro.amara_poms.amara;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import org.springframework.web.util.UriComponentsBuilder;

/**
 * Created by joost on 05/04/16.
 */
public class AmaraCollection {

    final static Logger logger = LoggerFactory.getLogger(AmaraCollection.class);

    static void getAllVideos() {

        HttpEntity<?> request = new HttpEntity<>(Utils.getGetHeaders());

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<String> response = restTemplate.exchange(Utils.getUriForGetAndPostVideos(), HttpMethod.GET, request, String.class);
        logger.info(String.valueOf(response));

//        RestTemplate restTemplate = new RestTemplate();
//        HttpEntity<String> request = new HttpEntity<String>(headers);
//        ResponseEntity<AmaraVideo[]> response = restTemplate.exchange(builder.build().encode().toUri(), HttpMethod.GET, request, AmaraVideo[].class);
//        AmaraVideo[] amaraVideos = response.getBody();

//        AmaraVideo amaraVideo = restTemplate.getForObject(url, AmaraVideo.class);
//        logger.info(amaraVideo.toString());
    }
}
