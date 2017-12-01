package nl.vpro.amara_poms;

import java.io.IOException;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import nl.vpro.amara.domain.*;
import nl.vpro.amara_poms.database.task.DatabaseTask;
import nl.vpro.domain.subtitles.SubtitlesFormat;


public class PomsPublisherTest {

    @Before
    public void init() {

        Config.init();
        Config.getDbManager().clear();
    }


    @Test
    public void testAmaraToPomsSubtitles() throws IOException {

        Subtitles amaraSubtitles = new Subtitles("test subtitles", "vtt",
            "WEBVTT\n" +
                "\n" +
                "1\n" +
                "00:00:02.018 --> 00:00:05.007\n" +
                "888\n" +
                "\n" +
                "2\n" +
                "00:00:05.012 --> 00:00:07.018\n" +
                "TUNE VAN DWDD\n", "test description", "complete");

        PomsPublisher pomsPublisher = new PomsPublisher();

        Language language = new Language();
        language.setCode("en");
        amaraSubtitles.setLanguage(language);

        nl.vpro.domain.subtitles.Subtitles pomsSubtitles = pomsPublisher.amaraToPomsSubtitles(amaraSubtitles, "WO_VPRO_3244288");
        Assert.assertEquals(amaraSubtitles.getLanguage().getCode(), pomsSubtitles.getLanguage().toString());
        System.out.println(pomsSubtitles.getContent().toString());



    }



}
