package nl.vpro.amara_poms;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Michiel Meeuwissen
 * @since 1.13
 */
public class ConfigTest {

    @BeforeEach
    public void setup() {
        Config.init();
    }
    @Test
    public void getFetcher() throws Exception {
        assertThat(Config.getFetcher()).hasSize(2);
    }

    @Test
    public void getDir() throws Exception {
        System.out.println(Config.getRequiredConfig("script.download.url.base"));
        assertThat(Config.getRequiredConfig("script.download.url.base")).startsWith("http://download.omroep.nl/vpro/netinnederland/");
    }

}
