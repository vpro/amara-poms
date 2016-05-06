package nl.vpro.amara_poms.poms;

import org.junit.Test;

import nl.vpro.amara_poms.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * @author joost
 */
public class PomsBroadcastTest {

    @Test
    public void downloadSubtitlesNotFound() throws Exception {
        Config.init();

        PomsBroadcast pomsBroadcast = new PomsBroadcast("AAA");

        assertNotEquals(0, pomsBroadcast.downloadSubtitles());
    }

    @Test
    public void downloadSubtitlesFromMainUrl() throws Exception {
        Config.init();

        // tt from dwdd
        PomsBroadcast pomsBroadcast = new PomsBroadcast("VARA_101373522");

        assertEquals(0, pomsBroadcast.downloadSubtitles());
    }



}
