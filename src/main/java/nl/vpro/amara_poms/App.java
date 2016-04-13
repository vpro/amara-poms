package nl.vpro.amara_poms;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.james.mime4j.field.datetime.DateTime;
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

        // check logfile
        Path path = Paths.get(Config.getRequiredConfig("process.lock.filepath"));
        FileTime lockfileTime = null;

        if (Files.exists(path)) {
            try {
                lockfileTime = Files.getLastModifiedTime(path);
                long lockfileTimeInSeconds = lockfileTime.to(TimeUnit.SECONDS);
                logger.info("timestamp lockfile: " + lockfileTime.toString());

                int locktime = Config.getRequiredConfigAsInt("process.lock.expire.seconds");
                long nowInSeconds = System.currentTimeMillis() / 1000;
                long diffInSeconds = nowInSeconds - lockfileTimeInSeconds;
                // calculate difference in seconds
                if (diffInSeconds < locktime) {
                    logger.error("Another AmaraPomsPublisher process is running -> will quit");
                    System.exit(Config.ERROR_LOCKFILE_EXISTS);
                } else {
                    logger.warn("Another AmaraPomsPublisher process is running but expired -> continue");
                    Files.delete(path);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            Files.createFile(path);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // run Amara publisher
        AmaraPublisher amaraPublisher = new AmaraPublisher();
        amaraPublisher.processPomsCollection();

        // run Poms publisher
        PomsPublisher pomsPublisher = new PomsPublisher();
        pomsPublisher.processAmaraTasks();


        // remove lockfile
        try {
            Files.delete(path);
        } catch (Exception e) {
            logger.error("Error removing lockfiles");
        }

        System.exit(0);
    }
}
