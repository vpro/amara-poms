package nl.vpro.amara_poms.poms.fetchers;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import nl.vpro.amara_poms.Config;
import nl.vpro.amara_poms.poms.SourceFetcher;
import nl.vpro.domain.media.AVFileFormat;
import nl.vpro.domain.media.Location;
import nl.vpro.domain.media.Program;

/**
 * WO_NTR_425372
 * @author Michiel Meeuwissen
 * @since 1.3
 */
@Slf4j
public class HaspFetcher implements SourceFetcher {
    @Override
    public FetchResult fetch(Program program) {
        String mid = program.getMid();
        Path dir = Paths.get(Config.getRequiredConfig("videofile.dir"), "hasp");
        for (Location location : program.getLocations()) {
            if (location.getAvFileFormat() == AVFileFormat.HASP) {
                URI url = URI.create(location.getProgramUrl());
                String[] path = url.getPath().split("/");
                String fileName = path[path.length - 1];
                try {
                    Files.find(Paths.get(Config.getRequiredConfig("hasp.source.dir")), 100, (p, a) -> Files.isDirectory(p))
                        .forEach(p -> {
                            if (p.getFileName().toString().equals(mid)) {
                                String[] fileNames = {mid + ".mp4", fileName + "_1092.ismv"};
                                for (String name : fileNames) {
                                    Path file = p.resolve(name);
                                    if (Files.isRegularFile(file)) {
                                        try {
                                            Files.copy(file, dir.resolve(mid + ".mp4"));
                                            //return false;
                                        } catch (IOException e) {
                                            log.error(e.getMessage(), e);
                                        }
                                    }
                                }
                            }
                            // continue
                            //return true;
                        });
                    return FetchResult.succes(URI.create(Config.getRequiredConfig("download.url.base") + "hasp/" + fileName));
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                    return FetchResult.error();
                }
            }
        }
        return FetchResult.notable();

    }
}
