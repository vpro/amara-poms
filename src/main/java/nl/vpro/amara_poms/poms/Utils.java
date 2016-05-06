package nl.vpro.amara_poms.poms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.vpro.amara_poms.Config;
import nl.vpro.rs.media.MediaRestClient;


/**
 * @author joost
 */
public class Utils {

    private static final Logger LOG = LoggerFactory.getLogger(Utils.class);

    private static MediaRestClient client = null;

    public static MediaRestClient getClient() {
        if (client == null) {
            client = new MediaRestClient();

            // get config
            String username = Config.getRequiredConfig("poms.username");
            String password = Config.getRequiredConfig("poms.password");
            String url = Config.getRequiredConfig("poms.url");
            String errors = Config.getRequiredConfig("poms.errors");

            // get client
            client.setTrustAll(true);
            client.setUserName(username);
            client.setPassword(password);
            client.setErrors(errors);
            client.setUrl(url);
            client.setThrottleRate(50);
            client.setWaitForRetry(true);
        }

        return client;
    }

}
