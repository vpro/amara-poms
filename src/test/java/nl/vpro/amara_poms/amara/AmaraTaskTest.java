package nl.vpro.amara_poms.amara;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.vpro.amara_poms.Config;
import nl.vpro.amara_poms.amara.task.AmaraTask;
import nl.vpro.amara_poms.amara.task.AmaraTaskCollection;

/**
 * @author joost
 */
public class AmaraTaskTest  {

    private final static Logger LOG = LoggerFactory.getLogger(AmaraSubtitlesTest.class);

    @Before
    public void setUp() {
        Config.init();
    }

//    public void testPost() {
//
//        AmaraTask amaraTask = new AmaraTask("gDq7bAA5XFCR", "nl", "Translate", "netinnl");
//
//        AmaraTask newAmaraTask = AmaraTask.post(amaraTask);
//
//        assertNotNull(newAmaraTask);
//    }

    @Test
    public void testGet() {
        long afterTimestampInSeconds = Config.getRequiredConfigAsLong("amara.task.fetchlastperiod.seconds");

        long now = System.currentTimeMillis() / 1000;

        List<AmaraTask> amaraTasks = AmaraTaskCollection.getListForType(AmaraTask.TYPE_TRANSLATE, now - afterTimestampInSeconds);

        LOG.info("Count:" + amaraTasks.size());
        if (amaraTasks.size() > 0) {
            LOG.info(amaraTasks.get(0).toString());
        }
        LOG.info(amaraTasks.toString());

    }

}
