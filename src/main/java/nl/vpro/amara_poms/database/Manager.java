package nl.vpro.amara_poms.database;

import nl.vpro.amara_poms.database.task.TaskReader;
import nl.vpro.amara_poms.database.task.Task;
import nl.vpro.amara_poms.database.task.TaskWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Manager for csv file database
 */
public class Manager {

    final static Logger logger = LoggerFactory.getLogger(TaskReader.class);

    String filenameTasks;
    String filenameActivities;
    ArrayList<Task> tasks;

    public void setFilenameTasks(String filenameTasks) {
        this.filenameTasks = filenameTasks;
    }
    public void setFilenameActivities(String filenameActivities) {
        this.filenameActivities = filenameActivities;
    }

    public ArrayList<Task> getTasks() {
        return tasks;
    }

    /**
     * singleton manager
     */
    private static Manager instance = null;
    protected Manager() {
        tasks = new ArrayList<Task>();
    }
    public static Manager getInstance() {
        if(instance == null) {
            instance = new Manager();
        }
        return instance;
    }

    /**
     * writing and reading
     */
    public void writeFile() {
        TaskWriter.writeCsvFile(filenameTasks, tasks);
    }
    public void readFile() {
        if (Files.exists((Paths.get(filenameTasks)))) {
            tasks = TaskReader.readCsvFile(filenameTasks);
        } else {
            tasks =  new ArrayList<Task>();
        }
    }

    /**
     * Remove all entries
     */
    public void clear() {
        tasks.clear();
    }

    /**
     * Add or update task
     * @param task
     */
    public void addOrUpdateTask(Task task) {

        // check if already exists
        Task foundTask = findTask(task.getVideoId(), task.getLanguage());
        if (foundTask == null) {
            task.setCreateDateTime(ZonedDateTime.now());
            task.setUpdateDateTime(ZonedDateTime.now());
        } else {
            // already found so merge
            task.setCreateDateTime(foundTask.getCreateDateTime());
            task.setUpdateDateTime(ZonedDateTime.now());
            removeTaskByVideoId(task.getVideoId(), task.getLanguage());
        }

        // add/update task
        tasks.add(task);
        writeFile();
    }

    /**
     * Find task by videoId and language
     * @param videoId
     * @param language
     * @return
     */
    public Task findTask(String videoId, String language) {
        Task foundTask = null;

        List<Task> foundTasks = tasks.stream().filter((task) -> task.getVideoId().equals(videoId) &&
                                                                task.getLanguage().equals(language)).collect(Collectors.toList());

        if (foundTasks.size() == 0) {
            logger.info(videoId + " not found (yet) in db");
        } else if (foundTasks.size() > 1) {
            logger.error(videoId + " found more than 1 time in db");
            foundTask = foundTasks.get(0);
        } else {
            foundTask = foundTasks.get(0);
        }

        return  foundTask;
    }

    /**
     * Find task by source mid
     * @param pomsMid
     * @return
     */
    public Task findTaskByPomsSourceId(String pomsMid) {
        Task foundTask = null;

        List<Task> foundTasks = tasks.stream().filter((task) -> task.getPomsSourceMid().equals(pomsMid)).collect(Collectors.toList());

        if (foundTasks.size() == 0) {
            logger.info(pomsMid + " not found (yet) in db");
        } else if (foundTasks.size() > 1) {
            logger.error(pomsMid + " found more than 1 time in db");
            foundTask = foundTasks.get(0);
        } else {
            foundTask = foundTasks.get(0);
        }

        return  foundTask;
    }

    /**
     * Remove task by videoId
     * @param videoId
     */
    public void removeTaskByVideoId(String videoId, String language) {
        logger.info("Task with videoId " + videoId + " has been removed");
        tasks.removeIf((task) -> task.getVideoId().equals(videoId) && task.getLanguage() == language);
    }

    /**
     * Get iterator
     * @return
     */
    public Iterator<Task> getTaskIterator() {
        return  tasks.iterator();
    }
}
