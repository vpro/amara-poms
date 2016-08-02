package nl.vpro.amara_poms.poms.fetchers;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;

import com.google.common.io.Files;

/**
 * @author Michiel Meeuwissen
 * @since 1.3
 */
@Slf4j
public abstract class AbstractFileCopyFetcher extends AbstractFileFetcher {

    protected AbstractFileCopyFetcher(File destDirectory, String destExtension, String downloadUrlBase) {
        super(destDirectory, destExtension, downloadUrlBase);
    }


    @Override
    protected File produce(File file, String mid) throws IOException {
        String destFileName = mid + "." + destExtension;
        File destFile = new File(destDirectory, destFileName);
        Files.copy(file, destFile);
        return  destFile;
    }
}
