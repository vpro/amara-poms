package nl.vpro.amara_poms.poms.fetchers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import nl.vpro.amara_poms.Config;
import nl.vpro.amara_poms.poms.SourceFetcher;
import nl.vpro.domain.media.AVFileFormat;
import nl.vpro.domain.media.Location;
import nl.vpro.domain.media.MediaBuilder;
import nl.vpro.domain.media.Program;
import nl.vpro.domain.media.support.OwnerType;

/**
 * @author Michiel Meeuwissen
 * @since 1.13
 */
public class HaspFetcherTest {

    @Before
    public void setup() {
        Map<String, String> props = new HashMap<>();
        props.put("videofile.dir", "/e/ap/video.dir");
        props.put("hasp.source.dir", "/tmp");
        props.put("download.url.base", "http://download.omroep.nl/vpro/netinnederland/");
        Config.init(props);
    }
    @Test
    public void fetch() throws Exception {



        HaspFetcher fetcher = new HaspFetcher();

        Location location = new Location("odi+http://odi.omroep.nl/video/h264_std/20070809_dieropvang01", OwnerType.BROADCASTER);
        location.setAvFileFormat(AVFileFormat.HASP);

        Program program = MediaBuilder.program().mid("WO_NTR_425372").locations(location).build();

        SourceFetcher.FetchResult result = fetcher.fetch(program);

        System.out.println(result.destination);

    }

    @Test
    public void testDamnJava() throws IOException {
        Files.find(Paths.get("/tmp"), 100,
            (p, a) -> Files.isDirectory(p)).forEach(System.out::println);
    }

}
