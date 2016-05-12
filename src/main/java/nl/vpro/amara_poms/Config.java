package nl.vpro.amara_poms;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.vpro.amara.Client;
import nl.vpro.rs.media.MediaRestClient;

/**
 * @author joost
 */
public class Config {

    private final static Logger LOG = LoggerFactory.getLogger(Config.class);
    private final static File[] FILES = new File[] { new File(System.getProperty("user.home") + File.separator + "conf" + File.separator + "amaraimport.properties")};
    private static final Properties PROPERTIES = new Properties();

    public static final int NO_ERROR = 0;
    public static final int ERROR_APP_CONFIG_NOT_FOUND = 1;
    public static final int ERROR_CONFIG_ERROR = 2;
    public static final int ERROR_INPUT_FILE_NOT_FOUND = 3;
    public static final int ERROR_COPY_INPUTFILE = 4;
    public static final int ERROR_CREATING_OUTPUTDIR = 5;
    public static final int ERROR_LOCKFILE_EXISTS = 6;

    public static final int ERROR_POM_SUBTITLES_MALFORMED_URL = 6;
    public static final int ERROR_POM_SUBTITLES_URL_OPEN = 7;
    public static final int ERROR_POM_SUBTITLES_RESPONSE = 8;
    public static final int ERROR_POM_SUBTITLES_NOT_FOUND = 9;
    public static final int ERROR_WRITING_SUBTITLES_TO_FILE = 10;

    public static final int ERROR_DB_NOT_READABLE = 11;

    private static Client amaraClient;

    public static void init() {
        // load config
        InputStream inputStream;
        try {

            for (File f : FILES) {
                if (f.canRead()) {
                    inputStream = new FileInputStream(f);
                    PROPERTIES.load(inputStream);
                    LOG.info(f + " loaded");
                } else {
                    LOG.info("Could not read {}", f);
                }
            }
        } catch (Exception e) {
            throw new Error("Error opening properties file: " + e.getMessage(), ERROR_APP_CONFIG_NOT_FOUND);
        }
    }

    public static Client getAmaraClient() {
        if (amaraClient ==  null) {
            amaraClient = new Client.Builder()
                .url(getRequiredConfig("amara.api.url"))
                .user(getRequiredConfig("amara.api.username"))
                .apiKey(getRequiredConfig("amara.api.key"))
                .team(getRequiredConfig("amara.api.team"))
                .build();
        }
        return amaraClient;
    }


    private static MediaRestClient pomsClient = null;

    public static MediaRestClient getPomsClient() {
        if (pomsClient == null) {
            pomsClient = new MediaRestClient();

            // get config
            String username = getRequiredConfig("poms.username");
            String password = getRequiredConfig("poms.password");
            String url = getRequiredConfig("poms.url");
            String errors = getRequiredConfig("poms.errors");

            // get client
            pomsClient.setTrustAll(true);
            pomsClient.setUserName(username);
            pomsClient.setPassword(password);
            pomsClient.setErrors(errors);
            pomsClient.setUrl(url);
            pomsClient.setThrottleRate(50);
            pomsClient.setWaitForRetry(true);
        }

        return pomsClient;
    }

    /**
     * Get required config and exit if not found
     *
     */
    public static String getRequiredConfig(String propertyName) {
        String returnValue = PROPERTIES.getProperty(propertyName);

        exitNotSet(propertyName, returnValue);

        return  returnValue;
    }

    /**
     * Get required config and exit if not found
     *
     */
    public static int getRequiredConfigAsInt(String propertyName) {
        String propertyValue = PROPERTIES.getProperty(propertyName);

        exitNotSet(propertyName, propertyValue);

        try {
            return Integer.parseInt(propertyValue);
        } catch (NumberFormatException e)  {
            throw new Error(propertyName + "-> not integer", ERROR_CONFIG_ERROR);
        }
    }

    /**
     * Get required config and exit if not found
     */
    public static long getRequiredConfigAsLong(String propertyName) {
        String propertyValue = PROPERTIES.getProperty(propertyName);

        exitNotSet(propertyName, propertyValue);

        try {
            return Long.parseLong(propertyValue);
        } catch (NumberFormatException e)  {
            throw new Error(propertyName + "-> not long", ERROR_CONFIG_ERROR);
        }
     }


    /**
     * Get required config as array and exit if not found
     *
     * @return array of values
     */
    public static String[] getRequiredConfigAsArray(String propertyName) {
        String propertyValue = PROPERTIES.getProperty(propertyName);

        exitNotSet(propertyName, propertyValue);

        return propertyValue.split(",");
    }


    /**
     * Get config and log error if not found
     */
    public static String getConfig(String propertyName) {
        String returnValue = PROPERTIES.getProperty(propertyName);

        if (returnValue == null) {
            LOG.debug(propertyName + " not set in " + Arrays.asList(FILES));
        }

        return  returnValue;
    }


    private static void exitNotSet(String propertyName, String propertyValue) {
        if (propertyValue == null) {
            throw new Error(propertyName + " not set in " + Arrays.asList(FILES), ERROR_CONFIG_ERROR);
        }
    }

    public static class Error extends RuntimeException {
        private final int errorCode;

        public Error(String message, int errorCode) {
            super(message);
            this.errorCode = errorCode;
        }

        public int getErrorCode() {
            return errorCode;
        }
    }

}
