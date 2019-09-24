package nl.vpro.amara_poms.poms;

import lombok.extern.slf4j.Slf4j;

import java.util.Iterator;

import nl.vpro.amara_poms.Config;
import nl.vpro.api.client.media.MediaRestClient;
import nl.vpro.domain.media.update.*;


/**
 * @author joost
 */
@Slf4j
public class PomsCollection implements Iterable<MemberUpdate> {
    private final GroupUpdate group;
    private final MediaUpdateList<MemberUpdate> memberUpdateArrayList;

    public PomsCollection(String collectionName) {
        MediaRestClient client = Config.getPomsClient();
        group = client.getGroup(collectionName); // get meta info for collection
        if (group == null) {
            throw new IllegalStateException("The group " + collectionName + " could not be found");
        } else {
            memberUpdateArrayList = client.getGroupMembers(collectionName); // get group numbers
            if (memberUpdateArrayList.getList() != null) {
                log.info("Found {} members to translate", memberUpdateArrayList.size());
            }
        }
    }


    public GroupUpdate getGroup() {
        return group;
    }

    @Override
    public Iterator<MemberUpdate> iterator() {
        return memberUpdateArrayList.iterator();

    }




}
