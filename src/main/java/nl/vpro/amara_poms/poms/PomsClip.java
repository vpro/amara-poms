package nl.vpro.amara_poms.poms;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.xml.bind.JAXB;

import org.apache.commons.lang3.StringUtils;

import nl.vpro.amara_poms.Config;
import nl.vpro.domain.media.AVFileFormat;
import nl.vpro.domain.media.ProgramType;
import nl.vpro.domain.media.RelationDefinition;
import nl.vpro.domain.media.support.TextualType;
import nl.vpro.domain.media.update.*;
import nl.vpro.logging.LoggerOutputStream;
import nl.vpro.rs.media.MediaRestClient;

/**
 * @author Joost Aafjes
 */
@Slf4j
public class PomsClip {


    public static final RelationDefinition ORIGINAL = RelationDefinition.of("TRANSLATION_SOURCE", "VPRO");
    public static final String PORTAL = "NETINNL";

    /**
     * Create clip in poms
     * @param sourcePomsMid - source broadcast
     * @param title - title from Amara (serie // translated titel or translated title)
     * @param description - translated description from Amara
     */
    @SneakyThrows
    public static String create(MediaRestClient client, String sourcePomsMid, String language, String title, String description, String crid) {

        // get source broadcast
        log.info("Getting {} from {}", sourcePomsMid, client);
        ProgramUpdate sourceProgram = ProgramUpdate.create(client.getFullProgram(sourcePomsMid));

        // construct new CLIP
        ProgramUpdate update = ProgramUpdate.create();
        update.setType(ProgramType.CLIP);
        update.setAgeRating(sourceProgram.getAgeRating());

        if (crid != null) {
            update.setCrids(Arrays.asList(crid));
        }


        // Parse title/subtitle from amara (extract series name if there)
        final String[] splitTitle = title.split("//");
        final String newTitle;
        final String newSubTitle;
        if (splitTitle.length > 1) {
            newTitle = splitTitle[0];
            newSubTitle = splitTitle[1];
        } else {
            newTitle = splitTitle[0];
            newSubTitle = null;
        }

        // take translated title (if set, otherwise take from source broadcast)
        TreeSet<TitleUpdate> titleUpdate = new TreeSet<>();

        if (StringUtils.isBlank(newTitle)) {
            log.debug("No title find in translation, using the title of the original broadcast");
            titleUpdate.add(sourceProgram.getTitles().first());
        } else {
            titleUpdate.add(new TitleUpdate(newTitle, TextualType.MAIN));
        }

        if (StringUtils.isNotBlank(newSubTitle)) {
            titleUpdate.add(new TitleUpdate(newSubTitle, TextualType.SUB));
        }

        update.setTitles(titleUpdate);

        // take translated description (if set, otherwise take from source broadcast)
        if (StringUtils.isNotBlank(description)) {
            DescriptionUpdate descriptionUpdate = new DescriptionUpdate(description, TextualType.MAIN);
            update.setDescriptions(descriptionUpdate);
        } else {
            update.setDescriptions(sourceProgram.getDescriptions());
        }

        // set duration
        update.setDuration(sourceProgram.getDuration());

        update.setBroadcasters(sourceProgram.getBroadcasters());
        update.setAVType(sourceProgram.getAVType());

        // set location
        LocationUpdate locationUpdate = new LocationUpdate();
        locationUpdate.setProgramUrl("mid://netinnederland.nl/" + sourcePomsMid);
        locationUpdate.setAvAttributes(new AVAttributesUpdate(AVFileFormat.UNKNOWN, 10000));
        update.setLocations(locationUpdate); // source

        // create relation with source program
        RelationUpdate relationUpdate = RelationUpdate.text(ORIGINAL, sourceProgram.getMid());

        SortedSet<RelationUpdate> relationUpdateSortedSet = update.getRelations();
        relationUpdateSortedSet.add(relationUpdate);

        // determine collection
        String property = "poms.output.collections_mid." + language;
        String netInNlTargetCollection = Config.getRequiredConfig(property);

        // add collection
        SortedSet<MemberRefUpdate> memberRefUpdates = update.getMemberOf();
        MemberRefUpdate memberRefUpdate = new MemberRefUpdate(memberRefUpdates.size() + 1, netInNlTargetCollection);
        memberRefUpdates.add(memberRefUpdate);
        update.setMemberOf(memberRefUpdates);

        // set portal
        update.getPortals().add(PORTAL);
        update.getPortalRestrictions().add(PortalRestrictionUpdate.of(PORTAL));

        // set genres and images
        update.setGenres(sourceProgram.getGenres());
        List<ImageUpdate> images = sourceProgram.getImages();
        images.forEach(iu -> iu.setId(null));
        update.setImages(images);

        // Work around misery with invalid images
        for (ImageUpdate u : images) {
            if (u.getWidth() != null && u.getWidth() <= 0) {
                u.setWidth(null);
            }
            if (u.getHeight() != null && u.getHeight() <= 0) {
                u.setHeight(null);
            }
        }

        JAXB.marshal(update, LoggerOutputStream.info(log));
        // update
        String newPomsMid = client.set(update);

        log.debug("Found new poms mid {} ({} translation for {}}", newPomsMid, language, sourcePomsMid);
        // waiting for it to exists
        Instant start = Instant.now();
        while(Duration.between(start, Instant.now()).compareTo(Duration.ofMinutes(2)) < 0) {
            if (client.get(newPomsMid) != null) {
                break;
            }
            log.info("Didn't find {} yet. Waiting another 10 seconds",  newPomsMid);
            Thread.sleep(10000);;
        }


        return newPomsMid;
    }

}
