package nl.vpro.amara_poms.database;

import nl.vpro.amara_poms.database.activity.Activity;
import nl.vpro.amara_poms.database.activity.ActivityReader;
import nl.vpro.amara_poms.database.activity.ActivityWriter;
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
    ArrayList<Activity> activities;

    public void setFilenameTasks(String filenameTasks) {
        this.filenameTasks = filenameTasks;
    }
    public void setFilenameActivities(String filenameActivities) {
        this.filenameActivities = filenameActivities;
    }

    public ArrayList<Task> getTasks() {
        return tasks;
    }
    public ArrayList<Activity> getActivities() {
        return activities;
    }

    /**
     * singleton manager
     */
    private static Manager instance = null;
    protected Manager() {
        tasks = new ArrayList<Task>();
        activities = new ArrayList<Activity>();
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
        ActivityWriter.writeCsvFile(filenameActivities, activities);
    }
    public void readFile() {
        if (Files.exists((Paths.get(filenameTasks)))) {
            tasks = TaskReader.readCsvFile(filenameTasks);
        } else {
            tasks =  new ArrayList<Task>();
        }

        if (Files.exists((Paths.get(filenameTasks)))) {
            activities = ActivityReader.readCsvFile(filenameActivities);
        } else {
            activities = new ArrayList<Activity>();
        }
    }

    /**
     * Remove all entries
     */
    public void clear() {
        tasks.clear();
        activities.clear();
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
            removeTaskByVideoId(task.getVideoId());
        }

        // add/update task
        tasks.add(task);
    }

    /**
     * Add or update activity
     * @param activity
     */
    public void addOrUpdateActivity(Activity activity) {

        // check if already exists
        Activity foundActivity = findActivityById(activity.getId());
        if (foundActivity == null) {
            activity.setCreateDateTime(ZonedDateTime.now());
            activity.setUpdateDateTime(ZonedDateTime.now());
        } else {
            // already found so merge
            activity.setCreateDateTime(foundActivity.getCreateDateTime());
            activity.setUpdateDateTime(ZonedDateTime.now());
            removeActivityByVideoId(activity.getId());
        }

        // add/update activity
        activities.add(activity);
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
     * Find activities by id
     * @param id
     * @return
     */
    public  Activity findActivityById(String id) {
        Activity foundActivity = null;
        List<Activity> foundActivities = activities.stream().filter((activity) -> activity.getId().equals(id)).collect(Collectors.toList());

        if (foundActivities.size() == 0) {
            logger.info(id + " not found in db");
        } else if (foundActivities.size() > 1) {
            logger.error(id + " found more than 1 time in db");
            foundActivity = foundActivities.get(0);
        } else {
            foundActivity = foundActivities.get(0);
        }

        return  foundActivity;
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
     * Remove activity by Id
     * @param id
     */
    public void removeActivityByVideoId(String id) {
        logger.info("Activity with Id " + id + " has been removed");
        activities.removeIf((activity) -> activity.getId().equals(id));
    }


    /**
     * Get iterator
     * @return
     */
    public Iterator<Task> getTaskIterator() {
        return  tasks.iterator();
    }
}
