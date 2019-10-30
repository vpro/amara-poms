package nl.vpro.amara_poms.database;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.vpro.amara_poms.Config;
import nl.vpro.amara_poms.database.task.DatabaseTask;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author joost
 */
public class DatabaseManagerTest {

    final static private Logger LOG = LoggerFactory.getLogger(DatabaseManagerTest.class);
    final static private String DB_TEST_FILENAME_TASKS = "./testdb-tasks.csv";


    DatabaseTask task1;
    String testVideoId;
    String testVideoLanguage;
    String testPomsMid;
    int countBefore;

    /**
     * setup method for writing
     */
    public void setupWriting() {

        Config.init();

        Manager dbManager = Manager.getInstance();
        dbManager.setFilenameTasks(DB_TEST_FILENAME_TASKS);
        dbManager.clear();

        // first test writing
        testVideoId = "testid";
        testVideoLanguage = "nl";
        testPomsMid = "VPWON_1250959";
        task1 = new DatabaseTask(testVideoId, testVideoLanguage, DatabaseTask.STATUS_UPLOADED_VIDEO_TO_AMARA);
        task1.setPomsSourceMid(testPomsMid);
        dbManager.addOrUpdateTask(task1);
        DatabaseTask task2 = new DatabaseTask("testid2", "nl", DatabaseTask.STATUS_UPLOADED_VIDEO_TO_AMARA);
        dbManager.addOrUpdateTask(task2);
        DatabaseTask task3 = new DatabaseTask(testVideoId, "en", DatabaseTask.STATUS_UPLOADED_VIDEO_TO_AMARA);
        dbManager.addOrUpdateTask(task3);

        countBefore = dbManager.getTasks().size();
        assertEquals(3, countBefore);

        dbManager.writeFile();

        assertEquals(countBefore, dbManager.getTasks().size());
    }

    /**
     * test reading to db
     */
    @Test
    public void testReading()
    {
        Manager dbManager = Manager.getInstance();
        dbManager.setFilenameTasks(DB_TEST_FILENAME_TASKS);

        setupWriting();

        // than test reading
        dbManager.readFile();

        DatabaseTask task = dbManager.findTask(testVideoId, testVideoLanguage);
        assertEquals(countBefore, dbManager.getTasks().size());

        for (DatabaseTask task3 : dbManager) {
            LOG.info(task3.toString());
        }

        Path pathTasksdb = Paths.get(DB_TEST_FILENAME_TASKS);
        try {
            Files.delete(pathTasksdb);
        } catch (IOException e) {
            e.printStackTrace();
            assert(true);
        }

    }


    /**
     * test reading to db
     */
    @Test
    public void testSearchTask()
    {
        Manager dbManager = Manager.getInstance();
        dbManager.setFilenameTasks(DB_TEST_FILENAME_TASKS);

        setupWriting();
        dbManager.readFile();

        // test searching - found
        DatabaseTask task = dbManager.findTask(testVideoId, testVideoLanguage);
        assertNotNull(task);
        assertEquals(testVideoId, task.getVideoId());

        // test searching - not found
        DatabaseTask task2 = dbManager.findTask("897yuhjklj", testVideoLanguage);
        assertNull(task2);

        // test search - pomsmid - found
        DatabaseTask task3 = dbManager.findTaskByPomsSourceId(testPomsMid);
        assertNotNull(task3);
        assertEquals(testPomsMid, task.getPomsSourceMid());

        // test search - pomsmid - not found
        DatabaseTask task4 = dbManager.findTaskByPomsSourceId("dsfgdfg");
        assertNull(task4);


        Path pathTasksdb = Paths.get(DB_TEST_FILENAME_TASKS);
        try {
            Files.delete(pathTasksdb);
        } catch (IOException e) {
            e.printStackTrace();
            assert(true);
        }

    }

    /**
     * test add multiple to db
     */
    @Test
    public void testAddMultiple()
    {
        Manager dbManager = Manager.getInstance();
        dbManager.setFilenameTasks(DB_TEST_FILENAME_TASKS);

        setupWriting();
        dbManager.readFile();

        String videoTestId = "%^&GHJK";
        DatabaseTask task2 = new DatabaseTask(videoTestId, "nl", DatabaseTask.STATUS_UPLOADED_VIDEO_TO_AMARA);
        dbManager.addOrUpdateTask(task2);
        dbManager.addOrUpdateTask(task2);

        // test searching
        DatabaseTask task = dbManager.findTask(testVideoId, testVideoLanguage);

        assertNotNull(task);
        assertEquals(testVideoId, task.getVideoId());

        Path pathTasksdb = Paths.get(DB_TEST_FILENAME_TASKS);
        try {
            Files.delete(pathTasksdb);
        } catch (IOException e) {
            e.printStackTrace();
            assert(true);
        }

    }


    /**
     * test iterator
     */
    @Test
    public void testIterator()
    {
        // setup
        Manager dbManager = Manager.getInstance();
        dbManager.setFilenameTasks(DB_TEST_FILENAME_TASKS);
        setupWriting();
        dbManager.readFile();

        // actual test
        for (DatabaseTask task3 : dbManager) {
            LOG.info(task3.toString());
        }

        Path pathTasksdb = Paths.get(DB_TEST_FILENAME_TASKS);
        try {
            Files.delete(pathTasksdb);
        } catch (IOException e) {
            e.printStackTrace();
            assert(true);
        }
    }

    @Test
    public void gt()
    {
        DatabaseTask task = new DatabaseTask();

        assertTrue(task.isNewer(null));
        assertTrue(task.isNewer(""));
        assertTrue(task.isNewer("0"));
        assertTrue(task.isNewer("1"));
        assertTrue(task.isNewer("1123"));

        task.setSubtitlesVersionNo("1");
        assertFalse(task.isNewer("0"));
        assertFalse(task.isNewer("1"));
        assertTrue(task.isNewer("1123"));
        assertTrue(task.isNewer("2"));

    }

}
