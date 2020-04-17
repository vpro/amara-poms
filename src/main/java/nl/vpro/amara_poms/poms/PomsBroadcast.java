package nl.vpro.amara_poms.poms;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import nl.vpro.amara_poms.Config;
import nl.vpro.domain.media.MediaObject;
import nl.vpro.domain.media.Program;
import nl.vpro.domain.media.support.Image;
import nl.vpro.domain.media.support.Images;
import nl.vpro.domain.media.update.ProgramUpdate;
import nl.vpro.domain.subtitles.*;
import nl.vpro.i18n.Locales;
import nl.vpro.util.CountedIterator;

/**
 * @author joost
 */
@Slf4j
public class PomsBroadcast {

    private final MediaObject program;

    @Getter
    private final String mid;

    String subtitles = "";

    public ProgramUpdate getUpdate() {
        if (program instanceof Program) {
            return ProgramUpdate.create((Program) program);
        } else {
            return null;
        }
    }

    public MediaObject getProgram() {
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

    public PomsBroadcast(String mid, MediaObject program) {
        this.mid = mid;
        this.program = program;
    }


    public void removeFromCollection(String midCollectionFrom) {
        log.info("Remove {} from collection {}", mid, midCollectionFrom);
        Config.getPomsClient().removeMember(midCollectionFrom, mid, null);
    }


    /**
     * Download subtitle from omroep.nl, otherwise try vpro.nl test
     *
     * @return NO_ERROR if successfull, otherwise errorcode
     */
    public int downloadSubtitles() throws IOException {
        try {

            Subtitles subtitlesObject = Config.getPomsClient().getBackendRestService()
                .getSubtitles(mid, Locales.DUTCH, SubtitlesType.CAPTION, true, false);
            if (subtitlesObject != null) {
                CountedIterator<StandaloneCue> parsed = SubtitlesUtil.standaloneIterator(subtitlesObject, false, true);
                ByteArrayOutputStream subtitleBytes = new ByteArrayOutputStream();
                SubtitlesUtil.stream(parsed, SubtitlesFormat.WEBVTT, subtitleBytes);
                this.subtitles = new String(subtitleBytes.toByteArray(),  SubtitlesFormat.WEBVTT.getCharset());
                return Config.NO_ERROR;
            }
        } catch (Exception e) {
            // TODO Heap space exception on poms-dev?
            log.error(e.getMessage());
        }
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
            log.error("malformed url " + e.toString());
            return Config.ERROR_POM_SUBTITLES_MALFORMED_URL;
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
                    log.info("Subtitles downloaded from " + urlName);
                    log.info("Subtitle content:" + StringUtils.abbreviate(content.replaceAll("(\\r|\\n)", ""), 100));
                    subtitles = content;
                } else {
                    returnValue = Config.ERROR_POM_SUBTITLES_NOT_FOUND;
                    log.error("Subtitle file {} doesn't start with WEBVTT -> file ignored", urlName);
                }
            } else {
                log.error("No subtitle file (Url=" + urlName + ") to download. Server replied HTTP code: " + responseCode);
                returnValue = Config.ERROR_POM_SUBTITLES_RESPONSE;
            }
            httpConn.disconnect();

        } catch (IOException e) {
            log.error("error opening url " + e.toString());
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
