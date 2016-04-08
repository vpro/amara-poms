package nl.vpro.amara_poms.amara;

import junit.framework.TestCase;
import nl.vpro.amara_poms.Config;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

/**
 * Created by joost on 05/04/16.
 */
public class AmaraCollectionTest extends TestCase{

    final static Logger logger = LoggerFactory.getLogger(AmaraCollectionTest.class);

    protected void setUp() {

        Config.init();
    }

    public void testGetAllVideos(){


        AmaraCollection.getAllVideos();
    }
}