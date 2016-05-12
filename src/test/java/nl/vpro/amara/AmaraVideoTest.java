package nl.vpro.amara;

import junit.framework.TestCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.vpro.amara.domain.Video;
import nl.vpro.amara.domain.VideoMetadata;
import nl.vpro.amara_poms.Config;

/**
 * @author Joost
 */
public class AmaraVideoTest extends TestCase {

    private final static Logger LOG = LoggerFactory.getLogger(AmaraVideoTest.class);

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
        VideoMetadata amaraVideoMetadata = new VideoMetadata("test speaker", "test location");
        Video amaraVideo = new Video("http://download.omroep.nl/vpro/netinnederland/NPO_bb.m4v",
                "nl", "Joost test (to be deleted)", "gebruikt for testing purposes", team, amaraVideoMetadata);
        String response = amaraVideo.postAsString(amaraVideo);
    }


    public void testGet() {
//        AmaraVideo amaraVideo = AmaraVideo.get("F8i3LbQkBbeG");
        Video amaraVideo = Config.getAmaraClient().getVideo("FSW0qzp2Enlk"); // test video

        LOG.info(amaraVideo.toString());
    }

    public void testPomsMidFromOmroepUrl() {
        Video amaraVideo = new Video();

        String[] urls = {"http://download.omroep.nl/vpro/netinnederland/h264/VPWON_1166750.m4v"};
        amaraVideo.setAll_urls(urls);

        String foundMid = amaraVideo.getPomsMidFromVideoUrl();

        assertEquals(foundMid, "VPWON_1166750");
    }

    public void testPomsMidFromYoutube() {
        Video amaraVideo = new Video();

        String[] urls = {"http://www.youtube.com/watch?v=ZXajCrcQ6sw"};
        amaraVideo.setAll_urls(urls);

        String foundMid = amaraVideo.getPomsMidFromVideoUrl();

        assertNull(foundMid);
    }

    public void testPomsMidFromEmpty() {
        Video amaraVideo = new Video();

        String foundMid = amaraVideo.getPomsMidFromVideoUrl();

        assertNull(foundMid);
    }


}
