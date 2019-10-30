package nl.vpro.amara_poms.poms;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import nl.vpro.amara_poms.Config;
import nl.vpro.domain.image.ImageType;
import nl.vpro.domain.media.MediaBuilder;
import nl.vpro.domain.media.support.Image;
import nl.vpro.domain.media.support.OwnerType;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * @author joost
 */
public class PomsBroadcastTest {

    @BeforeEach
    public void setup() {
        Config.init();
    }

    @Test
    public void downloadSubtitlesNotFound() throws Exception {
        PomsBroadcast pomsBroadcast = new PomsBroadcast("AAA", null);

        assertNotEquals(0, pomsBroadcast.downloadSubtitles());
    }

    @Test
    public void downloadSubtitlesFromMainUrl() throws Exception {
        // tt from dwdd
        PomsBroadcast pomsBroadcast = new PomsBroadcast("VARA_101373522", null);

        assertEquals(0, pomsBroadcast.downloadSubtitles());
    }


    @Test
    public void AMARAPOMS3() throws Exception {
        PomsBroadcast pomsBroadcast = new PomsBroadcast("VPWON_1256298", null);

        assertEquals(0, pomsBroadcast.downloadSubtitles());

        System.out.println(pomsBroadcast.getSubtitles());
    }

    @Test
    public void thumbnailUrl() {
        MediaBuilder.ProgramBuilder program = MediaBuilder.program().images(new Image(OwnerType.BROADCASTER, ImageType.PICTURE, "urn:vpro:image:810565"));
        PomsBroadcast pomsBroadcast = new PomsBroadcast("bla", program.build());
        assertThat(pomsBroadcast.getThumbNailUrl()).isEqualTo("https://images.poms.omroep.nl/image/s620/810565.jpg");

    }


}
