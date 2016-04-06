package nl.vpro.amara_poms.poms;

import nl.vpro.amara_poms.Config;
import nl.vpro.rs.media.MediaRestClient;

import java.util.Properties;

import org.apache.commons.logging.Log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by joost on 05/04/16.
 */
public class Utils {

    final Logger logger = LoggerFactory.getLogger(Utils.class);

    public static MediaRestClient getClient() {
        MediaRestClient client = new MediaRestClient();

        // get config
        String username = Config.getRequiredConfig("poms.username");
        String password = Config.getRequiredConfig("poms.password");
        String url = Config.getRequiredConfig("poms.url");

        // get client
        client.setTrustAll(true);
        client.setUserName(username);
        client.setPassword(password);
        client.setUrl(url);
        client.setThrottleRate(50);
        client.setWaitForRetry(true);

        return client;
    }




}
