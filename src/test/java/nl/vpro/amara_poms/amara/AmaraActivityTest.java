package nl.vpro.amara_poms.amara;

import java.util.List;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.vpro.amara_poms.Config;
import nl.vpro.amara_poms.amara.activity.AmaraActivity;
import nl.vpro.amara_poms.amara.activity.AmaraActivityCollection;

/**
 * @author joost
 */
public class AmaraActivityTest {

    private final static Logger LOG = LoggerFactory.getLogger(AmaraActivityTest.class);


    @Test
    public void get() throws Exception {
        Config.init();

        AmaraActivity amaraActivity = AmaraActivity.get("https://staging.amara.org/api/activity/5036197/");

        LOG.info(amaraActivity.toString());
        if (amaraActivity.getCreated() != null) {
            LOG.info((amaraActivity.getCreated()).toString());
        }
    }

    @Test
    public void getList() throws Exception {
        Config.init();

        List<AmaraActivity> amaraActivities = AmaraActivityCollection.getList();

        LOG.info("Count:" + amaraActivities.size());
        if (amaraActivities.size() > 0) {
            LOG.info(amaraActivities.get(0).toString());
        }
        LOG.info(amaraActivities.toString());
    }

    @Test
    public void getListForType() throws Exception {
        Config.init();

        long afterTimestampInSeconds = Config.getRequiredConfigAsLong("amara.task.fetchlastperiod.seconds");

        long now = System.currentTimeMillis() / 1000;

        List<AmaraActivity> amaraActivities = AmaraActivityCollection.getListForType(AmaraActivity.TYPE_APPROVE_VERSION, now - afterTimestampInSeconds);

        LOG.info("Count:" + amaraActivities.size());
        if (amaraActivities.size() > 0) {
            LOG.info(amaraActivities.get(0).toString());
        }
        LOG.info(amaraActivities.toString());
    }


}
