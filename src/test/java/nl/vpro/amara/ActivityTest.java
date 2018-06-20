package nl.vpro.amara;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.junit.Test;

import nl.vpro.amara.domain.Activity;
import nl.vpro.amara_poms.Config;

/**
 * @author joost
 */
@Slf4j
public class ActivityTest {


    @Test
    public void get() {
        Config.init();

        Activity amaraActivity = Config.getAmaraClient().activity().get("5036197");

        log.info(amaraActivity.toString());
        if (amaraActivity.getCreated() != null) {
            log.info((amaraActivity.getCreated()).toString());
        }
    }

    @Test
    public void getList() {
        Config.init();

        List<Activity> amaraActivities = Config.getAmaraClient().activity().list().getActivities();

        log.info("Count:" + amaraActivities.size());
        if (amaraActivities.size() > 0) {
            log.info(amaraActivities.get(0).toString());
        }
        log.info(amaraActivities.toString());
    }

    @Test
    public void getListForType() {
        Config.init();

        long afterTimestampInSeconds = Config.getRequiredConfigAsLong("amara.task.fetchlastperiod.seconds");

        long now = System.currentTimeMillis() / 1000;

        List<Activity> amaraActivities = Config.getAmaraClient().activity().list(Activity.TYPE_APPROVE_VERSION, now - afterTimestampInSeconds).getActivities();

        log.info("Count:" + amaraActivities.size());
        if (amaraActivities.size() > 0) {
            log.info(amaraActivities.get(0).toString());
        }
        log.info(amaraActivities.toString());
    }


}
