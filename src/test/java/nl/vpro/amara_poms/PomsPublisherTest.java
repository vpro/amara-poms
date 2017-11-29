package nl.vpro.amara_poms;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import nl.vpro.amara.domain.*;
import nl.vpro.amara_poms.database.task.DatabaseTask;


public class PomsPublisherTest {

    @Before
    public void init() {

        Config.init();
        Config.getDbManager().clear();
    }

    @Test
    public void testSubtitleConversion() throws IOException {
        PomsPublisher pomsPublisher = new PomsPublisher();
        Subtitles amaraSubs = Config.getAmaraClient().videos().getSubtitles("yiAGdgwxlD3J", "nl", "vtt");
        nl.vpro.domain.subtitles.Subtitles pomsSubs = pomsPublisher.amaraToPomsSubtitles(amaraSubs, "POW_02988308");
        Assert.assertEquals(pomsSubs.getLanguage().toString(), amaraSubs.getLanguage().getCode());
    }

    @Test
    public void testIdentifyInDatabse() throws IOException {
        PomsPublisher pomsPublisher = new PomsPublisher();
        DatabaseTask task = new DatabaseTask("yiAGdgwxlD3J", "nl", "6", "POW_02988308");
        Config.getDbManager().clear();
        Config.getDbManager().addOrUpdateTask(task);
        Task amaraTask = new Task("yiAGdgwxlD3J", "nl", TaskType.Translate, new User());
        pomsPublisher.identifyTaskinDatabase(amaraTask);
        Assert.assertEquals(1, Config.getDbManager().getTasks().size());
    }

    @Test
    public void testGetPomsSourceMid() {
        PomsPublisher pomsPublisher = new PomsPublisher();
        Task amaraTask = new Task("yiAGdgwxlD3J", "nl", TaskType.Translate, new User());
        Assert.assertEquals("POW_02988308", pomsPublisher.getPomsSourceMid(amaraTask));
    }

    @Test
    public void testIsMid() {
        PomsPublisher pomsPublisher = new PomsPublisher();
        String mid1 = "VARA_101379305";
        String mid2 = "";
        String mid3 = null;
        Assert.assertEquals(true, pomsPublisher.isMid(mid1));
        Assert.assertEquals(false, pomsPublisher.isMid(mid2));
        Assert.assertEquals(false, pomsPublisher.isMid(mid3));
    }

    @Test
    public void testGetSubtitles() {
        PomsPublisher pomsPublisher = new PomsPublisher();
        Task amaraTask = new Task("yiAGdgwxlD3J", "nl", TaskType.Translate, new User());
        DatabaseTask databaseTask = new DatabaseTask(amaraTask.getVideo_id(), amaraTask.getLanguage(), "3");
        Config.getDbManager().addOrUpdateTask(databaseTask);
        Assert.assertNotNull(pomsPublisher.getSubtitles(amaraTask));

    }

    @Test
    public void testHasValidLanguageCode() {
        PomsPublisher pomsPublisher = new PomsPublisher();
        Video video1 = new Video("url", "ar", "title", "description", "team", new VideoMetadata());
        Video video2 = new Video("url", "xx", "title", "description", "team", new VideoMetadata());
        Task amaraTask1 = new Task(video1.getId(), video1.getPrimary_audio_language_code(), TaskType.Translate, new User());
        Task amaraTask2 = new Task(video2.getId(), video2.getPrimary_audio_language_code(), TaskType.Translate, new User());
        Assert.assertTrue(pomsPublisher.hasValidLanguage(amaraTask1));
        Assert.assertFalse(pomsPublisher.hasValidLanguage(amaraTask2));
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
        Assert.assertTrue(pomsPublisher.isValid(amaraTask1));
        Assert.assertFalse(pomsPublisher.isValid(amaraTask2));
        Assert.assertFalse(pomsPublisher.isValid(amaraTask3));
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
