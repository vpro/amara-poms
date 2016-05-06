package nl.vpro.amara_poms.amara.subtitles;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import nl.vpro.amara_poms.Config;
import nl.vpro.amara_poms.amara.Utils;
import nl.vpro.amara_poms.amara.language.AmaraLanguage;
import nl.vpro.amara_poms.amara.video.AmaraVideoMetadata;

/**
 * @author joost
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AmaraSubtitles {
    private final static Logger LOG = LoggerFactory.getLogger(AmaraSubtitles.class);

    public String action; // only used for post, possible values: complete

    public String version_number; // version number for the subtitles
    public String subtitles; // Subtitle data (str)
    public String sub_format; // Format of the subtitles
    public AmaraLanguage language; // Language data

    @Override
    public String toString() {
        return "AmaraSubtitles{" +
                "action='" + action + '\'' +
                ", version_number='" + version_number + '\'' +
                ", subtitles='" + subtitles + '\'' +
                ", sub_format='" + sub_format + '\'' +
                ", language=" + language +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", metadata='" + metadata + '\'' +
                ", video_title='" + video_title + '\'' +
                ", video_description='" + video_description + '\'' +
                ", resource_uri='" + resource_uri + '\'' +
                ", site_uri='" + site_uri + '\'' +
                ", video='" + video + '\'' +
                ", version_no='" + version_no + '\'' +
                '}';
    }

    public String title; // Video title, translated into the subtitle’s language
    public String description; // Video description, translated into the subtitle’s language
    private AmaraVideoMetadata metadata; // Video metadata, translated into the subtitle’s language
    public String video_title; // Video title, translated into the video’s language
    public String video_description; // Video description, translated into the video’s language
    public String resource_uri; // API URI for the subtitles
    public String site_uri; // URI to view the subtitles on site
    public String video; // Copy of video_title (deprecated)
    public String version_no; // Copy of version_number (deprecated)

    @JsonIgnore
    public AmaraVideoMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(AmaraVideoMetadata metadata) {
        this.metadata = metadata;
    }

    public AmaraSubtitles() {
    }

    public AmaraSubtitles(String title, String sub_format, String subtitles, String description, String action) {
        this.subtitles = subtitles;
        this.sub_format = sub_format;
        this.title = title;
        this.description = description;
        this.action = action;
    }

    public String getSubtitleFilepath(String filename) {
        String basePath = Config.getRequiredConfig("subtitle.basepath");

        basePath += language.getCode() + "/" + filename;

        return basePath;
    }

    public int writeSubtitlesToFiles(String pomsMid) {
        int returnValue = Config.NO_ERROR;

        try( PrintWriter out = new PrintWriter(getSubtitleFilepath(pomsMid))){
            out.println(subtitles);
        } catch (FileNotFoundException e) {
            LOG.error("Error writing subtitles to file " + getSubtitleFilepath(pomsMid));
            LOG.error(e.toString());
            returnValue = Config.ERROR_WRITING_SUBTITLES_TO_FILE;
        }

        return returnValue;
    }

    public static AmaraSubtitles post(AmaraSubtitles amaraSubtitlesIn, String video_id, String language_code) {

        AmaraSubtitles amaraSubtitlesOut = null;

        try {
            // construct uri
            String url = Config.getRequiredConfig("amara.api.url") + "api/videos/" + video_id + "/languages/" +
                    language_code + "/subtitles/";
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                    .queryParam("team", Config.getRequiredConfig("amara.api.team"));
            URI uri = builder.build().encode().toUri();

            // do request
            RestTemplate restTemplate = new RestTemplate();
            HttpEntity<AmaraSubtitles> request = new HttpEntity<>(amaraSubtitlesIn, Utils.getPostHeaders());
            ResponseEntity<AmaraSubtitles> response = restTemplate.exchange(uri, HttpMethod.POST, request, AmaraSubtitles.class);

            if (response.getStatusCode() == HttpStatus.CREATED) {
                amaraSubtitlesOut = response.getBody();
            }
        } catch (HttpClientErrorException e) {
            LOG.info(e.toString());
            String responseBody = new String(e.getResponseBodyAsString());
            LOG.info(responseBody);
        }

        return  amaraSubtitlesOut;
    }

    // DOESN'T WORK BECAUSE SUBTITLE ARE ALSO IN JSON FORMAT - to get it working a more detailed model is needed
    public static AmaraSubtitles get(String video_id, String language_code) {

        AmaraSubtitles amaraSubtitlesOut = null;

        try {
            // construct uri
            String url = Config.getRequiredConfig("amara.api.url") + "api/videos/" + video_id + "/languages/" +
                    language_code + "/subtitles/";
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
//                    .queryParam("team", Config.getRequiredConfig("amara.api.team"))
                    .queryParam("sub_format", Config.getRequiredConfig("amara.subtitles.format"));
            URI uri = builder.build().encode().toUri();

            // do request
            RestTemplate restTemplate = new RestTemplate();
            HttpEntity<AmaraSubtitles> request = new HttpEntity<>(Utils.getGetHeaders());
            ResponseEntity<AmaraSubtitles> response = restTemplate.exchange(uri, HttpMethod.GET, request, AmaraSubtitles.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                amaraSubtitlesOut = response.getBody();
            }
        } catch (HttpClientErrorException e) {
            LOG.info(e.toString());
            String responseBody = new String(e.getResponseBodyAsString());
            LOG.info(responseBody);
        }

        return  amaraSubtitlesOut;
    }



    public static String getAsVTT(String video_id, String language_code) {

        String amaraSubtitles = null;

        try {
            // construct uri
            String url = Config.getRequiredConfig("amara.api.url") + "api/videos/" + video_id + "/languages/" +
                    language_code + "/subtitles/";
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                    .queryParam("team", Config.getRequiredConfig("amara.api.team"))
                    .queryParam("format", Config.getRequiredConfig("amara.subtitles.format"));
            URI uri = builder.build().encode().toUri();

            RestTemplate restTemplate = new RestTemplate();
            HttpEntity<AmaraSubtitles> request = new HttpEntity<>(Utils.getGetHeaders());
            HttpEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, request, String.class);
            LOG.info(response.getBody());

            // // TODO: 09/04/16 fix
//            if (response.statusCode = HttpStatus.OK) {
                amaraSubtitles = response.getBody();
//            }

        } catch (HttpClientErrorException e) {
            LOG.info(e.toString());
            String responseBody = new String(e.getResponseBodyAsString());
            LOG.info(responseBody);
        }

        return  amaraSubtitles;
    }


}
