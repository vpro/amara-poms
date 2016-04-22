package nl.vpro.amara_poms.poms;

import nl.vpro.amara_poms.Config;
import nl.vpro.domain.media.Location;
import nl.vpro.domain.media.Program;
import nl.vpro.domain.media.support.Image;
import nl.vpro.domain.media.support.TextualType;
import nl.vpro.domain.media.support.Title;
import nl.vpro.domain.media.update.*;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.io.IOException;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

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

                String content = "";
                int bytesRead = -1;
                byte[] buffer = new byte[BUFFER_SIZE];
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    content += new String(buffer);
                }

                inputStream.close();

                // check against 'WEBVTT'
                if (content.startsWith("WEBVTT")) {
                    logger.info("Subtitle downloadeded from " + urlName);
                    logger.info("Subtitle content:" + StringUtils.abbreviate(content.replaceAll("(\\r|\\n)", ""), 50));
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
