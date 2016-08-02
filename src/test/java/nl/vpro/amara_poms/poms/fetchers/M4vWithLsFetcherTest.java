package nl.vpro.amara_poms.poms.fetchers;

import org.junit.Before;
import org.junit.Test;

import nl.vpro.amara_poms.Config;
import nl.vpro.domain.media.MediaBuilder;

/**
 * @author Michiel Meeuwissen
 * @since 1.12
 */
public class M4vWithLsFetcherTest {

    @Before
    public void init() {
        Config.init();
    }
    @Test
    public void fetch() throws Exception {
        M4vWithLsFetcher fetcher = new M4vWithLsFetcher();

        fetcher.fetch(MediaBuilder.program().mid("MID_1234").build());


    }

}
