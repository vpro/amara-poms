package nl.vpro.amara_poms.poms;

import nl.vpro.amara_poms.Config;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by joost on 05/04/16.
 */
public class PomsBroadcastTest {

    @Test
    public void downloadFileToDownloadServerFailsWithUnknownMid() throws Exception {
        Config.init();
        PomsBroadcast pomsBroadcast = new PomsBroadcast("AAA");

        assertNotEquals(0, pomsBroadcast.downloadFileToDownloadServer());
    }

    @Test
    public void downloadFileToDownloadServerSuccess() throws Exception {
        Config.init();

        // todo create input file

        PomsBroadcast pomsBroadcast = new PomsBroadcast("BBB");

        assertEquals(0, pomsBroadcast.downloadFileToDownloadServer());

        // todo check file

    }

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

    @Test
    public void downloadSubtitlesFromBackupUrl() throws Exception {
        Config.init();

        PomsBroadcast pomsBroadcast = new PomsBroadcast("AAA");

        assertEquals(0, pomsBroadcast.downloadSubtitles());
    }


}