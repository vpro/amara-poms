package nl.vpro.amara;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.vpro.amara_poms.Config;
import nl.vpro.amara.video.AmaraVideoCollection;

/**
 * @author joost
 */
public class AmaraVideoCollectionTest {

    private final static Logger LOG = LoggerFactory.getLogger(AmaraVideoCollectionTest.class);

    @Before
    public void setUp() {
        Config.init();
    }

    @Test
    public void testGetAllVideos(){
        AmaraVideoCollection.getAllVideos();
    }
}
