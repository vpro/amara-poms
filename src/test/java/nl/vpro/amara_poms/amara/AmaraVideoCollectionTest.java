package nl.vpro.amara_poms.amara;

import junit.framework.TestCase;
import nl.vpro.amara_poms.Config;
import nl.vpro.amara_poms.amara.video.AmaraVideoCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by joost on 05/04/16.
 */
public class AmaraVideoCollectionTest extends TestCase{

    final static Logger logger = LoggerFactory.getLogger(AmaraVideoCollectionTest.class);

    protected void setUp() {

        Config.init();
    }

    public void testGetAllVideos(){


        AmaraVideoCollection.getAllVideos();
    }
}