package nl.vpro.amara_poms.poms;

import java.io.IOException;
import java.util.SortedSet;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import nl.vpro.amara_poms.Config;
import nl.vpro.domain.media.update.MemberRefUpdate;
import nl.vpro.domain.media.update.ProgramUpdate;
import nl.vpro.rs.media.MediaRestClient;

import static junit.framework.Assert.assertTrue;

/**
 * @author Michiel Meeuwissen
 * @since 1.0
 */
@Ignore("This is an integration test connecting to an actual server")
public class IntegratiePomsTest {

    String pomsMidBroadcast = "VPWON_1250959";
    String midCollectionFrom = "POMS_S_VPRO_1416538"; // NetInNederland - te vertalen;

    @Before
    public void init() {
        Config.init();
    }

    @Test
    public void testPomsClipCreate() throws IOException {

        MediaRestClient client = Config.getPomsClient();

        String result = "";
        try {
            result = PomsClip.create(client, "VPWON_1250959", "en", "serie//test vertaalde titel", "test vertaalde description");
        } catch (Exception exception) {
            assertTrue(exception.toString(), false);
        }

        System.out.println(result);
    }

    @Test
    public void testAddProgramToCollection() {

        MediaRestClient client = Config.getPomsClient();

        ProgramUpdate update = client.getProgram(pomsMidBroadcast); // test with this one

        SortedSet<MemberRefUpdate> memberUpdate = update.getMemberOf();

        // add collection
        MemberRefUpdate memberRefUpdate = new MemberRefUpdate(0, midCollectionFrom);
        memberUpdate.add(memberRefUpdate);

        // update
//        update.setMemberOf(memberUpdate);
        String result = client.set(update);
        System.out.println(result);
    }

    @Test
    public void removeFromTeVertalenToNetInNL() {

        PomsBroadcast pomsBroadcast = new PomsBroadcast(pomsMidBroadcast, Config.getPomsClient().getFullProgram(pomsMidBroadcast));

        pomsBroadcast.removeFromCollection(midCollectionFrom);

    }

    @Test
    public void extractImageId() {

        String pomsMid = "VPWON_1249693";
        PomsBroadcast pomsBroadcast = new PomsBroadcast(pomsMid, Config.getPomsClient().getFullProgram(pomsMidBroadcast));

        long duration = pomsBroadcast.getProgramUpdate().getDuration().getSeconds();

        System.out.println("Duration:" + Long.toString(duration));
        System.out.println("Image url: " + pomsBroadcast.getThumbNailUrl());
        System.out.println("Title:" + pomsBroadcast.getTitle());
        System.out.println("Subtitle:" + pomsBroadcast.getSubTitle());
        System.out.println("Description:" + pomsBroadcast.getDescription());

    }

}
