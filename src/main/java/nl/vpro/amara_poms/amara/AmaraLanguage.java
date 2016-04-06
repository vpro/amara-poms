package nl.vpro.amara_poms.amara;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


/**
 * Created by joost on 05/04/16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AmaraLanguage {
    private String code;
    private String name;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "AmaraLanguage{" +
                "code='" + code + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
