package nl.vpro.amara_poms.poms.fetchers;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import nl.vpro.amara_poms.Config;
import nl.vpro.amara_poms.poms.SourceFetcher;
import nl.vpro.domain.media.Program;

/**
 * @author Michiel Meeuwissen
 * @since 1.3
 */
@Slf4j
@Deprecated
public class WithBashScriptM4vFetcher implements SourceFetcher {

    /**
     * Copy source video file to download.omroep.nl to make it accessable for Amara
     *
     * @return NO_ERROR if successfull, otherwise errorcode
     * TODO The fetch script contains:
     * <pre>
     * DIR=/e/download/pages/vpro/netinnederland/h264
     * for i in $*; do
     * cp /e/pa/ceres/active/internetvod/h264/*...
     * done
     * </pre>
     * It seems odd to require a system call for that!
     */
    @Override
    public FetchResult fetch(Program program) {
        String mid = program.getMid();
        ProcessBuilder pb = new ProcessBuilder(Config.getRequiredConfig("fetch.script"), mid);
        pb.directory(new File("."));
        try {
            pb.start();
        } catch (IOException e) {
            log.error(e.toString());
            return FetchResult.error();
        }

        log.info("Copied video file for mid " + mid);

        String basePath = Config.getRequiredConfig("h264.download.url.base");
        String extension = Config.getRequiredConfig("h264.download.url.ext");
        String externalUrl = basePath + mid + "." + extension;
        return FetchResult.succes(URI.create(externalUrl));
    }
}
