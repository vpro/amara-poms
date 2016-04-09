package nl.vpro.amara_poms.amara;

import nl.vpro.amara_poms.Config;
import nl.vpro.amara_poms.amara.activity.AmaraActivity;
import nl.vpro.amara_poms.amara.activity.AmaraActivityCollection;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by joost on 08/04/16.
 */
public class AmaraActivityTest {

    final static Logger logger = LoggerFactory.getLogger(AmaraActivityTest.class);


    @Test
    public void get() throws Exception {
        Config.init();

        AmaraActivity amaraActivity = AmaraActivity.get("https://staging.amara.org/api/activity/5036197/");

        logger.info(amaraActivity.toString());
        if (amaraActivity.getCreated() != null) {
            logger.info((amaraActivity.getCreated()).toString());
        }
    }

    @Test
    public void getList() throws Exception {
        Config.init();

        List<AmaraActivity> amaraActivities = AmaraActivityCollection.getList();

        logger.info("Count:" + amaraActivities.size());
        if (amaraActivities.size() > 0) {
            logger.info(amaraActivities.get(0).toString());
        }
        logger.info(amaraActivities.toString());
    }

    @Test
    public void getListForType() throws Exception {
        Config.init();

        long afterTimestampInSeconds = Config.getRequiredConfigAsLong("amara.task.fetchlastperiod.seconds");

        long now = System.currentTimeMillis() / 1000;

        List<AmaraActivity> amaraActivities = AmaraActivityCollection.getListForType(AmaraActivity.TYPE_APPROVE_VERSION, now - afterTimestampInSeconds);

        logger.info("Count:" + amaraActivities.size());
        if (amaraActivities.size() > 0) {
            logger.info(amaraActivities.get(0).toString());
        }
        logger.info(amaraActivities.toString());
    }


}