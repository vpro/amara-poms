package nl.vpro.amara_poms.poms;

import java.util.SortedSet;

import javax.xml.bind.JAXB;

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

    public static final RelationDefinition ORIGINAL = RelationDefinition.of("ARTIST", "VPRO");


    public static String create(MediaRestClient client, String sourcePomsMid, String language) {
        // get source broadcast
        ProgramUpdate sourceProgram = client.getProgram(sourcePomsMid);
        // todo - error handling

        // construct new CLIP
        ProgramUpdate update = ProgramUpdate.create();
        update.setType(ProgramType.CLIP);

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

        JAXB.marshal(update, System.out);
        // update
        String newPomsMid = client.set(update);

        return newPomsMid;
    }

}
