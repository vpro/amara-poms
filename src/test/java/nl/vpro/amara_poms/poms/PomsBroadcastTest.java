package nl.vpro.amara_poms.poms;

import org.junit.Before;
import org.junit.Test;

import nl.vpro.amara_poms.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * @author joost
 */
public class PomsBroadcastTest {

    @Before
    public void setup() {
        Config.init();
    }

    @Test
    public void downloadSubtitlesNotFound() throws Exception {
        PomsBroadcast pomsBroadcast = new PomsBroadcast("AAA");

        assertNotEquals(0, pomsBroadcast.downloadSubtitles());
    }

    @Test
    public void downloadSubtitlesFromMainUrl() throws Exception {
        // tt from dwdd
        PomsBroadcast pomsBroadcast = new PomsBroadcast("VARA_101373522");

        assertEquals(0, pomsBroadcast.downloadSubtitles());
    }


    @Test
    public void AMARAPOMS3() throws Exception {
        PomsBroadcast pomsBroadcast = new PomsBroadcast("VPWON_1256298");

        assertEquals(0, pomsBroadcast.downloadSubtitles());

        System.out.println(pomsBroadcast.getSubtitles());
    }


}
