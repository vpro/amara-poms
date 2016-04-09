package nl.vpro.amara_poms.amara;

import nl.vpro.amara_poms.Config;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

/**
 * Created by joost on 06/04/16.
 */
public class Utils {

    public static HttpHeaders getGetHeaders() {
        HttpHeaders headers = new HttpHeaders();
//        headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);
//        headers.add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        headers.add("X-api-username", Config.getRequiredConfig("amara.api.username"));
        headers.add("X-api-key", Config.getRequiredConfig("amara.api.key"));

        return  headers;
    }

    public static HttpHeaders getPostHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);
        headers.add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        headers.add("X-api-username", Config.getRequiredConfig("amara.api.username"));
        headers.add("X-api-key", Config.getRequiredConfig("amara.api.key"));
//        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        return  headers;
    }


    /**
     * Uri for Get and Post videos
     * @return
     */
    public  static URI getUriForGetAndPostVideos() {
        String url = Config.getRequiredConfig("amara.api.url") + "api/videos/";

//        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
//                .queryParam("team", Config.getRequiredConfig("amara.api.team"));

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);


        return (builder.build().encode().toUri());
    }

    /**
     * Uri for Get videos
     * @return
     */
    public  static URI getUriForGetVideoWithId(String videoId) {
        String url = Config.getRequiredConfig("amara.api.url") + "api/videos/" + videoId;

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("team", Config.getRequiredConfig("amara.api.team"));

        return (builder.build().encode().toUri());
    }

    /**
     * Uri for url
     * @return
     */
    public  static URI getUriForUri(String url) {

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);

        return (builder.build().encode().toUri());
    }

    /**
     * Uri for url with team
     * @return
     */
    public  static URI getUriForUriWithTeam(String url) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("team", Config.getRequiredConfig("amara.api.team"));

        return (builder.build().encode().toUri());
    }

    /**
     * Uri for path
     * @return
     */
    public  static URI getUriForPath(String path) {
        String url = Config.getRequiredConfig("amara.api.url") + path;

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);

        return (builder.build().encode().toUri());
    }

}
