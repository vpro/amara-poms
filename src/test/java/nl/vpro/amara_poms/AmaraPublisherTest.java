package nl.vpro.amara_poms;

import org.junit.Before;
import org.junit.Test;

import nl.vpro.amara.domain.Subtitles;
import nl.vpro.amara.domain.Video;

/**
 * @author Michiel Meeuwissen
 * @since 1.3
 */
public class AmaraPublisherTest {

    @Before
    public void init() {
        Config.init();
    }

    @Test
    public void uploadSubtitles() throws Exception {

        Video video = Config.getAmaraClient().videos().get("yiAGdgwxlD3J");
        System.out.println(video.getTitle());
        Subtitles subtitles = Config.getAmaraClient().videos().getSubtitles("yiAGdgwxlD3J", "nl", "vtt");
        System.out.println(subtitles);

        Config.getAmaraClient().videos().post(subtitles, "yiAGdgwxlD3J", "nl");

    }

}
