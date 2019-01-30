package nl.vpro.amara_poms;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import nl.vpro.amara.domain.Language;
import nl.vpro.amara.domain.Task;
import nl.vpro.amara.domain.Video;
import nl.vpro.amara_poms.database.task.DatabaseTask;

@Slf4j
public class PomsPublisherITest {

    static {
        try {
            new File("/tmp/amara.db").createNewFile();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
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
            "SUBTITLES FOR HET GEHEIM VAN LUBBERS\n", "test description", "complete");

    {Language language = new Language();
        language.setCode("en");
        amaraSubtitles.setLanguage(language);
    }


    @Test
    public void createClipAndAddTranslation() throws IOException, InterruptedException {
        Task amaraTask = new Task();
        amaraTask.setLanguage("en");
        DatabaseTask dbt = new DatabaseTask();
        pomsPublisher.createClipAndAddSubtitles("VPWON_1269516", amaraTask, amaraSubtitles, dbt);
    }

    @Test
    public void addSubtitlesToPoms() throws IOException {
        pomsPublisher.addSubtitlesToPoms("VPWON_1269516", amaraSubtitles);
    }

    @Test
    public void getPomsSourceMid() {
    }

    @Test
    public void testVideo() {
        final Video amaraVideo = Config.getAmaraClient().videos().get("99gUNxCv8nkr");
        log.info("{}", amaraVideo);


    }
}
