package nl.vpro.amara_poms.database;


import nl.vpro.amara_poms.Config;

import nl.vpro.amara_poms.database.activity.Activity;
import nl.vpro.amara_poms.database.task.Task;
import org.junit.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

import static org.junit.Assert.*;

/**
 * Created by joost on 07/04/16.
 */
public class databaseManagerTest {

    final static Logger logger = LoggerFactory.getLogger(databaseManagerTest.class);
    final static String dbTestFilenameTasks = "./testdb-tasks.csv";
    final static String dbTestFilenameActivities = "./testdb-activities.csv";


    Task task1;
    String testVideoId;
    String testVideoLanguage;
    int countBefore;
    String testActivityId;

    Activity activity;

    /**
     * setup method for writing
     */
    public void setupWriting() {

        Config.init();

        Manager dbManager = Manager.getInstance();
        dbManager.setFilenameTasks(dbTestFilenameTasks);
        dbManager.setFilenameActivities(dbTestFilenameActivities);
        dbManager.clear();

        // first test writing
        testVideoId = "testid";
        testVideoLanguage = "nl";
        task1 = new Task(testVideoId, testVideoLanguage, Task.STATUS_UPLOADED_VIDEO_TO_AMARA);
        dbManager.addOrUpdateTask(task1);
        Task task2 = new Task("testid2", "nl", Task.STATUS_UPLOADED_VIDEO_TO_AMARA);
        dbManager.addOrUpdateTask(task2);
        Task task3 = new Task(testVideoId, "en", Task.STATUS_UPLOADED_VIDEO_TO_AMARA);
        dbManager.addOrUpdateTask(task3);

        countBefore = dbManager.getTasks().size();
        assertEquals(3, countBefore);

        // test activity
        testActivityId = "testId";
        activity = new Activity(testActivityId);
        dbManager.addOrUpdateActivity(activity);
        assertEquals(1, dbManager.getActivities().size());

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
        dbManager.setFilenameTasks(dbTestFilenameTasks);

        setupWriting();

        // than test reading
        dbManager.readFile();

        Task task = dbManager.findTask(testVideoId, testVideoLanguage);
        assertEquals(countBefore, dbManager.getTasks().size());

        Iterator<Task> taskIterator = dbManager.getTaskIterator();
        while (taskIterator.hasNext()) {
            Task task3 = taskIterator.next();
            logger.info(task3.toString());
        }

        Path pathTasksdb = Paths.get(dbTestFilenameTasks);
        Path pathActivitydb = Paths.get(dbTestFilenameActivities);
        try {
            Files.delete(pathTasksdb);
            Files.delete(pathActivitydb);
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
        dbManager.setFilenameTasks(dbTestFilenameTasks);

        setupWriting();
        dbManager.readFile();

        // test searching - found
        Task task = dbManager.findTask(testVideoId, testVideoLanguage);

        assertNotNull(task);
        assertEquals(testVideoId, task.getVideoId());

        // test searching - not found
        Task task2 = dbManager.findTask("897yuhjklj", testVideoLanguage);

        assertNull(task2);

        Path pathTasksdb = Paths.get(dbTestFilenameTasks);
        Path pathActivitydb = Paths.get(dbTestFilenameActivities);
        try {
            Files.delete(pathTasksdb);
            Files.delete(pathActivitydb);
        } catch (IOException e) {
            e.printStackTrace();
            assert(true);
        }

    }

    /**
     * test reading to db
     */
    @Test
    public void testSearchActivity()
    {
        Manager dbManager = Manager.getInstance();
        dbManager.setFilenameTasks(dbTestFilenameActivities);

        setupWriting();
        dbManager.readFile();

        // test searching - found
        Activity activity = dbManager.findActivityById(testActivityId);

        assertNotNull(activity);
        assertEquals(testActivityId, activity.getId());

        // test searching - not found
        Activity activity1 = dbManager.findActivityById("897yuhjklj");

        assertNull(activity1);

        Path pathTasksdb = Paths.get(dbTestFilenameTasks);
        Path pathActivitydb = Paths.get(dbTestFilenameActivities);
        try {
            Files.delete(pathTasksdb);
            Files.delete(pathActivitydb);
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
        dbManager.setFilenameTasks(dbTestFilenameTasks);

        setupWriting();
        dbManager.readFile();

        String videoTestId = "%^&GHJK";
        Task task2 = new Task(videoTestId, "nl", Task.STATUS_UPLOADED_VIDEO_TO_AMARA);
        dbManager.addOrUpdateTask(task2);
        dbManager.addOrUpdateTask(task2);

        // test searching
        Task task = dbManager.findTask(testVideoId, testVideoLanguage);

        assertNotNull(task);
        assertEquals(testVideoId, task.getVideoId());

        Path pathTasksdb = Paths.get(dbTestFilenameTasks);
        Path pathActivitydb = Paths.get(dbTestFilenameActivities);
        try {
            Files.delete(pathTasksdb);
            Files.delete(pathActivitydb);
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
        dbManager.setFilenameTasks(dbTestFilenameTasks);
        setupWriting();
        dbManager.readFile();

        // actual test
        Iterator<Task> taskIterator = dbManager.getTaskIterator();
        while (taskIterator.hasNext()) {
            Task task3 = taskIterator.next();
            logger.info(task3.toString());
        }

//        Path pathTasksdb = Paths.get(dbTestFilenameTasks);
//        Path pathActivitydb = Paths.get(dbTestFilenameActivities);
//        try {
//            Files.delete(pathTasksdb);
//            Files.delete(pathActivitydb);
//        } catch (IOException e) {
//            e.printStackTrace();
//            assert(true);
//        }

    }

}