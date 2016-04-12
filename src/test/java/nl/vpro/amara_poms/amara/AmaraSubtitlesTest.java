package nl.vpro.amara_poms.amara;

import junit.framework.TestCase;
import nl.vpro.amara_poms.Config;
import nl.vpro.amara_poms.amara.subtitles.AmaraSubtitles;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by joost on 06/04/16.
 */
public class AmaraSubtitlesTest extends TestCase {

    final static Logger logger = LoggerFactory.getLogger(AmaraSubtitlesTest.class);

    protected void setUp() {

        Config.init();
    }

    public void testDummy() {
        assertTrue(true);
    }

//    public void testPost() {
//
//        AmaraSubtitles amaraSubtitles = new AmaraSubtitles("test subtitles", "vtt",
//                "WEBVTT\n" +
//                "\n" +
//                "1\n" +
//                "00:00:02.018 --> 00:00:05.007\n" +
//                "888\n" +
//                "\n" +
//                "2\n" +
//                "00:00:05.012 --> 00:00:07.018\n" +
//                "TUNE VAN DWDD\n", "test description", "complete");
//        AmaraSubtitles newAmaraSubtitles = AmaraSubtitles.post(amaraSubtitles, "gDq7bAA5XFCR", "nl");
//
//        assertNotNull(newAmaraSubtitles);
//    }
//
//    public void testGetVTT() {
//        String amaraSubtitles = AmaraSubtitles.getAsVTT("G3CnVJdMw21Y", "nl");
//
//        assertNotNull(amaraSubtitles);
//
//        logger.info(amaraSubtitles);
//    }
//
//    public  void testGet() {
//        AmaraSubtitles amaraSubtitles = AmaraSubtitles.get("G3CnVJdMw21Y", "nl");
//
//        assertNotNull(amaraSubtitles);
//
//        logger.info(StringUtils.abbreviate(amaraSubtitles.subtitles, 20));
//        logger.info((amaraSubtitles.version_no));
//
//    }
}
