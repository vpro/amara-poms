package nl.vpro.amara_poms.database.activity;

import nl.vpro.amara_poms.database.activity.Activity;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by joost on 09/04/16.
 */
public class ActivityWriter {

    //Delimiter used in CSV file
    private static final String NEW_LINE_SEPARATOR = "\n";


    public static void writeCsvFile(String fileName, ArrayList<Activity> activities) {

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
            csvFilePrinter.printRecord(Activity.getFileHeader());

            // Write a new activity object list to the CSV file
            for (Activity activity : activities) {
                csvFilePrinter.printRecord(activity.get());
            }

            System.out.println("CSV file was created successfully !!!");

        } catch (Exception e) {
            System.out.println("Error in CsvFileWriter !!!");
            e.printStackTrace();
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
