package nl.vpro.amara_poms.database;

import org.apache.james.mime4j.field.datetime.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Manager for csv file database
 */
public class Manager {

    final static Logger logger = LoggerFactory.getLogger(Reader.class);

    String filename;
    ArrayList<Task> tasks;

    public String getFilename() {
        return filename;
    }
    public void setFilename(String filename) {
        this.filename = filename;
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
        Writer.writeCsvFile(filename, tasks);
    }
    public void readFile() {
        tasks = Reader.readCsvFile(filename);
    }

    /**
     * Remove all entries
     */
    public void clear() {
        tasks.clear();
    }

    /**
     * Add task
     * @param task
     */
    public void addTask(Task task) {

        // check if already exists
        Task foundTask = findTaskByVideoId(task.getVideoId());
        if (foundTask == null) {
            task.setCreateDateTime(ZonedDateTime.now());
            task.setUpdateDateTime(ZonedDateTime.now());
        } else {
            // already found so merge
            task.setCreateDateTime(foundTask.getCreateDateTime());
            task.setUpdateDateTime(ZonedDateTime.now());
            removeTaskByVideoId(task.getVideoId());
        }

        // add/update task
        tasks.add(task);
    }

    /**
     * Find task by videoId
     * @param videoId
     * @return
     */
    public Task findTaskByVideoId(String videoId) {
        Task foundTask = null;
        List<Task> foundTasks = tasks.stream().filter((task) -> task.getVideoId().equals(videoId)).collect(Collectors.toList());

        if (foundTasks.size() == 0) {
            logger.info(videoId + " not found in db");
        } else if (foundTasks.size() > 1) {
            logger.error(videoId + " found more than 1 time in db");
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
    public void removeTaskByVideoId(String videoId) {
        logger.info("Task with videoId " + videoId + " has been removed");
        tasks.removeIf((task) -> task.getVideoId().equals(videoId));
    }

    /**
     * Get iterator
     * @return
     */
    public Iterator<Task> getTaskIterator() {
        return  tasks.iterator();
    }
}
