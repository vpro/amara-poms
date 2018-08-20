package nl.vpro.amara_poms.poms.fetchers;

import java.net.URI;

import org.junit.BeforeClass;
import org.junit.Test;

import nl.vpro.amara_poms.Config;
import nl.vpro.amara_poms.poms.SourceFetcher;
import nl.vpro.domain.media.AVFileFormat;
import nl.vpro.domain.media.MediaTestDataBuilder;
import nl.vpro.domain.media.Program;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Michiel Meeuwissen
 * @since 1.3
 */
public class UseLocationFetcherTest {

    @BeforeClass
    public static void init() {
        Config.init();
    }
    @Test
    public void fetchNotAble() throws Exception {
        Program program = MediaTestDataBuilder.program().locations("odi+http://odi.omroep.nl/video/h264_sb/VPRO_1142704").build();
        program.getLocations().first().setAvFileFormat(AVFileFormat.MP4);

        SourceFetcher.FetchResult result = new UseLocationFetcher().fetch(program);
        assertThat(result.status).isEqualTo(SourceFetcher.Status.NOTABLE);
    }

    @Test
    public void fetch() throws Exception {
        Program program = MediaTestDataBuilder.program().locations("http://download.omroep.nl/video/h264_sb/VPRO_1142704.mp4").build();
        program.getLocations().first().setAvFileFormat(AVFileFormat.MP4);

        SourceFetcher.FetchResult result = new UseLocationFetcher().fetch(program);
        assertThat(result.status).isEqualTo(SourceFetcher.Status.SUCCESS);
        assertThat(result.destination).isEqualTo(URI.create("http://download.omroep.nl/video/h264_sb/VPRO_1142704.mp4"));
    }

    @Test
    public void fetchWithSpace() {
        Program program = MediaTestDataBuilder.program().locations("http://video.omroep.nl/ntr/educatie/netinnederland/video/gmo-afl11 rockstar.mp4").build();
        program.getLocations().first().setAvFileFormat(AVFileFormat.MP4);

        SourceFetcher.FetchResult result = new UseLocationFetcher().fetch(program);
        assertThat(result.status).isEqualTo(SourceFetcher.Status.SUCCESS);
        assertThat(result.destination).isEqualTo(URI.create("http://video.omroep.nl/ntr/educatie/netinnederland/video/gmo-afl11%20rockstar.mp4"));
    }

}
