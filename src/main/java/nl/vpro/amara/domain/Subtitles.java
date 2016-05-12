package nl.vpro.amara.domain;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import nl.vpro.amara_poms.Config;

/**
 * @author joost
 * @todo static methods should not be in domain
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Subtitles {
    private final static Logger LOG = LoggerFactory.getLogger(Subtitles.class);

    public String action; // only used for post, possible values: complete

    public String version_number; // version number for the subtitles
    public String subtitles; // Subtitle data (str)
    public String sub_format; // Format of the subtitles
    public Language language; // Language data

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
    private VideoMetadata metadata; // Video metadata, translated into the subtitle’s language
    public String video_title; // Video title, translated into the video’s language
    public String video_description; // Video description, translated into the video’s language
    public String resource_uri; // API URI for the subtitles
    public String site_uri; // URI to view the subtitles on site
    public String video; // Copy of video_title (deprecated)
    public String version_no; // Copy of version_number (deprecated)

    @JsonIgnore
    public VideoMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(VideoMetadata metadata) {
        this.metadata = metadata;
    }

    public Subtitles() {
    }

    public Subtitles(String title, String sub_format, String subtitles, String description, String action) {
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

    public void writeSubtitlesToFiles(String pomsMid) throws FileNotFoundException {
        try(PrintWriter out = new PrintWriter(getSubtitleFilepath(pomsMid))){
            out.println(subtitles);
        }
    }







}
