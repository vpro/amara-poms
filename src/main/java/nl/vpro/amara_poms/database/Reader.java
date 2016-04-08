package nl.vpro.amara_poms.database;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by joost on 07/04/16.
 */
public class Reader {

    final static Logger logger = LoggerFactory.getLogger(Reader.class);

    public static ArrayList<Task> readCsvFile(String fileName) {

        ArrayList<Task> tasks = new ArrayList<Task>();

        FileReader fileReader = null;

        CSVParser csvFileParser = null;

        // Create the CSVFormat object with the header mapping
        CSVFormat csvFileFormat = CSVFormat.DEFAULT.withHeader(Task.getFileHeaderForReading());

        try {

            // initialize FileReader object
            fileReader = new FileReader(fileName);

            // initialize CSVParser object
            csvFileParser = new CSVParser(fileReader, csvFileFormat);

            Iterator<CSVRecord> iterator = csvFileParser.iterator();

            // skip header row
            if (iterator.hasNext()) { iterator.next();};

            // loop
            while (iterator.hasNext()) {
                tasks.add(Task.Factory(iterator.next()));
            }

        }
        catch (Exception e) {
            System.out.println("Error in CsvFileReader !!!");
            e.printStackTrace();
        } finally {
            try {
                fileReader.close();
                csvFileParser.close();
            } catch (IOException e) {
                System.out.println("Error while closing fileReader/csvFileParser !!!");
                e.printStackTrace();
            }
        }

        return tasks;

    }
}
