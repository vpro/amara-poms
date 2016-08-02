package nl.vpro.amara_poms.poms.fetchers;

import lombok.ToString;

import java.net.URI;

import nl.vpro.amara_poms.poms.SourceFetcher;
import nl.vpro.domain.media.Program;

/**
 * @author Michiel Meeuwissen
 * @since 1.3
 */
@ToString
public class NOPSourceFetcher implements SourceFetcher {
    @Override
    public FetchResult fetch(Program program) {
        return FetchResult.succes(URI.create("mock:success"));
    }
}
