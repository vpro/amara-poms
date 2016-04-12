package nl.vpro.amara_poms.amara.video;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import com.fasterxml.jackson.annotation.JsonInclude;
import nl.vpro.amara_poms.amara.language.AmaraLanguage;
import nl.vpro.amara_poms.amara.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * Created by joost on 05/04/16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class AmaraVideo {

    final static Logger logger = LoggerFactory.getLogger(AmaraVideo.class);


    private String id;
    private String video_url;
    private String title;
    private String description;
//    private String duration;
    private String primary_audio_language_code;
    private String thumbnail;
    private AmaraVideoMetadata metadata;
    private String team;
    private String project;

    private AmaraLanguage amaraLanguage;


    // constructor with no parameter
    public AmaraVideo() {}

    public AmaraVideo(String video_url, String primary_audio_language_code, String title, String description, String team, AmaraVideoMetadata metadata) {
        this.video_url = video_url;
        this.primary_audio_language_code = primary_audio_language_code;
        this.title = title;
        this.description = description;
        this.team = team;
        this.metadata = metadata;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVideo_url() {
        return video_url;
    }

    public void setVideo_url(String video_url) {
        this.video_url = video_url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

//    public String getDuration() {
//        return duration;
//    }
//
//    public void setDuration(String duration) {
//        this.duration = duration;
//    }

    public String getPrimary_audio_language_code() {
        return primary_audio_language_code;
    }

    public void setPrimary_audio_language_code(String primary_audio_language_code) {
        this.primary_audio_language_code = primary_audio_language_code;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public AmaraVideoMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(AmaraVideoMetadata metadata) {
        this.metadata = metadata;
    }

    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public AmaraLanguage getAmaraLanguage() {
        return amaraLanguage;
    }

    public void setAmaraLanguage(AmaraLanguage amaraLanguage) {
        this.amaraLanguage = amaraLanguage;
    }

    @Override
    public String toString() {
        return "AmaraVideo{" +
                "id='" + id + '\'' +
                ", video_url='" + video_url + '\'' +
                ", primary_audio_language_code='" + primary_audio_language_code + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", team='" + team + '\'' +
                ", amaraLanguage=" + amaraLanguage +
                '}';
    }

    public static AmaraVideo post(AmaraVideo amaraVideoIn) {

        AmaraVideo amaraVideoOut = null;

        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpEntity<AmaraVideo> request = new HttpEntity<>(amaraVideoIn, Utils.getPostHeaders());
            ResponseEntity<AmaraVideo> response = restTemplate.exchange(Utils.getUriForGetAndPostVideos(), HttpMethod.POST, request, AmaraVideo.class);

            if (response.getStatusCode() == HttpStatus.CREATED) {
                amaraVideoOut = response.getBody();
            }
        } catch (HttpClientErrorException e) {
            logger.info(e.toString());
            String responseBody = new String(e.getResponseBodyAsString());
            logger.info(responseBody);
        }

        return  amaraVideoOut;
    }

    public static String postAsString(AmaraVideo amaraVideoIn) {

        String stringResponse = "";

        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpEntity<AmaraVideo> request = new HttpEntity<>(amaraVideoIn, Utils.getPostHeaders());
            HttpEntity<String> response = restTemplate.exchange(Utils.getUriForGetAndPostVideos(), HttpMethod.POST, request, String.class);

            stringResponse = response.getBody();
            logger.info(stringResponse);
        } catch (HttpClientErrorException e) {
            logger.info(e.toString());
            String responseBody = new String(e.getResponseBodyAsString());
            logger.info(responseBody);
        }

        return stringResponse;
    }


    public static AmaraVideo get(String videoId) {

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<AmaraVideo> request = new HttpEntity<>(Utils.getGetHeaders());
        ResponseEntity<AmaraVideo> response = restTemplate.exchange(Utils.getUriForGetVideoWithId(videoId), HttpMethod.GET, request, AmaraVideo.class);
        AmaraVideo amaraVideo = response.getBody();

//        logger.info(String.valueOf(response));

        return  amaraVideo;
    }

    public  static String getAsString(String videoId) {

        HttpEntity<AmaraVideo> request = new HttpEntity<>(Utils.getGetHeaders());

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<String> response = restTemplate.exchange(Utils.getUriForGetVideoWithId(videoId), HttpMethod.GET, request, String.class);
        logger.info(response.getBody());

        return response.getBody();
    }


}

