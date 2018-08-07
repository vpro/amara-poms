package nl.vpro.amara_poms.poms;

import lombok.extern.slf4j.Slf4j;

import java.util.SortedSet;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import nl.vpro.amara_poms.Config;
import nl.vpro.domain.media.update.MemberRefUpdate;
import nl.vpro.domain.media.update.ProgramUpdate;
import nl.vpro.rs.media.MediaRestClient;

/**
 * @author Michiel Meeuwissen
 * @since 1.0
 */
@Ignore("This is an integration test connecting to an actual server")
@Slf4j
public class IntegratiePomsTest {

    String pomsMidBroadcast = "VPWON_1250959";
    String midCollectionFrom = "POMS_S_VPRO_1416538"; // NetInNederland - te vertalen;

    @Before
    public void init() {
        Config.init();
    }

    @Test
    public void testPomsClipCreate() {

        MediaRestClient client = Config.getPomsClient();
        log.info("Using {}", client);

        String result = PomsClip.create(client, "VPWON_1250959", "en", "serie//test vertaalde titel", "test vertaalde description", "crid://integratiepomstest/" + System.currentTimeMillis());

        log.info(result);
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
        log.info(result);
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

        long duration = pomsBroadcast.getUpdate().getDuration().getSeconds();

        log.info("Duration:" + Long.toString(duration));
        log.info("Image url: " + pomsBroadcast.getThumbNailUrl());
        log.info("Title:" + pomsBroadcast.getTitle());
        log.info("Subtitle:" + pomsBroadcast.getSubTitle());
        log.info("Description:" + pomsBroadcast.getDescription());

    }

}
