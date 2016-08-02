package nl.vpro.amara_poms.poms.fetchers;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.io.*;

import nl.vpro.amara_poms.Config;
import nl.vpro.domain.media.Program;
import nl.vpro.util.CommandExecutor;
import nl.vpro.util.CommandExecutorImpl;

/**
 * @author Michiel Meeuwissen
 * @since 1.3
 */
@Slf4j
@ToString
public class M4vWithLsFetcher extends AbstractFileCopyFetcher {


    CommandExecutor BASH = new CommandExecutorImpl(Config.getRequiredConfig("bash"));

    File sourceDir = new File(Config.getRequiredConfig("h264.source.dir"));
    int depth = (int) Config.getRequiredConfigAsLong("h264.source.dir.depth");


    public M4vWithLsFetcher() {
        super(new File(Config.getRequiredConfig("h264.videofile.dir")), "mp4", Config.getRequiredConfig("h264.download.url.base"));
    }

    /**
     * Copy source video file to download.omroep.nl to make it accessable for Amara
     */
    @Override
    public FetchResult fetch(Program program) {
        String mid = program.getMid();
        File sourceDir = new File(Config.getRequiredConfig("h264.source.dir"));
        log.info("Search files in {}", sourceDir);
        StringWriter files = new StringWriter();
        String stars = new String(new char[depth]).replaceAll("\0", "*/");
        BASH.execute(files, "-c", "ls -d -1 " + new File(sourceDir, stars + mid + "/*").toString());
        BufferedReader reader = new BufferedReader(new StringReader(files.toString()));
        String f;
        try {
            while ((f = reader.readLine()) != null) {
                File candidate = new File(f);
                try {
                    if (candidate.canRead() && candidate.getName().startsWith("sb.") && candidate.getName().endsWith(".m4v")) {
                        return success(candidate, mid);
                    }
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        log.info("No feasible file found in\n{}", files.toString());
        return FetchResult.notable();
    }
}
