package nl.vpro.amara_poms.database.task;

import lombok.extern.slf4j.Slf4j;

import java.time.*;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;

import nl.vpro.amara.domain.Task;
import nl.vpro.amara.domain.TaskType;
import nl.vpro.amara_poms.Config;
import nl.vpro.jackson2.Jackson2Mapper;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author joost
 */

@Slf4j
public class DatabaseTaskTest {

    @BeforeEach
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
        List<Task> amaraTasks = Config.getAmaraClient().teams().getTasks(TaskType.Translate,
            Instant.now().minus(Duration.ofDays(10)), 0, 100).getTasks();

        log.info("Count:" + amaraTasks.size());
        if (amaraTasks.size() > 0) {
            log.info(amaraTasks.get(0).toString());
        }
        log.info(amaraTasks.toString());

    }

    @Test
    public void test() {
        assertThat(DatabaseTask.parse("13/03/2019 - 12:00")).isEqualTo(ZonedDateTime.parse("2019-03-13T12:00+01:00[Europe/Amsterdam]"));


    }

}
