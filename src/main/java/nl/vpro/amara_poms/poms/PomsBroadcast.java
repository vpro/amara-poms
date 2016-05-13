package nl.vpro.amara_poms.poms;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.vpro.amara_poms.Config;
import nl.vpro.domain.media.support.TextualType;
import nl.vpro.domain.media.update.*;

/**
 * @author joost
 */
public class PomsBroadcast {

    private static final Logger LOG = LoggerFactory.getLogger(PomsBroadcast.class);
    private static final int BUFFER_SIZE = 4096;

    ProgramUpdate programUpdate;
    MemberUpdate memberUpdate;

    SortedSet<LocationUpdate> locationSortedSet;

    String mid;
    String externalUrl;

    // config
    String subtitleUrl;
    String subtitleUrlBackup;

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
        programUpdate = ProgramUpdate.forAllOwners(Config.getPomsClient().getFullProgram(mid));

        locationSortedSet = programUpdate.getLocations();

        getConfig();
    }

    private void getConfig() {
        // config
        subtitleUrl = Config.getRequiredConfig("subtitle.url");
        subtitleUrlBackup = Config.getConfig("subtitle.url.backup"); // optional
    }

    public void removeFromCollection(String midCollectionFrom) {
        LOG.info("Remove {} from collection {}", mid, midCollectionFrom);
        Config.getPomsClient().removeMember(midCollectionFrom, mid, null);
    }

    /**
     * Copy source video file to download.omroep.nl to make it accessable for Amara
     *
     * @return NO_ERROR if successfull, otherwise errorcode
     * TODO The fetch script contains:
     * <pre>
    DIR=/e/download/pages/vpro/netinnederland/h264
    for i in $*; do
       cp /e/pa/ceres/active/internetvod/h264/*...
    done
        </pre>
        It seems odd to require a system call for that!
     */
    public int downloadFileToDownloadServer() {

        int returnValue = Config.NO_ERROR;

        ProcessBuilder pb = new ProcessBuilder(Config.getRequiredConfig("fetch.script"), mid);
        pb.directory(new File("."));
        try {
            pb.start();
        } catch (IOException e) {
            LOG.error(e.toString());
            returnValue = Config.ERROR_COPY_INPUTFILE;
        }

        if (returnValue == Config.NO_ERROR) {
            LOG.info("Copied video file for mid " + mid);
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
            LOG.error("malformed url " + e.toString());
            return (Config.ERROR_POM_SUBTITLES_MALFORMED_URL);
        }

        try {
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
            int responseCode = httpConn.getResponseCode();

            // always check HTTP response code first
            if (responseCode == HttpURLConnection.HTTP_OK) {

                // TODO unused variables?
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
                    LOG.info("Subtitle downloadeded from " + urlName);
                    LOG.info("Subtitle content:" + StringUtils.abbreviate(content.replaceAll("(\\r|\\n)", ""), 100));
                    subtitles = content;
                } else {
                    returnValue = Config.ERROR_POM_SUBTITLES_NOT_FOUND;
                    LOG.info("Subtitle file doesn't start with WEBVTT -> file ignored");
                }
            } else {
                LOG.error("No subtitle file (Url=" + urlName + ") to download. Server replied HTTP code: " + responseCode);
                returnValue = Config.ERROR_POM_SUBTITLES_RESPONSE;
            }
            httpConn.disconnect();

        } catch (IOException e) {
                LOG.error("error opening url " + e.toString());
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
                LOG.info("Image id extracted " + imageId);
            } else {
                LOG.error("Image id not numeric " + imageId);
                imageId = null;
            }
        }

        return imageId;
    }

}
