package nl.vpro.amara;

import java.util.List;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.vpro.amara.domain.Activity;
import nl.vpro.amara_poms.Config;

/**
 * @author joost
 */
public class ActivityTest {

    private final static Logger LOG = LoggerFactory.getLogger(ActivityTest.class);


    @Test
    public void get() throws Exception {
        Config.init();

        Activity amaraActivity = Config.getAmaraClient().getActivity("5036197");

        LOG.info(amaraActivity.toString());
        if (amaraActivity.getCreated() != null) {
            LOG.info((amaraActivity.getCreated()).toString());
        }
    }

    @Test
    public void getList() throws Exception {
        Config.init();

        List<Activity> amaraActivities = Config.getAmaraClient().getActivities().getActivities();

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

        List<Activity> amaraActivities = Config.getAmaraClient().getActivities(Activity.TYPE_APPROVE_VERSION, now - afterTimestampInSeconds).getActivities();

        LOG.info("Count:" + amaraActivities.size());
        if (amaraActivities.size() > 0) {
            LOG.info(amaraActivities.get(0).toString());
        }
        LOG.info(amaraActivities.toString());
    }


}
