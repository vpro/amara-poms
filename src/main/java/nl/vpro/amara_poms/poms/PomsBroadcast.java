package nl.vpro.amara_poms.poms;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.vpro.amara_poms.Config;
import nl.vpro.domain.media.Program;
import nl.vpro.domain.media.support.Image;
import nl.vpro.domain.media.support.Images;
import nl.vpro.domain.media.update.ProgramUpdate;

/**
 * @author joost
 */
public class PomsBroadcast {

    private static final Logger LOG = LoggerFactory.getLogger(PomsBroadcast.class);

    private final Program program;
    private final String mid;

    String subtitles = "";

    public ProgramUpdate getProgramUpdate() {
        return ProgramUpdate.forAllOwners(program);
    }

    public Program getProgram() {
        return program;
    }


    public String getTitle() {
        return StringUtils.trim(program.getMainTitle());
    }

    public String getSubTitle() {
        return StringUtils.trim(program.getSubTitle());
    }

    public String getDescription() {
        return StringUtils.trim(program.getShortDescription());
    }

    public String getSubtitles() {
        return subtitles;
    }

    public String getDuration() {
        return String.valueOf(program.getDuration().get().getSeconds());
    }

    public PomsBroadcast(String mid, Program  program) {
        this.mid = mid;
        this.program = program;
    }


    public void removeFromCollection(String midCollectionFrom) {
        LOG.info("Remove {} from collection {}", mid, midCollectionFrom);
        Config.getPomsClient().removeMember(midCollectionFrom, mid, null);
    }


    /**
     * Download subtitle from omroep.nl, otherwise try vpro.nl
     *
     * @return NO_ERROR if successfull, otherwise errorcode
     */
    public int downloadSubtitles() {
        String subtitleUrl = Config.getRequiredConfig("subtitle.url");
        String subtitleUrlBackup = Config.getConfig("subtitle.url.backup"); // optional

        int returnValue = downloadSubtitles(subtitleUrl + mid);
        if (returnValue != Config.NO_ERROR && subtitleUrlBackup != null && !subtitleUrlBackup.isEmpty()) {
            returnValue = downloadSubtitles(subtitleUrlBackup + mid);
        }

        return returnValue;
    }

    /**
     * Download subtitles
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
                InputStream inputStream = httpConn.getInputStream();

                StringWriter writer = new StringWriter();
                IOUtils.copy(inputStream, writer, "UTF-8");
                String content = writer.toString();

                inputStream.close();

                // check against 'WEBVTT'
                if (content.startsWith("WEBVTT")) {
                    LOG.info("Subtitles downloaded from " + urlName);
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
            return Config.ERROR_POM_SUBTITLES_URL_OPEN;
        }

        return returnValue;
    }

    /**
     * Extract image id from first image
     * @return id as a String
     */
    public Optional<Image> getImage() {
        return program.getImages().stream().findFirst();
    }


    public String getThumbNailUrl() {
        return getImage()
            .map(i -> Images.getImageLocation(i, "jpg", "s620"))
            .orElse(null);
    }
}
