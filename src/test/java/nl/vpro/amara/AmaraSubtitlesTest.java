package nl.vpro.amara;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.vpro.amara.domain.Action;
import nl.vpro.amara.domain.Subtitles;
import nl.vpro.amara_poms.Config;
import nl.vpro.amara_poms.poms.PomsBroadcast;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author joost
 */
public class AmaraSubtitlesTest  {

    final static Logger LOG = LoggerFactory.getLogger(AmaraSubtitlesTest.class);

    @Before
    public void setUp() {
        Config.init();
    }

    @Test
    public void testDummy() {
        assertTrue(true);
    }

    @Test
    public void testPost() {

        Subtitles amaraSubtitles = new Subtitles("test subtitles", "vtt",
                "WEBVTT\n" +
                "\n" +
                "1\n" +
                "00:00:02.018 --> 00:00:05.007\n" +
                "888\n" +
                "\n" +
                "2\n" +
                "00:00:05.012 --> 00:00:07.018\n" +
                "TUNE VAN DWDD\n", "test description", "complete");
        Subtitles newAmaraSubtitles = Config.getAmaraClient().post(amaraSubtitles, "gDq7bAA5XFCR", "nl");

        assertNotNull(newAmaraSubtitles);
    }

    @Test
    public void getActions() {
        String video_id = "Ep1jZa6c2NRt";
        List<Action> actions = Config.getAmaraClient().getActions(video_id, "nl");
        System.out.println("" + actions);
    }
    @Test
    public void amarapoms3() {
        String video_id = "Ep1jZa6c2NRt";

        PomsBroadcast pomsBroadcast = new PomsBroadcast("VPWON_1256298");
        pomsBroadcast.downloadSubtitles();

        Subtitles amaraSubtitles = new Subtitles("Blauw Bloed // Een interview met prinses Irene", "vtt",
            pomsBroadcast.getSubtitles(), "Een interview met prinses Irene, we volgen koning Willem-Alexander bij de start van de Giro d'Italia en couturier Paul Schulten vertelt alles over koninklijke bloemetjesjurken.", "save-draft");

        Subtitles newAmaraSubtitles = Config.getAmaraClient().post(amaraSubtitles, video_id, "nl");

        assertNotNull(newAmaraSubtitles);
    }

    public void testGetVTT() {
        String amaraSubtitles = Config.getAmaraClient().getAsVTT("G3CnVJdMw21Y", "nl");

        assertNotNull(amaraSubtitles);

        LOG.info(amaraSubtitles);
    }

    public  void testGet() {
        Subtitles amaraSubtitles = Subtitles.get("G3CnVJdMw21Y", "nl");

        assertNotNull(amaraSubtitles);

        LOG.info(StringUtils.abbreviate(amaraSubtitles.subtitles, 20));
        LOG.info((amaraSubtitles.version_no));

    }
}
