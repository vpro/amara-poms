package nl.vpro.amara_poms.amara.language;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


/**
 * @author joost
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AmaraLanguage {
    private String code;
    private String name;
    private String ltr;

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
                ", ltr='" + ltr + '\'' +
                '}';
    }

    public String getLtr() {
        return ltr;
    }

    public void setLtr(String ltr) {
        this.ltr = ltr;
    }

}
