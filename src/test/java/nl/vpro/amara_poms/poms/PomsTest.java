package nl.vpro.amara_poms.poms;

import nl.vpro.amara_poms.Config;
import nl.vpro.domain.media.AVType;
import nl.vpro.domain.media.Location;
import nl.vpro.domain.media.ProgramType;
import nl.vpro.domain.media.exceptions.ModificationException;
import nl.vpro.domain.media.update.*;
import nl.vpro.rs.media.MediaRestClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.JAXB;
import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;

import static junit.framework.TestCase.assertNotNull;
import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Created by joost on 10/04/16.
 */
public class PomsTest {

    private MediaRestClient client;

    @Before
    public void setUp() {
        client = new MediaRestClient();
        client.setTrustAll(true);
        client.setUserName("vpro-mediatools");
        client.setPassword("Id7shuu7");
        //client.setUrl("http://localhost:8071/rs/");
        client.setUrl("https://api-test.poms.omroep.nl/");
//        client.setUrl("https://api-dev.poms.omroep.nl/");
        client.setThrottleRate(50);
        client.setWaitForRetry(true);
        client.setErrors("joostaafjes@gmail.com");

        Config.init();

    }

    @Test
    public void prepareTestingAdd() {
        ProgramUpdate update = client.getProgram("NPS_1207292"); // test with this one
        String midCollectionFrom = "POMS_S_VPRO_1416538"; // NetInNederland - te vertalen

        SortedSet<MemberRefUpdate> memberUpdate = update.getMemberOf();

        // add collection
        MemberRefUpdate memberRefUpdate = new MemberRefUpdate(0, midCollectionFrom);
        memberUpdate.add(memberRefUpdate);

        // update
        update.setMemberOf(memberUpdate);
        String result = client.set(update);
        System.out.println(result);
    }

    @Test
    public void moveFromTeVertalenToNetInNL() {
        ProgramUpdate update = client.getProgram("NPS_1207292"); // test with this one
        String midCollectionFrom = "POMS_S_VPRO_1416538"; // NetInNederland - te vertalen
        String midCollectionTo = "POMS_S_VPRO_1414788"; // NetInNederland

        SortedSet<MemberRefUpdate> memberUpdate = update.getMemberOf();
        memberUpdate.removeIf(member -> member.getMediaRef().equals(midCollectionFrom));

        // add collection
        MemberRefUpdate memberRefUpdate = new MemberRefUpdate(0, midCollectionTo);
        memberUpdate.add(memberRefUpdate);

        // update
        update.setMemberOf(memberUpdate);
        String result = client.set(update);
        System.out.println(result);
    }

//    @After
    @Test
    public void removeFromNetInNL() {
        ProgramUpdate update = client.getProgram("NPS_1207292"); // test with this one
        String midCollection = "POMS_S_VPRO_1414788"; // NetInNederland

        SortedSet<MemberRefUpdate> memberUpdate = update.getMemberOf();
        memberUpdate.removeIf(member -> member.getMediaRef().equals(midCollection));

        // update
        update.setMemberOf(memberUpdate);
        String result = client.set(update);
        System.out.println(result);
    }


    @Test
    public void testNewClipFromFileWithUpdates() {
        String sourceMid = "VPRO_1131866";
                        // = "VPRO_1135816"; // -> 1 bron is verplaatst
                // "POMS_VPRO_158078"; -> 1 bron wordt verplaatst
        ProgramUpdate sourceProgram = client.get(sourceMid);

//        ProgramUpdate newProgram = JAXB.unmarshal(getClass().getResourceAsStream("/POMS_VPRO_CLIP.xml"), ProgramUpdate.class);
//        client.setLookupCrid(false);
        ProgramUpdate newProgram = new ProgramUpdate();
        newProgram.setType(ProgramType.CLIP);
        newProgram.setAVType(AVType.VIDEO);
        newProgram.setBroadcasters(Arrays.asList("VPRO"));
        // copy from source
        newProgram.setTitles(sourceProgram.getTitles());
        newProgram.setDescriptions(sourceProgram.getDescriptions());
        try {
            newProgram.setDuration(sourceProgram.getDuration());
        } catch (ModificationException e) {
            e.printStackTrace();
        }

        // location
//        sourceProgram.getLocations().forEach(l -> {l.setUrn(null);});
        newProgram.setLocations(client.cloneLocations(sourceMid));

        System.out.println(sourceProgram.getLocations().size());

        newProgram.setBroadcasters(sourceProgram.getBroadcasters());
        newProgram.setAVType(sourceProgram.getAVType());
        newProgram.setImages(sourceProgram.getImages());
        newProgram.setGenres(sourceProgram.getGenres());
        newProgram.setWebsites(sourceProgram.getWebsites());

        // create relation with source program
        RelationUpdate relationUpdate = new RelationUpdate("ARTIST", "VPRO", null, sourceProgram.getMid());
        SortedSet<RelationUpdate> relationUpdateSortedSet = newProgram.getRelations();
        relationUpdateSortedSet.add(relationUpdate);
//        newProgram.setRelations(relationUpdateSortedSet);

        // add collection
        String netInNlEngelsCollection = "POMS_S_VPRO_1409252";
        SortedSet<MemberRefUpdate> memberRefUpdates = newProgram.getMemberOf();
        MemberRefUpdate memberRefUpdate = new MemberRefUpdate(memberRefUpdates.size(), netInNlEngelsCollection);
        memberRefUpdates.add(memberRefUpdate);
        newProgram.setMemberOf(memberRefUpdates);

//        JAXB.marshal(newProgram, System.out);

        // update
        String result2 = client.set(newProgram);
        System.out.println(result2);

//        assertThat(result2).startsWith("POMS_VPRO_");
    }

    @Test
    public void sourceCount() {
        // String pomsMid = "VPRO_1142324"; // volgens POMS test 6 bronnen
        // String pomsMid = "POW_00625459"; // volgens POMS test 4 bronnen
//        String pomsMid = "RBX_BNN_781416"; // volgens POMS test 1 bronnen
//String pomsMid = "VPWON_1227733"; // volgens POMS test 5 bronnen
        String pomsMid = "VPRO_1122474"; // volgens POMS test 5 bronnen


        ProgramUpdate program = client.get(pomsMid);

        System.out.println(program.getLocations().size());
//        JAXB.marshal(program, System.out);
    }

    @Test
    public void extractImageId() {
        String pomsMid = "VPRO_1122474";
        PomsBroadcast pomsBroadcast = new PomsBroadcast(pomsMid);
        pomsBroadcast.programUpdate = client.get(pomsMid); // not so nice way to test this function

        String imageId = pomsBroadcast.getImageId();
        long duration = pomsBroadcast.getProgramUpdate().getDuration().getSeconds();

        System.out.println("Duration:" + Long.toString(duration));

        assertNotNull(imageId);



    }

}
