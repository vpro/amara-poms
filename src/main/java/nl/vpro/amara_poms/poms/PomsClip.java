package nl.vpro.amara_poms.poms;

import java.util.SortedSet;
import java.util.TreeSet;

import nl.vpro.domain.media.AVFileFormat;
import nl.vpro.domain.media.support.TextualType;
import nl.vpro.domain.media.update.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.vpro.amara_poms.Config;
import nl.vpro.domain.media.ProgramType;
import nl.vpro.domain.media.RelationDefinition;
import nl.vpro.domain.media.exceptions.ModificationException;
import nl.vpro.domain.media.support.TextualType;
import nl.vpro.domain.media.update.*;
import nl.vpro.rs.media.MediaRestClient;

/**
 * @author Joost Aafjes
 */
public class PomsClip {

    private static final Logger logger = LoggerFactory.getLogger(PomsClip.class);


    public static final RelationDefinition ORIGINAL = RelationDefinition.of("TRANSLATION_SOURCE", "VPRO");
    public static final String PORTAL = "NETINNL";

    /**
     * Create clip in poms
     * @param client
     * @param sourcePomsMid - source broadcast
     * @param language
     * @param title - title from Amara (serie // translated titel or translated title)
     * @param description - translated description from Amara
     * @return
     */
    public static String create(MediaRestClient client, String sourcePomsMid, String language, String title, String description) {

        // get source broadcast
        ProgramUpdate sourceProgram = ProgramUpdate.forAllOwners(client.getFullProgram(sourcePomsMid));
        //  full program
        //ProgramUpdate sourceProgram = client.getProgram(sourcePomsMid);

        //JAXB.marshal(sourceProgram, System.out);
        // todo - error handling

        // construct new CLIP
        ProgramUpdate update = ProgramUpdate.create(ProgramType.CLIP);

        SortedSet<TitleUpdate> titleUpdate2 = sourceProgram.getTitles();

        // sub title (extract series name if there)
        String[] splitTitle = title.split("//");
        String newTitle = "";
        String newSubTitle = "";
        if (splitTitle.length > 1) {
            newTitle = splitTitle[0];
            newSubTitle = splitTitle[1];
        } else {
            newTitle = splitTitle[0];
        }

        TreeSet<TitleUpdate> titleUpdate = new TreeSet<>();
        titleUpdate.add(new TitleUpdate(newTitle, TextualType.MAIN));
        titleUpdate.add(new TitleUpdate(newSubTitle, TextualType.SUB));
        update.setTitles(titleUpdate);

        // take translated description
        if (description != null && !description.equals("")) {
            DescriptionUpdate descriptionUpdate = new DescriptionUpdate(description, TextualType.MAIN);
            update.setDescriptions(descriptionUpdate);
        }
        try {
            update.setDuration(sourceProgram.getDuration());
        } catch (ModificationException e) {
            // ignore
            logger.error("Error setting duration for source POM Mid " + sourcePomsMid);

        }
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
        MemberRefUpdate memberRefUpdate = new MemberRefUpdate(memberRefUpdates.size(), netInNlTargetCollection);
        memberRefUpdates.add(memberRefUpdate);
        update.setMemberOf(memberRefUpdates);

        // set portal
        update.getPortals().add(PORTAL);
        update.getPortalRestrictions().add(PortalRestrictionUpdate.of(PORTAL));

        // set genres
        update.setGenres(sourceProgram.getGenres());

        for (ImageUpdate u : sourceProgram.getImages()) {
            // work around bug.
            u.setOffset(null);
        }
        update.setImages(sourceProgram.getImages());

        //JAXB.marshal(update, System.out);
        // update
        String newPomsMid = client.set(update);

        return newPomsMid;
    }

}
