package nl.vpro.amara_poms.poms;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.IOUtils;

import nl.vpro.amara_poms.Config;
import nl.vpro.domain.media.AVFileFormat;
import nl.vpro.domain.media.Location;
import nl.vpro.domain.media.Program;

/**
 * @author Michiel Meeuwissen
 * @since 1.3
 */
@Slf4j
public class Mp4Fetcher implements SourceFetcher {

    @Override
    public FetchResult fetch(Program program) {
        File dest = new File(Config.getRequiredConfig("videofile.dir"), program.getMid() + ".mp4");
        for (Location location : program.getLocations()) {
            if (location.isPublishable() && location.getAvFileFormat() == AVFileFormat.MP4) {
                try {
                    URL url = new URL(location.getProgramUrl());
                    IOUtils.copy(url.openStream(), new FileOutputStream(dest));
                    return FetchResult.succes(dest);
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
        return FetchResult.notable();

    }
}
