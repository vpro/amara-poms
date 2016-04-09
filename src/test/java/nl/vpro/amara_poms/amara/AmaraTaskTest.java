package nl.vpro.amara_poms.amara;

import junit.framework.TestCase;
import nl.vpro.amara_poms.Config;
import nl.vpro.amara_poms.amara.task.AmaraTask;
import nl.vpro.amara_poms.amara.task.AmaraTaskCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

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

    public void testGet() {
        long afterTimestampInSeconds = Config.getRequiredConfigAsLong("amara.task.fetchlastperiod.seconds");

        long now = System.currentTimeMillis() / 1000;

        List<AmaraTask> amaraTasks = AmaraTaskCollection.getListForType(AmaraTask.TYPE_TRANSLATE, now - afterTimestampInSeconds);

        logger.info("Count:" + amaraTasks.size());
        if (amaraTasks.size() > 0) {
            logger.info(amaraTasks.get(0).toString());
        }
        logger.info(amaraTasks.toString());

    }

}