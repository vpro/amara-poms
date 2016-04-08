package nl.vpro.amara_poms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by joost on 05/04/16.
 */
public class Config {

    final static Logger logger = LoggerFactory.getLogger(Config.class);
    private static Properties properties;

    static final String fileName = "app.config";

    public static final int NO_ERROR = 0;
    public static final int ERROR_APP_CONFIG_NOT_FOUND = 1;
    public static final int ERROR_CONFIG_ERROR = 2;
    public static final int ERROR_INPUT_FILE_NOT_FOUND = 3;
    public static final int ERROR_COPY_INPUTFILE = 4;
    public static final int ERROR_CREATING_OUTPUTDIR = 5;

    public static final int ERROR_POM_SUBTITLES_MALFORMED_URL = 6;
    public static final int ERROR_POM_SUBTITLES_URL_OPEN = 7;
    public static final int ERROR_POM_SUBTITLES_RESPONSE = 8;
    public static final int ERROR_POM_SUBTITLES_NOT_FOUND = 9;

    public static void init() {
        // load config
        properties = new Properties();
        InputStream inputStream;
        try {
            inputStream = new FileInputStream(fileName);
            properties.load(inputStream);
            logger.info(fileName + " loaded");
        } catch (Exception e) {
            logger.error("Error opening properties file: " + e.getMessage());
            System.exit(ERROR_APP_CONFIG_NOT_FOUND);
        }
    }

    /**
     * Get required config and exit if not found
     *
     * @param propertyName
     * @return
     */
    public static String getRequiredConfig(String propertyName) {
        String returnValue = properties.getProperty(propertyName);

        if (returnValue == null) {
            logger.error(propertyName + " not set in app.confg");
            System.exit((ERROR_CONFIG_ERROR));
        }

        return  returnValue;
    }

    /**
     * Get required config as array and exit if not found
     *
     * @param propertyName
     * @return array of values
     */
    public static String[] getRequiredConfigAsArray(String propertyName) {
        String propertyValue = properties.getProperty(propertyName);
        String returnValue[] = {};

        if (propertyValue == null) {
            logger.error(propertyName + " not set in app.confg");
            System.exit((ERROR_CONFIG_ERROR));
        } else {
            returnValue = propertyValue.split(",");
        }

        return  returnValue;
    }


    /**
     * Get config and log error if not found
     *
     * @param propertyName
     * @return
     */
    public static String getConfig(String propertyName) {
        String returnValue = properties.getProperty(propertyName);

        if (returnValue == null) {
            logger.error("poms.username not set in app.confg");
        }

        return  returnValue;
    }


}
