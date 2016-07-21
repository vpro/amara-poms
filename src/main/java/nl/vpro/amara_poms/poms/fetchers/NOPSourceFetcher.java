package nl.vpro.amara_poms.poms.fetchers;

import java.net.URI;

import nl.vpro.amara_poms.poms.SourceFetcher;
import nl.vpro.domain.media.Program;

/**
 * @author Michiel Meeuwissen
 * @since 1.3
 */
public class NOPSourceFetcher implements SourceFetcher {
    @Override
    public FetchResult fetch(Program program) {
        return FetchResult.succes(URI.create("mock:success"));
    }
}
