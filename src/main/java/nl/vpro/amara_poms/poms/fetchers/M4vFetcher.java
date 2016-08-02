package nl.vpro.amara_poms.poms.fetchers;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;

import com.google.common.io.Files;

import nl.vpro.amara_poms.Config;
import nl.vpro.domain.media.Program;

/**
 * @author Michiel Meeuwissen
 * @since 1.3
 */
@Slf4j
@ToString
public class M4vFetcher extends AbstractFileCopyFetcher {

    File sourceDir = new File(Config.getRequiredConfig("h264.source.dir"));


    public M4vFetcher() {
        super(new File(Config.getRequiredConfig("h264.videofile.dir")), "mp4", Config.getRequiredConfig("h264.download.url.base"));
    }

    /**
     * Copy source video file to download.omroep.nl to make it accessable for Amara
     */
    @Override
    public FetchResult fetch(Program program) {
        String mid = program.getMid();
        File sourceDir = new File(Config.getRequiredConfig("h264.source.dir"));
        for (File f : Files.fileTreeTraverser().preOrderTraversal(sourceDir)) {
            if (f.isDirectory() && f.getName().equals(mid)) {
                for (File candidate : f.listFiles()) {
                    try {
                        if (candidate.canRead() && candidate.getName().startsWith("sb.") && candidate.getName().endsWith(".m4v")) {
                            return success(candidate, mid);
                        }
                    } catch (IOException e) {
                        log.error(e.getMessage(), e);
                    }
                }
            }
        }
        return FetchResult.notable();
    }
}
