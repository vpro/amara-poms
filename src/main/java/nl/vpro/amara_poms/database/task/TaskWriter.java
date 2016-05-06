package nl.vpro.amara_poms.database.task;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
* @author joost
*/
public class TaskWriter {

    private final static Logger LOG = LoggerFactory.getLogger(TaskWriter.class);

    //Delimiter used in CSV file
    private static final String NEW_LINE_SEPARATOR = "\n";


    public static void writeCsvFile(String fileName, List<Task> tasks) {

        FileWriter fileWriter = null;

        CSVPrinter csvFilePrinter = null;

        // Create the CSVFormat object with "\n" as a record delimiter
        CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator(NEW_LINE_SEPARATOR);

        try {

            // initialize FileWriter object
            fileWriter = new FileWriter(fileName);

            // initialize CSVPrinter object
            csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat);

            // Create CSV file header
            csvFilePrinter.printRecord(Task.getFileHeader());

            // Write a new task object list to the CSV file
            for (Task task : tasks) {
                csvFilePrinter.printRecord(task.get());
            }
        } catch (Exception e) {
            LOG.error("Error in CsvFileWriter !!! " + e.getMessage(), e);
        } finally {
            try {
                fileWriter.flush();
                fileWriter.close();
                csvFilePrinter.close();
            } catch (IOException e) {
                System.out.println("Error while flushing/closing fileWriter/csvPrinter !!!");
                e.printStackTrace();
            }
        }
    }
}

