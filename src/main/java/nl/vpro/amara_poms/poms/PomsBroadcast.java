package nl.vpro.amara_poms.poms;

import nl.vpro.amara_poms.Config;
import nl.vpro.domain.media.Location;
import nl.vpro.domain.media.Program;
import nl.vpro.domain.media.support.Image;
import nl.vpro.domain.media.support.TextualType;
import nl.vpro.domain.media.support.Title;
import nl.vpro.domain.media.update.*;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tools.ant.DirectoryScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
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

    ProgramUpdate programUpdate;
    MemberUpdate memberUpdate;

    SortedSet<LocationUpdate> locationSortedSet;

    String mid;
    String externalUrl;

    // config
    String subtitleUrl;
    String subtitleUrlBackup;

    String downloadSourcepathBase;
    String downloadSourcepathVariable;
    String downloadSourcefilePattern;
    String downloadSourcepathFull;

    String downloadTargetpathBase;
    String downloadTargetpathExtended;
    String downloadTargetpathFull;
    String downloadTargetpathFullInclFile;

    String subtitles = "";

    public MediaUpdate getProgramUpdate() {
        return programUpdate;
    }

    public String getExternalUrl() {

        String basePath = Config.getRequiredConfig("download.url.base");
        String extension = Config.getRequiredConfig("download.url.ext");
        externalUrl = basePath + mid + "." + extension;
        return externalUrl;
    }

    public String getTitle() {
        String title = "";

        Iterator<TitleUpdate> iterator = programUpdate.getTitles().iterator();
        while (iterator.hasNext() && title.equals("")) {
            TitleUpdate titleUpdate = iterator.next();
            if (titleUpdate.getType() == TextualType.MAIN) {
                title = titleUpdate.getTitle();
            }
        }

        return title;
    }

    public String getSubTitle() {
        String subTitle = "";

        Iterator<TitleUpdate> iterator = programUpdate.getTitles().iterator();
        while (iterator.hasNext() && subTitle.equals("")) {
            TitleUpdate titleUpdate = iterator.next();
            if (titleUpdate.getType() == TextualType.SUB) {
                subTitle = titleUpdate.getTitle();
            }
        }

        return subTitle;
    }

    public String getDescription() {

        String shortDescription = "";

        Iterator<DescriptionUpdate> iterator = programUpdate.getDescriptions().iterator();
        while (iterator.hasNext() && shortDescription.equals("")) {
            DescriptionUpdate descriptionUpdate = iterator.next();
            if (descriptionUpdate.getType() == TextualType.SHORT) {
                shortDescription = descriptionUpdate.getDescription();
            }
        }

        return shortDescription;
    }

    public String getSubtitles() {
        return subtitles;
    }

    public String getDuration() {
        long duration = programUpdate.getDuration().getSeconds();
        return Long.toString(duration);
    }

    public PomsBroadcast(String mid) {
        this.mid = mid;

        getConfig();
    }

    public  PomsBroadcast(MemberUpdate memberUpdate) {
        this.memberUpdate = memberUpdate;
        mid = memberUpdate.getMediaUpdate().getMid();

        // get more info through program update
        programUpdate = ProgramUpdate.forAllOwners(Utils.getClient().getFullProgram(mid));

        locationSortedSet = programUpdate.getLocations();

        getConfig();
    }

    private void getConfig() {
        // config
        subtitleUrl = Config.getRequiredConfig("subtitle.url");
        subtitleUrlBackup = Config.getConfig("subtitle.url.backup"); // optional

        // get config
        downloadSourcepathBase = Config.getRequiredConfig("download.source.path.base");
        downloadSourcepathVariable = Config.getRequiredConfig("download.source.path.variable");
        downloadSourcefilePattern = Config.getRequiredConfig("download.source.file.pattern");
        downloadSourcepathFull = downloadSourcepathVariable + mid + "/" + downloadSourcefilePattern;

        downloadTargetpathBase = Config.getRequiredConfig("download.target.path.base");
        downloadTargetpathExtended = Config.getRequiredConfig("download.target.path.extended");
        downloadTargetpathFull = downloadTargetpathBase + downloadTargetpathExtended;
        downloadTargetpathFullInclFile = downloadTargetpathFull + mid + "." + Config.getRequiredConfig("download.url.ext");
    }

    public void removeFromCollection(String midCollectionFrom) {
        logger.info("Remove from collection " + midCollectionFrom);

        SortedSet<MemberRefUpdate> memberUpdate = programUpdate.getMemberOf();

        // remove collection
        memberUpdate.removeIf(member -> member.getMediaRef().equals(midCollectionFrom));

        // update
//        programUpdate.setMemberOf(memberUpdate);
        String pomsMid = Utils.getClient().set(programUpdate);
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
            Path targetDirInclFile = Paths.get(downloadTargetpathFullInclFile);

            // create output dir if not exists
            try {
                Files.createDirectories(targetDir);
            } catch (IOException e) {
                logger.error(e.toString());
                System.exit(Config.ERROR_CREATING_OUTPUTDIR);
            }

            // copy file
            logger.info("About to copy file from " + inputPathInclFile + " to " + downloadTargetpathFullInclFile);
            try {
                Files.copy(source, targetDirInclFile, REPLACE_EXISTING);
            } catch (IOException e) {
                logger.error(e.toString());
                returnValue = Config.ERROR_COPY_INPUTFILE;
            }
//            String basePath = Config.getRequiredConfig("download.url.base");
//            externalUrl = basePath + downloadTargetpathExtended + mid + File.separator + targetDirInclFile.getFileName();
        }

        return returnValue;
    }


    /**
     * Download subtitle from omroep.nl, otherwise try vpro.nl
     *
     * @return NO_ERROR if successfull, otherwise errorcode
     */
    public int downloadSubtitles() {

        int returnValue = downloadSubtitles(subtitleUrl + mid);
        if (returnValue != Config.NO_ERROR && subtitleUrlBackup != null && !subtitleUrlBackup.isEmpty()) {
            returnValue = downloadSubtitles(subtitleUrlBackup + mid);
        }

        return returnValue;
    }

    /**
     * Download subtitles
     * @param urlName
     * @return NO_ERROR if successfull, otherwise errorcode
     */
    private int downloadSubtitles(String urlName) {

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

                StringWriter writer = new StringWriter();
                IOUtils.copy(inputStream, writer, "UTF-8");
                String content = writer.toString();

                inputStream.close();

                // check against 'WEBVTT'
                if (content.startsWith("WEBVTT")) {
                    logger.info("Subtitle downloadeded from " + urlName);
                    logger.info("Subtitle content:" + StringUtils.abbreviate(content.replaceAll("(\\r|\\n)", ""), 100));
                    subtitles = content;
                } else {
                    returnValue = Config.ERROR_POM_SUBTITLES_NOT_FOUND;
                    logger.info("Subtitle file doesn't start with WEBVTT -> file ignored");
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

    /**
     * Extract image id from first image
     * @return id as a String
     */
    public String getImageId() {
        List<ImageUpdate> images = programUpdate.getImages();
        String imageId = null;

        // get first image
        if (images.size() > 0) {
            ImageUpdate imageUpdate = images.get(0);

            // get urn (e.g. urn:vpro.image:6975) and extract last id
            String urn = imageUpdate.getImage().toString();
            String[] urnParts = urn.split(":");
            imageId = urnParts[urnParts.length - 1];
            if (imageId.matches("[0-9]+")) {
                logger.info("Image id extracted " + imageId);
            } else {
                logger.error("Image id not numeric " + imageId);
                imageId = null;
            }
        }

        return imageId;
    }

}
