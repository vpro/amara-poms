package nl.vpro.amara_poms.poms.fetchers;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import nl.vpro.amara_poms.Config;
import nl.vpro.domain.media.Program;
import nl.vpro.util.CommandExecutor;
import nl.vpro.util.CommandExecutorImpl;

/**
 * @author Michiel Meeuwissen
 * @since 1.3
 */
@Slf4j
@ToString(callSuper = true)
public class M4vWithLsFetcher extends AbstractFileCopyFetcher {

    CommandExecutor BASH = new CommandExecutorImpl(Config.getRequiredConfig("bash")); // bash does globbing very well!
    List<File> sourceDirs = Arrays.stream(Config.getRequiredConfigAsArray("h264.source.dir")).map(File::new).collect(Collectors.toList());
    int depth = (int) Config.getRequiredConfigAsLong("h264.source.dir.depth");

    public M4vWithLsFetcher() {
        super(new File(
            Config.getRequiredConfig("h264.videofile.dir")), "mp4",
            Config.getRequiredConfig("h264.download.url.base"));
    }

    /**
     * Copy source video file to download.omroep.nl to make it accessable for Amara
     */
    @Override
    public FetchResult fetch(Program program) {
        String mid = program.getMid();
        for (File sourceDir : sourceDirs) {
            log.info("Search files in {}", sourceDir);
            StringWriter files = new StringWriter();
            String stars = new String(new char[depth]).replaceAll("\0", "*/");
            File dirs = new File(sourceDir, stars + mid + "/*");
            BASH.execute(files, "-c", "ls -d -1 " + dirs.toString());
            if (StringUtils.isNotBlank(files.toString().trim())) {
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
                log.info("No feasible file found in\n{}", files);
            } else {
                log.info("No files found in {}", dirs);
            }
        }
        return FetchResult.notable();
    }
}
