package nl.vpro.amara_poms;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *  Main app
 */
public class App
{
    /**
     *
     * @param args
     */
    public static void main( String[] args )
    {
        Properties properties;

        // load config
        properties = new Properties();
        String fileName = "app.config";
        InputStream inputStream;
        try {
            inputStream = new FileInputStream(fileName);
            properties.load(inputStream);
        } catch (Exception e) {
            System.err.println("Error opening properties file: " + e.getMessage());
        }

        Logger logger = LoggerFactory.getLogger(App.class);
        logger.info("Started...");



    }
}
