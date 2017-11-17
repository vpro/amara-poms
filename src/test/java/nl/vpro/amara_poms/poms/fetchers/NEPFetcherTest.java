package nl.vpro.amara_poms.poms.fetchers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import nl.vpro.amara_poms.Config;
import nl.vpro.amara_poms.poms.SourceFetcher;
import nl.vpro.domain.media.Location;
import nl.vpro.domain.media.MediaBuilder;
import nl.vpro.domain.media.Platform;
import nl.vpro.domain.media.Program;
import nl.vpro.domain.media.support.OwnerType;

public class NEPFetcherTest {


    @Before
    public void init() {
        Map<String, String> config = new HashMap<>();
        config.put("nep.source.dir", "/tmp");
        Config.init();
        Config.init(config);
    }

    @Test
    public void testFetch() throws Exception {

        NEPFetcher fetcher = new NEPFetcher();
        URI dest = new URI (Config.getRequiredConfig("nep.videofile.dir"));

        Location location = new Location("npo://internetvod.omroep.nl/POW_03372983", OwnerType.AUTHORITY, Platform.INTERNETVOD);

        Program program = MediaBuilder.program().mid("POW_03372983").locations(location).duration(Duration.ofHours(1)).build();

        SourceFetcher.FetchResult result = fetcher.fetch(program);

        Assert.assertEquals(result.toString(), SourceFetcher.FetchResult.succes(dest).toString());

    }




}
