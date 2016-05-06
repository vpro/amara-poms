package nl.vpro.amara_poms.amara;

/**
 * @author joost
 */
import java.io.IOException;

import org.joda.time.DateTime;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;

public class JacksonTest {

    private static final String json = "{ \n" +
            "  \"timestamp\" : \"2014-08-20T11:51:31.233Z\" \n" +
            "}";

    @Test
    public void test() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JodaModule());

        System.out.println(mapper.readValue(json, GreetingResource.class));
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
class GreetingResource {
    @JsonProperty("timestamp")
    private DateTime date;

    public DateTime getDate() {
        return date;
    }

    public void setDate(DateTime date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "GreetingResource{" +
                "date=" + date +
                '}';
    }
}
