package nl.vpro.amara_poms.poms.fetchers;

import lombok.ToString;

import java.net.URI;

import nl.vpro.amara_poms.poms.SourceFetcher;
import nl.vpro.domain.media.MediaObject;

/**
 * @author Michiel Meeuwissen
 * @since 1.3
 */
@ToString
public class NOPSourceFetcher implements SourceFetcher {
    @Override
    public FetchResult fetch(MediaObject program) {
        return FetchResult.succes(URI.create("mock:success"));
    }
}
