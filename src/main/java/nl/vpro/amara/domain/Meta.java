package nl.vpro.amara.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author joost
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Meta {

    public String previous;
    public String next;
    public int offset;
    public int limit;
    public int total_count;
}
