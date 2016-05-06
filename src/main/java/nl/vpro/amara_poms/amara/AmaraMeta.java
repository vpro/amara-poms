package nl.vpro.amara_poms.amara;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author joost
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AmaraMeta {

    public String previous;
    public String next;
    public int offset;
    public int limit;
    public int total_count;
}
