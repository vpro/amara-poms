package nl.vpro.amara_poms.poms;

import java.util.SortedSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.vpro.amara_poms.Config;
import nl.vpro.domain.media.ProgramType;
import nl.vpro.domain.media.RelationDefinition;
import nl.vpro.domain.media.exceptions.ModificationException;
import nl.vpro.domain.media.update.MemberRefUpdate;
import nl.vpro.domain.media.update.ProgramUpdate;
import nl.vpro.domain.media.update.RelationUpdate;
import nl.vpro.rs.media.MediaRestClient;

/**
 * @author Joost Aafjes
 */
public class PomsClip {

    private static final Logger LOG = LoggerFactory.getLogger(PomsClip.class);


    public static final RelationDefinition ORIGINAL = RelationDefinition.of("TRANSLATION_SOURCE", "VPRO");
    public static final String PORTAL = "WOORD"; // "NETINNL" was requested


    public static String create(MediaRestClient client, String sourcePomsMid, String language) {
        // get source broadcast
        ProgramUpdate sourceProgram = ProgramUpdate.forAllOwners(client.getFullProgram(sourcePomsMid));
        //  full program
        //ProgramUpdate sourceProgram = client.getProgram(sourcePomsMid);

        //JAXB.marshal(sourceProgram, System.out);
        // todo - error handling

        // construct new CLIP
        ProgramUpdate update = ProgramUpdate.create(ProgramType.CLIP);

        // copy fields from source
        update.setTitles(sourceProgram.getTitles());
        update.setDescriptions(sourceProgram.getDescriptions());
        try {
            update.setDuration(sourceProgram.getDuration());
        } catch (ModificationException e) {
            // ignore
        }
        update.setBroadcasters(sourceProgram.getBroadcasters());
        update.setAVType(sourceProgram.getAVType());
        update.setLocations(client.cloneLocations(sourcePomsMid)); // source

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

        // todo enable when available
//        update.getPortals().add(PORTAL);
//        update.getPortalRestrictions().add(PortalRestrictionUpdate.of(PORTAL));


        update.setImages(sourceProgram.getImages());

        //JAXB.marshal(update, System.out);
        // update
        String newPomsMid = client.set(update);

        return newPomsMid;
    }

}
