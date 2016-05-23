package nl.vpro.amara;

import java.time.Instant;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;

import nl.vpro.amara.domain.Task;
import nl.vpro.amara_poms.Config;
import nl.vpro.jackson2.Jackson2Mapper;

/**
 * @author joost
 */
public class TaskTest {

    private final static Logger LOG = LoggerFactory.getLogger(SubtitlesTest.class);

    @Before
    public void setUp() {
        Config.init();
    }

//    public void testPost() {
//
//        AmaraTask amaraTask = new AmaraTask("gDq7bAA5XFCR", "nl", "Translate", "netinnl");
//
//        AmaraTask newAmaraTask = AmaraTask.post(amaraTask);
//
//        assertNotNull(newAmaraTask);
//    }

    @Test
    public void bind() throws JsonProcessingException {
        Task task = new Task();
        task.setCompleted(Instant.now());
        System.out.println(Jackson2Mapper.getInstance().writeValueAsString(task));
    }

    @Test
    public void testGet() {
        long afterTimestampInSeconds = Config.getRequiredConfigAsLong("amara.task.fetchlastperiod.seconds");

        long now = System.currentTimeMillis() / 1000;

        List<Task> amaraTasks = Config.getAmaraClient().teams().getTasks(Task.TYPE_TRANSLATE, Instant.now().minusSeconds(afterTimestampInSeconds)).getTasks();

        LOG.info("Count:" + amaraTasks.size());
        if (amaraTasks.size() > 0) {
            LOG.info(amaraTasks.get(0).toString());
        }
        LOG.info(amaraTasks.toString());

    }

}
