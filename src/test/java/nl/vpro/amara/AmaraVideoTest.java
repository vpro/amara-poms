package nl.vpro.amara;

import junit.framework.TestCase;
import nl.vpro.amara_poms.Config;
import nl.vpro.amara.video.AmaraVideo;
import nl.vpro.amara.video.AmaraVideoMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Created by joost on 06/04/16.
 */
public class AmaraVideoTest extends TestCase {

    final static Logger logger = LoggerFactory.getLogger(AmaraVideoTest.class);

    protected void setUp() {

        Config.init();
    }

//    public void testPost() {
//
//        String team = Config.getRequiredConfig("amara.api.team");
//        AmaraVideoMetadata amaraVideoMetadata = new AmaraVideoMetadata("test speaker", "test location");
//        AmaraVideo amaraVideo = new AmaraVideo("http://download.omroep.nl/vpro/netinnederland/NPO_bb4.m4v", "nl",
//                                    "Joost test (to be deleted)", "gebruikt for testing purposes", team, amaraVideoMetadata);
//        AmaraVideo newAmaraVideo = amaraVideo.post(amaraVideo);
//
//        assertNotNull(newAmaraVideo);
//    }

    public void testPostAsString() {

        String team = Config.getRequiredConfig("amara.api.team");
        AmaraVideoMetadata amaraVideoMetadata = new AmaraVideoMetadata("test speaker", "test location");
        AmaraVideo amaraVideo = new AmaraVideo("http://download.omroep.nl/vpro/netinnederland/NPO_bb.m4v",
                "nl", "Joost test (to be deleted)", "gebruikt for testing purposes", team, amaraVideoMetadata);
        String response = amaraVideo.postAsString(amaraVideo);
    }


    public void testGet() {
//        AmaraVideo amaraVideo = AmaraVideo.get("F8i3LbQkBbeG");
        AmaraVideo amaraVideo = AmaraVideo.get("FSW0qzp2Enlk"); // test video

        logger.info(amaraVideo.toString());
    }

    public void testPomsMidFromOmroepUrl() {
        AmaraVideo amaraVideo = new AmaraVideo();

        String[] urls = {"http://download.omroep.nl/vpro/netinnederland/h264/VPWON_1166750.m4v"};
        amaraVideo.setAll_urls(urls);

        String foundMid = amaraVideo.getPomsMidFromVideoUrl();

        assertEquals(foundMid, "VPWON_1166750");
    }

    public void testPomsMidFromYoutube() {
        AmaraVideo amaraVideo = new AmaraVideo();

        String[] urls = {"http://www.youtube.com/watch?v=ZXajCrcQ6sw"};
        amaraVideo.setAll_urls(urls);

        String foundMid = amaraVideo.getPomsMidFromVideoUrl();

        assertNull(foundMid);
    }

    public void testPomsMidFromEmpty() {
        AmaraVideo amaraVideo = new AmaraVideo();

        String foundMid = amaraVideo.getPomsMidFromVideoUrl();

        assertNull(foundMid);
    }


}
