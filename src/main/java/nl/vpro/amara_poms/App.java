package nl.vpro.amara_poms;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *  Main app
 */
public class App  {
    
    
    public static void main( String[] args ) throws IOException {
        final Logger logger = LoggerFactory.getLogger(App.class);
        logger.info("Started...");

        Config.init();

        // check lock file
        Path path = Paths.get(Config.getRequiredConfig("process.lock.filepath"));
        try {
            if (Files.exists(path)) {                 
                logger.error("Another AmaraPomsPublisher process is running -> will quit");
                System.exit(Config.ERROR_LOCKFILE_EXISTS);
            } else {
                Files.createFile(path);
                logger.info("Wrote lock file {}", path);
            }

            new AmaraPublisher().processPomsCollection();
            new PomsPublisher().processAmaraTasks();

        } finally {
            logger.debug("removing lockfile {}", path);
            Files.delete(path);
        }

        System.exit(0);
    }
}
