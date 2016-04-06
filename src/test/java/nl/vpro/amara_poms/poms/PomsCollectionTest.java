package nl.vpro.amara_poms.poms;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import nl.vpro.amara_poms.Config;
import nl.vpro.amara_poms.poms.PomsCollection;
import nl.vpro.amara_poms.poms.PomsBroadcast;
import nl.vpro.domain.media.update.GroupUpdate;
import nl.vpro.rs.media.MediaRestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.AssertTrue;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

/**
 * Created by joost on 04/04/16.
 */
public class PomsCollectionTest extends TestCase {

    final Logger logger = LoggerFactory.getLogger(PomsCollectionTest.class);

    MediaRestClient client;
    Properties properties;

    /**
     * Create the test case
     *
     * @param testName PomsCollection
     */
    public PomsCollectionTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( PomsCollectionTest.class );
    }

    protected void setUp() {

        Config.init();
    }

    public void testGetCollectionExists() {

        // load collection
        PomsCollection pomsCollection = new PomsCollection("POMS_S_VPRO_1416538");
        assertEquals(0, pomsCollection.getBroadcastsFromPOMS());

        logger.info(pomsCollection.getGroup().toString());
    }

    public void testGetCollectionNotExists() {

        // load collection
        PomsCollection pomsCollection = new PomsCollection("ABC");
        assertNotEquals(0, pomsCollection.getBroadcastsFromPOMS());
    }

}
