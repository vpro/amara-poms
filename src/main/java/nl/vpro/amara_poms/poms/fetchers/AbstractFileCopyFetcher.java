package nl.vpro.amara_poms.poms.fetchers;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import com.google.common.io.Files;

import nl.vpro.amara_poms.poms.SourceFetcher;

/**
 * @author Michiel Meeuwissen
 * @since 1.23
 */
@Slf4j
public abstract class AbstractFileCopyFetcher implements  SourceFetcher {

    final File destDirectory;
    final String destExtension;
    final String downloadUrlBase;

    protected AbstractFileCopyFetcher(File destDirectory, String destExtension, String downloadUrlBase) {
        this.destDirectory = destDirectory;
        this.destExtension = destExtension;
        this.downloadUrlBase = downloadUrlBase;
        if (destDirectory.mkdirs()) {
            log.info("Made", destDirectory);
        }
    }


    protected SourceFetcher.FetchResult success(File file, String mid) throws IOException {
        String destFileName = mid + "." + destExtension;
        Files.copy(file, new File(destDirectory, destFileName));
        return SourceFetcher.FetchResult.succes(URI.create(downloadUrlBase + "/" + destFileName).normalize());
    }
}
