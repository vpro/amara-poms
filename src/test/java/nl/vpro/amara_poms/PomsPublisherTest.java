package nl.vpro.amara_poms;

import java.io.IOException;
import java.util.Locale;
import java.util.Optional;

import org.junit.jupiter.api.*;

import nl.vpro.amara.domain.*;
import nl.vpro.amara_poms.database.task.DatabaseTask;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class PomsPublisherTest {



    @BeforeEach
    public void init() {
        Config.init();
        Config.getDbManager().clear();
    }

    @Test
    public void testSubtitleConversion() throws IOException {
        PomsPublisher pomsPublisher = new PomsPublisher();
        Subtitles amaraSubtitles = new Subtitles("NOS Journaal Engels", "vtt", "Bla bla bla bla", "translation NOS Journaal", "no action");
        Language language = new Language();
        language.setLtr("english");
        language.setCode("en");
        amaraSubtitles.setLanguage(language);
        nl.vpro.domain.subtitles.Subtitles pomsSubtitles = pomsPublisher.amaraToPomsSubtitles(amaraSubtitles, "mid");
        Assertions.assertEquals(pomsSubtitles.getLanguage(), Locale.ENGLISH);

    }

    @Test
    public void testIdentifyInDatabse() throws IOException {
        PomsPublisher pomsPublisher = new PomsPublisher();
        DatabaseTask task = new DatabaseTask("yiAGdgwxlD3J", "nl", "6", "POW_02988308");
        Config.getDbManager().clear();
        Config.getDbManager().addOrUpdateTask(task);
        Task amaraTask = new Task("yiAGdgwxlD3J", "nl", TaskType.Translate, new User());
        pomsPublisher.identifyTaskinDatabase(amaraTask);
        Assertions.assertEquals(1, Config.getDbManager().getTasks().size());
        Assertions.assertEquals("yiAGdgwxlD3J", Config.getDbManager().getTasks().get(0).getVideoId());
    }

    @Test
    public void testGetPomsSourceMid() {
        PomsPublisher pomsPublisher = new PomsPublisher();
        Task amaraTask = new Task("mRHfHebcJlnp", "nl", TaskType.Translate, new User());
        Assertions.assertEquals(Optional.of("POW_02990422"), pomsPublisher.getPomsSourceMid(amaraTask));
    }


    @Test
    public void testGetSubtitles() {
        PomsPublisher pomsPublisher = new PomsPublisher();
        Task amaraTask = new Task();
        amaraTask.setVideo_id("9mAKaADTV0XX");
        amaraTask.setLanguage("nl");
        Assertions.assertNotNull(pomsPublisher.getSubtitles(amaraTask));

    }

    @Test
    public void testHasValidLanguageCode() {
        PomsPublisher pomsPublisher = new PomsPublisher();
        Video video1 = new Video("url", "ar", "title", "description", "team", new VideoMetadata());
        Video video2 = new Video("url", "xx", "title", "description", "team", new VideoMetadata());
        Task amaraTask1 = new Task(video1.getId(), video1.getPrimary_audio_language_code(), TaskType.Translate, new User());
        Task amaraTask2 = new Task(video2.getId(), video2.getPrimary_audio_language_code(), TaskType.Translate, new User());
        assertTrue(pomsPublisher.hasValidLanguage(amaraTask1));
        assertFalse(pomsPublisher.hasValidLanguage(amaraTask2));
    }

    @Test
    public void testAmarataskIsValid() {
        PomsPublisher pomsPublisher = new PomsPublisher();
        Video video1 = new Video("url", "ar", "title", "description", "team", new VideoMetadata());
        Video video2 = new Video("url", "xx", "title", "description", "team", new VideoMetadata());
        Video video3 = new Video("url", "ar", "title", "description", "team", new VideoMetadata());
        Task amaraTask1 = new Task(video1.getId(), video1.getPrimary_audio_language_code(), TaskType.Translate, new User());
        Task amaraTask2 = new Task(video2.getId(), video2.getPrimary_audio_language_code(), TaskType.Translate, new User());
        Task amaraTask3 = new Task(video2.getId(), video3.getPrimary_audio_language_code(), TaskType.Translate, new User());
        amaraTask1.setApproved(Task.TASK_APPROVED);
        amaraTask2.setApproved(Task.TASK_APPROVED);
        amaraTask3.setApproved("");
        assertTrue(pomsPublisher.isValid(amaraTask1));
        assertFalse(pomsPublisher.isValid(amaraTask2));
        assertFalse(pomsPublisher.isValid(amaraTask3));
    }




}
