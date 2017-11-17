package nl.vpro.amara_poms.poms.fetchers;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import nl.vpro.amara_poms.poms.SourceFetcher;

/**
 * @author Michiel Meeuwissen
 * @since 1.3
 */
@Slf4j
@ToString
public abstract class AbstractFileFetcher implements  SourceFetcher {

    final File destDirectory;
    final String destExtension;
    final String downloadUrlBase;

    protected AbstractFileFetcher(File destDirectory, String destExtension, String downloadUrlBase) {
        this.destDirectory = destDirectory;
        this.destExtension = destExtension;
        this.downloadUrlBase = downloadUrlBase;
        if (destDirectory.mkdirs()) {
            log.info("Made", destDirectory);
        }
    }


    protected SourceFetcher.FetchResult success(File file, String mid) throws IOException {
        File destFile = produce(file, mid);
        return SourceFetcher.FetchResult.succes(URI.create(downloadUrlBase + "/" + destFile.getName()).normalize());
    }

    /**
     * Destination file name
     * @param file
     */
    protected abstract File produce(File file, String mid) throws IOException;

}
