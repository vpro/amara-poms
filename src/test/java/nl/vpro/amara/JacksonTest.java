package nl.vpro.amara;

/**
 * @author joost
 */

import java.io.IOException;
import java.time.Instant;

import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import static org.assertj.core.api.Assertions.assertThat;


public class JacksonTest {

    private static final String json = "{ \n" +
            "  \"timestamp\" : \"2014-08-20T11:51:31.233Z\" \n" +
            "}";

    @Test
    public void test() throws IOException {

        GreetingResource greeting = AmaraObjectMapper.INSTANCE.readValue(json, GreetingResource.class);
        assertThat(greeting.getDate()).isEqualTo(Instant.parse("2014-08-20T11:51:31.233Z"));
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
class GreetingResource {
    @JsonProperty("timestamp")
    private Instant date;

    public Instant getDate() {
        return date;
    }

    public void setDate(Instant date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "GreetingResource{" +
                "date=" + date +
                '}';
    }
}
