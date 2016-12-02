package nl.vpro.amara_poms.database.task;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author joost
 */
public class TaskReader {

    private final static Logger LOG = LoggerFactory.getLogger(TaskReader.class);

    public static List<DatabaseTask> readCsvFile(String fileName) {

        List<DatabaseTask> tasks = new ArrayList<>();


        // Create the CSVFormat object with the header mapping
        CSVFormat csvFileFormat = CSVFormat.DEFAULT.withHeader(DatabaseTask.getFileHeaderForReading());

        try (
            FileReader fileReader  = new FileReader(fileName);
            CSVParser csvFileParser= new CSVParser(fileReader,csvFileFormat) ;
        ) {
            Iterator<CSVRecord> iterator = csvFileParser.iterator();

            // skip header row
            if (iterator.hasNext()) {
                iterator.next();
            }

            // loop
            while (iterator.hasNext()) {
                tasks.add(DatabaseTask.from(iterator.next()));
            }

        } catch (Exception e) {
            LOG.error("Error in CsvFileReader: " + e.getMessage(), e);
        }

        return tasks;

    }
}
