package nl.vpro.amara_poms.amara.video;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by joost on 09/04/16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)

public class AmaraVideoMetadata {

    @JsonProperty("speaker-name")
    public String speaker_name;
    public String location;

    public AmaraVideoMetadata() {
    }

    @Override
    public String toString() {
        return "AmaraVideoMetadata{" +
                "speaker_name='" + speaker_name + '\'' +
                ", location='" + location + '\'' +
                '}';
    }
}
