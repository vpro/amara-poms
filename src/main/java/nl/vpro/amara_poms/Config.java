package nl.vpro.amara_poms;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.text.StrSubstitutor;

import nl.vpro.amara.Client;
import nl.vpro.amara_poms.database.Manager;
import nl.vpro.amara_poms.poms.ChainedFetcher;
import nl.vpro.amara_poms.poms.SourceFetcher;
import nl.vpro.domain.media.support.Images;
import nl.vpro.rs.media.MediaRestClient;

/**
 * @author joost
 */
@Slf4j
public class Config {

    private static final Map<String, String> PROPERTIES = new HashMap<>();

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
    private static MediaRestClient pomsClient;
    private static Manager dbManager;
    private static ChainedFetcher fetcher;

    private static File configFile = new File(System.getProperty("user.home") +
        File.separator + "conf" + File.separator +"amaraimport.properties");



    public static void init() {
        // load config
        InputStream inputStream;
        try {
            Properties properties = new Properties();
            properties.load(Config.class.getResourceAsStream("/amaraimport.properties"));
            //File configFile =
            if (configFile.canRead()) {
                inputStream = new FileInputStream(configFile);
                properties.load(inputStream);
                log.info(configFile + " loaded");
            } else {
                log.info("Could not read {}", configFile);
            }
            init((Map) properties);

        } catch (Exception e) {
            throw new Error("Error opening properties file: " + e.getMessage(), ERROR_APP_CONFIG_NOT_FOUND);
        }
    }

    public static void init(String[] argv) {
        if (argv.length > 0) {
            configFile = new File(argv[0]);
        }
        init();
    }

    public static void init(Map<String, String> props) {
        PROPERTIES.putAll(props);
        StrSubstitutor subst = new StrSubstitutor(PROPERTIES);
        for (Map.Entry<String, String> e : PROPERTIES.entrySet()) {
            e.setValue(subst.replace(e.getValue()));
        }
        Images.setImageHost(getRequiredConfig("poms.image_url"));
    }

    public static Client getAmaraClient() {
        if (amaraClient ==  null) {
            amaraClient = Client.builder()
                .url(getRequiredConfig("amara.api.url"))
                .user(getRequiredConfig("amara.api.username"))
                .apiKey(getRequiredConfig("amara.api.key"))
                .team(getRequiredConfig("amara.api.team"))
                .build();
            log.info("Created amara client {}", amaraClient);
        }
        return amaraClient;
    }




    public static MediaRestClient getPomsClient() {
        if (pomsClient == null) {
            // get config
            String username = getRequiredConfig("poms.username");
            String password = getRequiredConfig("poms.password");
            String url = getRequiredConfig("poms.url");
            String errors = getRequiredConfig("poms.errors");

            pomsClient = MediaRestClient.builder()
                .userName(username)
                .password(password)
                .errors(errors)
                .baseUrl(url)
                .waitForRetry(true)
                .trustAll(true) // can be removed soon
                .build()
            ;

            pomsClient.setThrottleRate(50);
        }

        return pomsClient;
    }

    public static Manager getDbManager() {
        if (dbManager == null) {
            dbManager = Manager.getInstance();
            dbManager.setFilenameTasks(Config.getRequiredConfig("db.filepath"));
            dbManager.readFile();
        }
        return dbManager;
    }

    public static ChainedFetcher getFetcher() {
        if (fetcher == null) {
            fetcher = new ChainedFetcher();
            for(String clazz : getRequiredConfigAsArray("poms.fetchers", "[\\s,]+")) {
                try {
                    Class<SourceFetcher> fetcherClass = (Class<SourceFetcher>) Class.forName(clazz);
                    fetcher.add(fetcherClass.newInstance());
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                    throw new IllegalStateException(e);
                }
            }


        }
        return fetcher;
    }

    /**
     * Get required config and exit if not found
     *
     */
    public static String getRequiredConfig(String propertyName) {
        String returnValue = PROPERTIES.get(propertyName);

        exitNotSet(propertyName, returnValue);

        return  returnValue;
    }

    /**
     * Get required config and exit if not found
     *
     */
    public static int getRequiredConfigAsInt(String propertyName) {
        String propertyValue = PROPERTIES.get(propertyName);

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
        String propertyValue = PROPERTIES.get(propertyName);

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
        return getRequiredConfigAsArray(propertyName, "\\s*,\\s*");
    }

    /**
     * Get required config as array and exit if not found
     *
     * @return array of values
     */
    public static String[] getRequiredConfigAsArray(String propertyName, String split) {
        String propertyValue = PROPERTIES.get(propertyName);

        exitNotSet(propertyName, propertyValue);

        return propertyValue.split(split);
    }


    /**
     * Get config and log error if not found
     */
    public static String getConfig(String propertyName) {
        String returnValue = PROPERTIES.get(propertyName);

        if (returnValue == null) {
            log.debug(propertyName + " not set in " + configFile);
        }

        return  returnValue;
    }


    private static void exitNotSet(String propertyName, String propertyValue) {
        if (propertyValue == null) {
            throw new Error(propertyName + " not set in " + configFile, ERROR_CONFIG_ERROR);
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
