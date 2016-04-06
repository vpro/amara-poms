package nl.vpro.amara_poms.poms;

import nl.vpro.amara_poms.Config;
import nl.vpro.domain.media.Location;
import nl.vpro.domain.media.MemberRef;
import nl.vpro.domain.media.Program;
import nl.vpro.domain.media.update.MediaUpdate;
import nl.vpro.domain.media.update.MemberRefUpdate;
import nl.vpro.domain.media.update.MemberUpdate;
import nl.vpro.rs.media.MediaRestClient;

import org.apache.tools.ant.DirectoryScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.util.SortedSet;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * Created by joost on 04/04/16.
 */
public class PomsBroadcast {

    final Logger logger = LoggerFactory.getLogger(PomsBroadcast.class);
    private static final int BUFFER_SIZE = 4096;

    MediaUpdate mediaUpdate;
    MemberUpdate memberUpdate;
    SortedSet<Location> locationSortedSet;

    String mid;
    String externalUrl;

    String downloadSourcepathBase;
    String downloadSourcepathVariable;
    String downloadSourcefilePattern;
    String downloadSourcepathFull;

    String downloadTargetpathBase;
    String downloadTargetpathExtended;
    String downloadTargetpathFull;

    // config
    String subtitleUrl;
    String subtitleUrlBackup;
    String subtitleFilename;

    String subtitles = "";

    Program program;

    public MediaUpdate getMediaUpdate() {
        return mediaUpdate;
    }

    public String getExternalUrl() {
        return externalUrl;
    }

    public String getTitle() {
        return program.getMainTitle();
    }

    public String getDescription() {
        return program.getShortDescription();
    }

    public String getSubtitles() {
        return subtitles;
    }

    public PomsBroadcast(String mid) {
        this.mid = mid;

        getConfig();
    }

    public  PomsBroadcast(MemberUpdate memberUpdate) {
        this.memberUpdate = memberUpdate;
        mediaUpdate = memberUpdate.getMediaUpdate();

        mid = mediaUpdate.getMid();

        program = Utils.getClient().getFullProgram(mid);
        locationSortedSet = program.getLocations();

        getConfig();
    }

    private void getConfig() {
        // get config
        downloadSourcepathBase = Config.getRequiredConfig("download.source.path.base");
        downloadSourcepathVariable = Config.getRequiredConfig("download.source.path.variable");
        downloadSourcefilePattern = Config.getRequiredConfig("download.source.file.pattern");
        downloadSourcepathFull = downloadSourcepathVariable + mid + "/" + downloadSourcefilePattern;

        downloadTargetpathBase = Config.getRequiredConfig("download.target.path.base");
        downloadTargetpathExtended = Config.getRequiredConfig("download.target.path.extended");
        downloadTargetpathFull = downloadTargetpathBase + downloadTargetpathExtended + mid + "/";

        // config
        subtitleUrl = Config.getRequiredConfig("subtitle.url");
        subtitleUrlBackup = Config.getConfig("subtitle.url.backup"); // optional
        subtitleFilename = Config.getRequiredConfig("subtitle.filename");
    }

    public void moveFromCollectionToCollection(String midCollectionFrom, String midCollectionTo) {
        logger.info("Move from collection " + midCollectionFrom + " to collection " + midCollectionTo);

        SortedSet<MemberRef> memberUpdate = mediaUpdate.getMemberOf();

        // remove collection
        memberUpdate.removeIf(member -> member.getMidRef() == midCollectionFrom);

        // add collection
        MemberRef newCollection = new MemberRef();
        newCollection.setMidRef(midCollectionTo);
        memberUpdate.add(newCollection);

        // update
        mediaUpdate.setMemberOf(memberUpdate);
        Utils.getClient().set(mediaUpdate);
    }

    /**
     * Copy source video file to download.omroep.nl to make it accessable for Amara
     *
     * @return NO_ERROR if successfull, otherwise errorcode
     */
    public int downloadFileToDownloadServer() {

        int returnValue = Config.NO_ERROR;

        // find input file
        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setIncludes(new String[]{downloadSourcepathFull});
        scanner.setBasedir(downloadSourcepathBase);
        scanner.setCaseSensitive(false);
        scanner.scan();
        String[] files = scanner.getIncludedFiles();
        if (files.length == 0) {
            logger.error("Input file not found in " + downloadSourcepathBase + downloadSourcepathFull);
            returnValue = Config.ERROR_INPUT_FILE_NOT_FOUND;
        } else {

            // input file found
            String inputPathInclFile = downloadSourcepathBase + files[0];
            logger.info("Found input file:" + inputPathInclFile);
            Path source = Paths.get(inputPathInclFile);
            Path targetDir = Paths.get(downloadTargetpathFull);
            Path targetDirInclFile = Paths.get(downloadTargetpathFull + source.getFileName());

            // create output dir if not exists
            try {
                Files.createDirectories(targetDir);
            } catch (IOException e) {
                logger.error(e.toString());
                System.exit(Config.ERROR_CREATING_OUTPUTDIR);
            }

            // copy file
            logger.info("About to copy file from " + inputPathInclFile + " to " + downloadTargetpathFull);
            try {
                Files.copy(source, targetDirInclFile, REPLACE_EXISTING);
            } catch (IOException e) {
                logger.error(e.toString());
                returnValue = Config.ERROR_COPY_INPUTFILE;
            }
            String basePath = Config.getRequiredConfig("download.url.base");
            externalUrl = basePath + downloadTargetpathExtended + mid + File.separator + targetDirInclFile.getFileName();
        }

        return returnValue;
    }

    /**
     * Download subtitle from omroep.nl, otherwise try vpro.nl
     *
     * @return NO_ERROR if successfull, otherwise errorcode
     */
    public int downloadSubtitles() {

        int returnValue = downloadSubtitles(subtitleUrl + mid, subtitleFilename);
        if (returnValue != Config.NO_ERROR && subtitleUrlBackup != null && !subtitleUrlBackup.isEmpty()) {
            returnValue = downloadSubtitles(subtitleUrlBackup + mid, subtitleFilename);
        }

        return returnValue;
    }

    /**
     * Download subtitles
     * @param urlName
     * @param targetFilename
     * @return NO_ERROR if successfull, otherwise errorcode
     */
    private int downloadSubtitles(String urlName, String targetFilename) {

        int returnValue = Config.NO_ERROR;

        URL url;
        try {
            url = new URL(urlName);
        } catch (MalformedURLException e) {
            logger.error("malformed url " + e.toString());
            return (Config.ERROR_POM_SUBTITLES_MALFORMED_URL);
        }

        try {
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
            int responseCode = httpConn.getResponseCode();

            // always check HTTP response code first
            if (responseCode == HttpURLConnection.HTTP_OK) {
                String disposition = httpConn.getHeaderField("Content-Disposition");
                String contentType = httpConn.getContentType();
                int contentLength = httpConn.getContentLength();

                // opens input stream from the HTTP connection
                InputStream inputStream = httpConn.getInputStream();
                String saveFilePath = downloadTargetpathFull + File.separator + targetFilename;

                // opens an output stream to save into file
                FileOutputStream outputStream = new FileOutputStream(saveFilePath);

                String content = "";
                int bytesRead = -1;
                byte[] buffer = new byte[BUFFER_SIZE];
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                    content += new String(buffer);
                }

                outputStream.close();
                inputStream.close();

                // check against 'No subtitle found'
                if (content.startsWith(Config.getRequiredConfig("subtitle.notfoundmsg"))) {
                    returnValue = Config.ERROR_POM_SUBTITLES_NOT_FOUND;
                    logger.info("Subtitle file contains " + Config.getRequiredConfig("subtitle.notfoundmsg") + " -> ignored");
                } else {
                    logger.info("Subtitle file " + targetFilename + " downloaded");
                    subtitles = content;
                }
            } else {
                logger.error("No subtitle file (Url=" + urlName + ") to download. Server replied HTTP code: " + responseCode);
                returnValue = Config.ERROR_POM_SUBTITLES_RESPONSE;
            }
            httpConn.disconnect();

        } catch (IOException e) {
                logger.error("error opening url " + e.toString());
                return (Config.ERROR_POM_SUBTITLES_URL_OPEN);
        }

        return returnValue;
    }

}
