package nl.vpro.amara_poms;

import java.io.IOException;
import java.time.Duration;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import nl.vpro.amara.domain.Subtitles;
import nl.vpro.amara.domain.Video;
import nl.vpro.amara_poms.poms.PomsBroadcast;
import nl.vpro.amara_poms.poms.SourceFetcher;
import nl.vpro.amara_poms.poms.fetchers.NEPFetcher;
import nl.vpro.domain.media.Location;
import nl.vpro.domain.media.MediaBuilder;
import nl.vpro.domain.media.Platform;
import nl.vpro.domain.media.Program;
import nl.vpro.domain.media.support.OwnerType;

import static org.mockito.Mockito.mock;

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
        System.out.println(Config.getAmaraClient().videos().getActions("yiAGdgwxlD3J", "nl"));

        SourceFetcher fetcher = mock(SourceFetcher.class);
        AmaraPublisher amaraPublisher = new AmaraPublisher(fetcher);
        amaraPublisher.uploadSubtitles(video, new PomsBroadcast("POW_02988308", null));
    }



}
