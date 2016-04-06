package nl.vpro.amara_poms.amara;

import com.sun.tools.classfile.ConstantPool;
import junit.framework.TestCase;
import nl.vpro.amara_poms.Config;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;
import static org.junit.Assert.assertNotNull;

/**
 * Created by joost on 06/04/16.
 */
public class AmaraVideoTest extends TestCase {

    final static Logger logger = LoggerFactory.getLogger(AmaraVideoTest.class);

    protected void setUp() {

        Config.init();
    }

    public void testPost() {

        String team = Config.getRequiredConfig("amara.api.team");
        AmaraVideo amaraVideo = new AmaraVideo("http://download.omroep.nl/vpro/netinnederland/NPO_bb4.m4v", "nl",
                                    "Joost test (to be deleted)", "gebruikt for testing purposes", team);
        AmaraVideo newAmaraVideo = amaraVideo.post(amaraVideo);

        assertNotNull(newAmaraVideo);
    }

    public void testPostAsString() {

        String team = Config.getRequiredConfig("amara.api.team");
        AmaraVideo amaraVideo = new AmaraVideo("http://download.omroep.nl/vpro/netinnederland/NPO_bb.m4v",
                "nl", "Joost test (to be deleted)", "gebruikt for testing purposes", team);
        String response = amaraVideo.postAsString(amaraVideo);
    }


    public void testGet() {
//        AmaraVideo amaraVideo = AmaraVideo.get("F8i3LbQkBbeG");
        AmaraVideo amaraVideo = AmaraVideo.get("FSW0qzp2Enlk"); // test video

        logger.info(amaraVideo.toString());
    }


    public void testGetAsString() {
        String response = AmaraVideo.getAsString("F8i3LbQkBbeG");

        logger.info(response);
    }


}