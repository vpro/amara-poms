package nl.vpro.amara_poms.database;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.vpro.amara_poms.Config;
import nl.vpro.amara_poms.database.task.Task;
import nl.vpro.amara_poms.database.task.TaskReader;
import nl.vpro.amara_poms.database.task.TaskWriter;

/**
 * Manager for csv file database
 */
public class Manager implements Iterable<Task> {

    private final static Logger LOG = LoggerFactory.getLogger(TaskReader.class);

    /**
     * singleton manager
     */
    private static final Manager INSTANCE = new Manager();

    public static Manager getInstance() {
        return INSTANCE;
    }

    private String filenameTasks;
       private final List<Task> tasks = new ArrayList<>();

    public void setFilenameTasks(String filenameTasks) {
        this.filenameTasks = filenameTasks;
    }

    public List<Task> getTasks() {
        return tasks;
    }


    /**
     * writing and reading
     */
    public void writeFile() {
        TaskWriter.writeCsvFile(filenameTasks, tasks);
    }
    public void readFile() {
        if (Files.isReadable((Paths.get(filenameTasks)))) {
            tasks.clear();
            tasks.addAll(TaskReader.readCsvFile(filenameTasks));
        } else {
            throw new Config.Error("The database file " + filenameTasks + " cannot be opened", Config.ERROR_DB_NOT_READABLE);
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
     */
    public Task findTask(String videoId, String language) {

        List<Task> foundTasks = tasks.stream().filter((task) -> task.getVideoId().equals(videoId) &&
                                                                task.getLanguage().equals(language)).collect(Collectors.toList());

        if (foundTasks.size() == 0) {
            LOG.info("videoid {}/{} not found (yet) in db", videoId, language);
            return null;
        } else if (foundTasks.size() > 1) {
            LOG.error("videoId {}/{} found more than 1 time in db", videoId, language);
        }

        return  foundTasks.get(0);
    }

    /**
     * Find task by source mid
     */
    public Task findTaskByPomsSourceId(String pomsMid) {

        List<Task> foundTasks = tasks.stream().filter((task) -> task.getPomsSourceMid().equals(pomsMid)).collect(Collectors.toList());

        if (foundTasks.size() == 0) {
            LOG.info("MID {} not found (yet) in db", pomsMid);
            return null;
        } else if (foundTasks.size() > 1) {
            LOG.error("MID {} found more than 1 time in db", pomsMid);
        }

        return  foundTasks.get(0);
    }

    /**
     * Remove task by videoId
     */
    public void removeTaskByVideoId(String videoId, String language) {
        LOG.info("Task with videoId " + videoId + " has been removed");
        tasks.removeIf((task) -> task.getVideoId().equals(videoId) && task.getLanguage() == language);
    }


    @Override
    public Iterator<Task> iterator() {
        return tasks.iterator();
    }
}
