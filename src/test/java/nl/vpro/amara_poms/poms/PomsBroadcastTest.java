package nl.vpro.amara_poms.poms;

import nl.vpro.amara_poms.Config;
import nl.vpro.domain.media.update.MemberRefUpdate;
import nl.vpro.domain.media.update.ProgramUpdate;
import org.junit.Test;

import java.util.SortedSet;

import static org.junit.Assert.*;

/**
 * Created by joost on 05/04/16.
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
