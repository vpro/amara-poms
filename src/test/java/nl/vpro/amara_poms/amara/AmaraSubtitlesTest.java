package nl.vpro.amara_poms.amara;

import junit.framework.TestCase;
import nl.vpro.amara_poms.Config;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

/**
 * Created by joost on 06/04/16.
 */
public class AmaraSubtitlesTest extends TestCase {

    final static Logger logger = LoggerFactory.getLogger(AmaraSubtitlesTest.class);

    protected void setUp() {

        Config.init();
    }

    public void testPost() {

        AmaraSubtitles amaraSubtitles = new AmaraSubtitles("test subtitles", "vtt",
                "WEBVTT\n" +
                "\n" +
                "1\n" +
                "00:00:02.018 --> 00:00:05.007\n" +
                "888\n" +
                "\n" +
                "2\n" +
                "00:00:05.012 --> 00:00:07.018\n" +
                "TUNE VAN DWDD\n", "test description", "complete");
        AmaraSubtitles newAmaraSubtitles = AmaraSubtitles.post(amaraSubtitles, "gDq7bAA5XFCR", "nl");

        assertNotNull(newAmaraSubtitles);
    }
}