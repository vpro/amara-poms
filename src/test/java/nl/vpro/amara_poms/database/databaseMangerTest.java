package nl.vpro.amara_poms.database;


import nl.vpro.amara_poms.Config;

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
public class databaseMangerTest {

    final static Logger logger = LoggerFactory.getLogger(databaseMangerTest.class);
    final static String dbTestFilename = "./testdb.csv";

    Task task1;
    String testVideoId;
    int countBefore;

    /**
     * setup method for writing
     */
    public void setupWriting() {

        Config.init();

        Manager dbManager = Manager.getInstance();
        dbManager.setFilename(dbTestFilename);
        dbManager.clear();

        // first test writing
        testVideoId = "testid";
        task1 = new Task(testVideoId, "nl", Task.STATUS_INITTIAL);
        dbManager.addTask(task1);
        Task task2 = new Task("testid2", "nl", Task.STATUS_INITTIAL);
        dbManager.addTask(task2);
        countBefore = dbManager.getTasks().size();
        assertEquals(2, countBefore);

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
        dbManager.setFilename(dbTestFilename);

        setupWriting();

        // than test reading
        dbManager.readFile();

        Task task = dbManager.findTaskByVideoId(testVideoId);
        assertEquals(countBefore, dbManager.getTasks().size());

        Iterator<Task> taskIterator = dbManager.getTaskIterator();
        while (taskIterator.hasNext()) {
            Task task3 = taskIterator.next();
            logger.info(task3.toString());
        }

        Path path = Paths.get(dbTestFilename);
        try {
            Files.delete(path);
        } catch (IOException e) {
            e.printStackTrace();
            assert(true);
        }

    }


    /**
     * test reading to db
     */
    @Test
    public void testSearch()
    {
        Manager dbManager = Manager.getInstance();
        dbManager.setFilename(dbTestFilename);

        setupWriting();
        dbManager.readFile();

        // test searching - found
        Task task = dbManager.findTaskByVideoId(testVideoId);

        assertNotNull(task);
        assertEquals(testVideoId, task.getVideoId());

        // test searching - not found
        Task task2 = dbManager.findTaskByVideoId("897yuhjklj");

        assertNull(task2);

        Path path = Paths.get(dbTestFilename);
        try {
            Files.delete(path);
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
        dbManager.setFilename(dbTestFilename);

        setupWriting();
        dbManager.readFile();

        String videoTestId = "%^&GHJK";
        Task task2 = new Task(videoTestId, "nl", Task.STATUS_INITTIAL);
        dbManager.addTask(task2);
        dbManager.addTask(task2);

        // test searching
        Task task = dbManager.findTaskByVideoId(testVideoId);

        assertNotNull(task);
        assertEquals(testVideoId, task.getVideoId());

        Path path = Paths.get(dbTestFilename);
        try {
            Files.delete(path);
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
        dbManager.setFilename(dbTestFilename);
        setupWriting();
        dbManager.readFile();

        // actual test
        Iterator<Task> taskIterator = dbManager.getTaskIterator();
        while (taskIterator.hasNext()) {
            Task task3 = taskIterator.next();
            logger.info(task3.toString());
        }

        Path path = Paths.get(dbTestFilename);
        try {
            Files.delete(path);
        } catch (IOException e) {
            e.printStackTrace();
            assert(true);
        }

    }

}