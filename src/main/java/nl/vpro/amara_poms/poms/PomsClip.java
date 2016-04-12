package nl.vpro.amara_poms.poms;

import nl.vpro.amara_poms.Config;
import nl.vpro.domain.media.exceptions.ModificationException;
import nl.vpro.domain.media.update.MemberRefUpdate;
import nl.vpro.domain.media.update.ProgramUpdate;
import nl.vpro.domain.media.update.RelationUpdate;
import nl.vpro.rs.media.MediaRestClient;

import javax.xml.bind.JAXB;
import java.util.SortedSet;

/**
 * Created by joost on 11/04/16.
 */
public class PomsClip {


    public static String create(String sourcePomsMid, String language) {
        // get source broadcast
        ProgramUpdate sourceProgram = Utils.getClient().getProgram(sourcePomsMid);
        // todo - error handling

        // construct new CLIP
        ProgramUpdate update = JAXB.unmarshal(PomsClip.class.getResourceAsStream("/POMS_VPRO_CLIP.xml"), ProgramUpdate.class);

        // copy fields from source
        update.setTitles(sourceProgram.getTitles());
        update.setDescriptions(sourceProgram.getDescriptions());
        try {
            update.setDuration(sourceProgram.getDuration());
        } catch (ModificationException e) {
            e.printStackTrace();
        }
        update.setBroadcasters(sourceProgram.getBroadcasters());
        update.setAVType(sourceProgram.getAVType());
        update.setLocations(Utils.getClient().cloneLocations(sourcePomsMid)); // source

        // create relation with source program
        RelationUpdate relationUpdate = new RelationUpdate(Config.getRequiredConfig("poms.out.relation.type"),
                                                           Config.getRequiredConfig("poms.out.relation.broadcaster"),
                                                           null, sourceProgram.getMid());
        SortedSet<RelationUpdate> relationUpdateSortedSet = update.getRelations();
        relationUpdateSortedSet.add(relationUpdate);
        update.setRelations(relationUpdateSortedSet);

        // determine collection
        String property = "poms.output.collections_mid." + language;
        String netInNlTargetCollection = Config.getRequiredConfig(property);

        // add collection
        SortedSet<MemberRefUpdate> memberRefUpdates = update.getMemberOf();
        MemberRefUpdate memberRefUpdate = new MemberRefUpdate(memberRefUpdates.size(), netInNlTargetCollection);
        memberRefUpdates.add(memberRefUpdate);
        update.setMemberOf(memberRefUpdates);

        // update
        MediaRestClient client = Utils.getClient();
        client.setLookupCrid(false);

        String newPomsMid = client.set(update);

        return newPomsMid;
    }

}
