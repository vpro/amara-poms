package nl.vpro.amara_poms.poms.fetchers;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import com.google.common.io.Files;

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
        for (Location location : program.getLocations()) {
            if (location.getAvFileFormat() == AVFileFormat.HASP) {
                File file = new File("/d/media3/ru/09/pa/ceres/mnt/active/webonly/adaptive/5/ntr/rest/2014/" + program.getMid() + "/20070809_dieropvang01_628.ismv");
                try {
                    String fileName = program.getMid() + ".ismv";
                    Files.copy(file, new File(Config.getRequiredConfig("videofile.dir"), fileName));

                    return FetchResult.succes(URI.create(Config.getRequiredConfig("download.url.base") + "/" + fileName));
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                    return FetchResult.error();
                }
            }
        }
        return FetchResult.notable();

    }
}
