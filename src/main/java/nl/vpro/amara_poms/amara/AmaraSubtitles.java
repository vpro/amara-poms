package nl.vpro.amara_poms.amara;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import nl.vpro.amara_poms.Config;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import org.apache.commons.lang3.StringUtils;

import static com.sun.xml.bind.v2.util.XmlFactory.logger;

/**
 * Created by joost on 06/04/16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AmaraSubtitles {

    String title;
    String subtitles;
    String sub_format;
    String description;
    String action;

    public AmaraSubtitles() {
    }

    public AmaraSubtitles(String title, String sub_format, String subtitles, String description, String action) {
        this.subtitles = subtitles;
        this.sub_format = sub_format;
        this.title = title;
        this.description = description;
        this.action = action;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitles() {
        return subtitles;
    }
    public void setSubtitles(String subtitles) {
        this.subtitles = subtitles;
    }

    public String getSub_format() {
        return sub_format;
    }
    public void setSub_format(String sub_format) {
        this.sub_format = sub_format;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public String getAction() {
        return action;
    }
    public void setAction(String action) {
        this.action = action;
    }

    @Override
    public String toString() {
        return "AmaraSubtitles{" +
                "subtitles='" + StringUtils.abbreviate(subtitles, 20) + '\'' +
                ", sub_format='" + sub_format + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", action='" + action + '\'' +
                '}';
    }

    private static URI getPostUri(String video_id, String language_code) {
        String url = Config.getRequiredConfig("amara.api.url") + "api/videos/" + video_id + "/languages/" +
                language_code + "/subtitles/";

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("team", Config.getRequiredConfig("amara.api.team"));

        return (builder.build().encode().toUri());
    }


    public static AmaraSubtitles post(AmaraSubtitles amaraSubtitlesIn, String video_id, String language_code) {

        AmaraSubtitles amaraSubtitlesOut = null;

        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpEntity<AmaraSubtitles> request = new HttpEntity<>(amaraSubtitlesIn, Utils.getPostHeaders());
            ResponseEntity<AmaraSubtitles> response = restTemplate.exchange(getPostUri(video_id, language_code),
                    HttpMethod.POST, request, AmaraSubtitles.class);

            if (response.getStatusCode() == HttpStatus.CREATED) {
                amaraSubtitlesOut = response.getBody();
            }
        } catch (HttpClientErrorException e) {
            logger.info(e.toString());
            String responseBody = new String(e.getResponseBodyAsString());
            logger.info(responseBody);
        }

        return  amaraSubtitlesOut;
    }


}
