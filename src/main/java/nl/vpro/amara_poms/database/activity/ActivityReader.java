package nl.vpro.amara_poms.database.activity;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by joost on 09/04/16.
 */
public class ActivityReader {


    final static Logger logger = LoggerFactory.getLogger(ActivityReader.class);

    public static ArrayList<Activity> readCsvFile(String fileName) {

        ArrayList<Activity> activities = new ArrayList<Activity>();

        FileReader fileReader = null;

        CSVParser csvFileParser = null;

        // Create the CSVFormat object with the header mapping
        CSVFormat csvFileFormat = CSVFormat.DEFAULT.withHeader(Activity.getFileHeaderForReading());

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
                activities.add(Activity.Factory(iterator.next()));
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

        return activities;

    }

}
