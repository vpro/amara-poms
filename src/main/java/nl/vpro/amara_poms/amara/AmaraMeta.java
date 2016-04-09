package nl.vpro.amara_poms.amara;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by joost on 09/04/16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AmaraMeta {

    public String previous;
    public String next;
    public int offset;
    public int limit;
    public int total_count;
}
