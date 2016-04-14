package nl.vpro.amara_poms.poms;

import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;

import nl.vpro.amara_poms.Config;
import nl.vpro.rs.media.MediaRestClient;

/**
 * @author Michiel Meeuwissen
 * @since 1.0
 */
@Ignore("This is an integration test connection to an actual server")
public class ITest {
    @Test
    public void testPomsClipCreate() throws IOException {
        Config.init();
        MediaRestClient client = new MediaRestClient().configured();

        String result = PomsClip.create(client, "WO_VPRO_043085", "en");
        System.out.println(result);

    }
}
