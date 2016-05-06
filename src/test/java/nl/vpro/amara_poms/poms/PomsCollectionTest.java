package nl.vpro.amara_poms.poms;

import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.vpro.amara_poms.Config;
import nl.vpro.rs.media.MediaRestClient;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * @author joost
 */
public class PomsCollectionTest  {

    private final static Logger LOG = LoggerFactory.getLogger(PomsCollectionTest.class);

    MediaRestClient client;
    Properties properties;

    @Before
    public void setUp() {
        Config.init();
    }

    @Test
    public void testGetCollectionExists() {

        // load collection
        PomsCollection pomsCollection = new PomsCollection("POMS_S_VPRO_1416538");
        assertEquals(0, pomsCollection.getBroadcastsFromPOMS());

        LOG.info(pomsCollection.getGroup().toString());
    }

    @Test
    public void testGetCollectionNotExists() {
        // load collection
        PomsCollection pomsCollection = new PomsCollection("ABC");
        assertNotEquals(0, pomsCollection.getBroadcastsFromPOMS());
    }



}
