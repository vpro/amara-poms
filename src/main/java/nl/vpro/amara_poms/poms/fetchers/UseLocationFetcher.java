package nl.vpro.amara_poms.poms.fetchers;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import nl.vpro.amara_poms.Config;
import nl.vpro.amara_poms.poms.SourceFetcher;
import nl.vpro.domain.media.AVFileFormat;
import nl.vpro.domain.media.Location;
import nl.vpro.domain.media.Program;

/**
 * @author Michiel Meeuwissen
 * @since 1.3
 */
@Slf4j
@ToString
public class UseLocationFetcher implements SourceFetcher {
    
    private final Set<String>       SCHEMES = new HashSet<>(
        Arrays.asList(Config.getRequiredConfigAsArray("use.location.schemes")));
    private final Set<AVFileFormat> FORMATS = new HashSet<>(
        Arrays.stream(Config.getRequiredConfigAsArray("use.location.formats")).map(AVFileFormat::valueOf).collect(Collectors.toList())
    );
    
    @Override
    public FetchResult fetch(Program program) {
        //File dest = new File(Config.getRequiredConfig("videofile.dir"), program.getMid() + ".mp4");
        for (Location location : program.getLocations()) {
            if (location.isPublishable() && FORMATS.contains(location.getAvFileFormat())) {
                URI uri = URI.create(location.getProgramUrl());
                if (SCHEMES.contains(uri.getScheme())) {
                    //IOUtils.copy(url.openStream(), new FileOutputStream(dest));
                    return FetchResult.succes(uri);
                }
            }
        }
        return FetchResult.notable();

    }
}
