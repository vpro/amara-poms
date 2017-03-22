package nl.vpro.amara_poms.poms;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import nl.vpro.domain.media.MediaObject;

/**
 * @author Michiel Meeuwissen
 * @since 1.3
 */
@Slf4j
public class ChainedFetcher implements SourceFetcher, Iterable<SourceFetcher> {

    List<SourceFetcher> fetchers = new ArrayList<>();
    @Override
    public FetchResult fetch(MediaObject program) {
        for (SourceFetcher sf : this) {
            FetchResult result = sf.fetch(program);
            if (result.status == Status.SUCCESS) {
                log.info("Matched {}", sf);
                return result;
            }
        }
        return FetchResult.notable();

    }

    public void add(SourceFetcher fs) {
        log.info("Adding {}", fs);
        fetchers.add(fs);
    }

    @Override
    public Iterator<SourceFetcher> iterator() {
        return fetchers.iterator();

    }
}
