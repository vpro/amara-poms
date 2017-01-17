package nl.vpro.amara_poms;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;

import nl.vpro.domain.classification.ClassificationServiceLocator;
import nl.vpro.domain.media.MediaClassificationService;
import nl.vpro.util.CommandExecutorImpl;


/**
 *  Main app
 */
@Slf4j
public class App  {

    public static void main(String[] args) throws IOException {

        log.info("Started...");
        ClassificationServiceLocator.setInstance(new MediaClassificationService());

        Config.init(args);

        Path lockFile = Paths.get(Config.getRequiredConfig("process.lock.filepath"));
        checkLockFile(lockFile);

        int exitCode;
        try {
            Files.createFile(lockFile);
            log.info("Wrote lock file {}", lockFile);
            log.info("=AMARA PUBLISHER (creating new tasks)======================================");
            new AmaraPublisher(Config.getFetcher()).processPomsCollection();
            log.info("=POMS PUBLISHER (handling finished tasks)==================================");
            new PomsPublisher().processAmaraTasks();
            exitCode = 0;
        } catch (Config.Error e) {
            log.error(e.getMessage(), e);
            exitCode = e.getErrorCode();
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
            exitCode = 1;
        } finally {
            log.info("removing lockfile {}", lockFile);
            Files.delete(lockFile);
            Config.getPomsClient().shutdown();
            log.info("=ready=================================");
            log.info("=======================================");
        }
        System.exit(exitCode);


    }

    private static void checkLockFile(Path lockFile) throws IOException {
        if (Files.exists(lockFile)) {
            Instant lastModified = Files.getLastModifiedTime(lockFile).toInstant();
            if (Instant.now().minus(Duration.ofHours(4)).isAfter(lastModified)) {
                CommandExecutorImpl env = new CommandExecutorImpl("/usr/bin/env");
                long running = env
                    .lines("ps", "u")
                    .filter(line -> line.contains("amara_poms_publisher"))
                    .count();
                if (running == 0) {
                    log.warn("Lock file {} still exists (since {}), but no process found. Will proceed anyway", lockFile, lastModified);
                    Files.delete(lockFile);
                }

            } else {
                log.error("Another AmaraPomsPublisher process is running ({} exists since {}) -> will quit", lockFile, lastModified);
                System.exit(Config.ERROR_LOCKFILE_EXISTS);
            }
        }
    }
}
