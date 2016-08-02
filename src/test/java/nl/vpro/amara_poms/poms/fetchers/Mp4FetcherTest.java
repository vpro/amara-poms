package nl.vpro.amara_poms.poms.fetchers;

import org.junit.Test;

import nl.vpro.amara_poms.poms.SourceFetcher;
import nl.vpro.domain.media.AVFileFormat;
import nl.vpro.domain.media.Location;
import nl.vpro.domain.media.MediaTestDataBuilder;
import nl.vpro.domain.media.Program;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Michiel Meeuwissen
 * @since 1.3
 */
public class Mp4FetcherTest {
    @Test
    public void fetch() throws Exception {
        Program program = MediaTestDataBuilder.program().locations("odi+http://odi.omroep.nl/video/h264_sb/VPRO_1142704").build();
        program.getLocations().first().setAvFileFormat(AVFileFormat.MP4);

        SourceFetcher.FetchResult result = new Mp4Fetcher().fetch(program);
        assertThat(result.status).isEqualTo(SourceFetcher.Status.NOTABLE);
    }

}
