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
public class AmaraTaskTest extends TestCase {

    final static Logger logger = LoggerFactory.getLogger(AmaraSubtitlesTest.class);

    protected void setUp() {

        Config.init();
    }

    public void testPost() {

        AmaraTask amaraTask = new AmaraTask("gDq7bAA5XFCR", "nl", "Translate", "netinnl");

        AmaraTask newAmaraTask = AmaraTask.post(amaraTask);

        assertNotNull(newAmaraTask);
    }


}