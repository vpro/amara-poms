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
        Logger logger = LoggerFactory.getLogger(App.class);
        logger.info("Started...");

        Config.init();

        // start amara publisher
        AmaraPublisher amaraPublisher = new AmaraPublisher();
        amaraPublisher.processPomsCollection();
    }
}
