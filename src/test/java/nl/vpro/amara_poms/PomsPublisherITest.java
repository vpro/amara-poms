package nl.vpro.amara_poms;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import nl.vpro.amara.domain.Language;
import nl.vpro.amara.domain.Task;
import nl.vpro.amara_poms.database.task.DatabaseTask;

public class PomsPublisherITest {

    static {

        Config.init();
        Config.getDbManager().clear();
    }

    PomsPublisher pomsPublisher = new PomsPublisher();

    nl.vpro.amara.domain.Subtitles amaraSubtitles = new nl.vpro.amara.domain.Subtitles("test subtitles", "vtt",
        "WEBVTT\n" +
            "\n" +
            "1\n" +
            "00:00:02.018 --> 00:00:05.007\n" +
            "888\n" +
            "\n" +
            "2\n" +
            "00:00:05.012 --> 00:00:07.018\n" +
            "TUNE VAN DWDD\n", "test description", "complete");

    {Language language = new Language();
        language.setCode("en");
        amaraSubtitles.setLanguage(language);
    }


    @Test
    public void createClipAndAddSubs() throws IOException, InterruptedException {
        Task amaraTask = new Task();
        amaraTask.setLanguage("en");

        DatabaseTask dbt = new DatabaseTask();
        pomsPublisher.createClipAndAddSubtitles("WO_NTR_428626", amaraTask, amaraSubtitles, dbt);

    }

    @Test
    public void addASubtitlesToPoms() throws IOException {

        pomsPublisher.addSubtitlesToPoms("WO_NTR_428626", amaraSubtitles);

    }
}
