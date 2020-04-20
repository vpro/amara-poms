package nl.vpro.amara_poms.database.task;

import lombok.extern.slf4j.Slf4j;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

/**
 * @author joost
 */
@Slf4j
public class TaskReader {

    public static List<DatabaseTask> readCsvFile(String fileName) {

        final List<DatabaseTask> tasks = new ArrayList<>();


        // Create the CSVFormat object with the header mapping
        CSVFormat csvFileFormat = CSVFormat.DEFAULT.withHeader(DatabaseTask.getFileHeaderForReading());

        try (
            FileReader fileReader  = new FileReader(fileName);
            CSVParser csvFileParser= new CSVParser(fileReader,csvFileFormat)
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
            log.error("Error in CsvFileReader: " + e.getMessage(), e);
        }

        return tasks;

    }
}
