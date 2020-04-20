package nl.vpro.amara_poms.poms;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.Iterator;

import org.checkerframework.checker.nullness.qual.NonNull;

import nl.vpro.amara_poms.Config;
import nl.vpro.api.client.media.MediaRestClient;
import nl.vpro.domain.media.update.*;


/**
 * @author joost
 */
@Slf4j
@ToString
public class PomsCollection implements Iterable<MemberUpdate> {
    private final GroupUpdate group;
    @NonNull
    private final MediaUpdateList<MemberUpdate> memberUpdateList;

    public PomsCollection(String collectionName) {
        MediaRestClient client = Config.getPomsClient();
        group = client.getGroup(collectionName); // get meta info for collection
        if (group == null) {
            throw new IllegalStateException("The group " + collectionName + " could not be found");
        } else {
            memberUpdateList = client.getGroupMembers(collectionName); // get group numbers
            if (memberUpdateList.getList() != null) {
                log.info("Found {} members to translate", memberUpdateList.size());
            }
        }
    }

    public GroupUpdate getGroup() {
        return group;
    }

    @Override
    @NonNull
    public Iterator<MemberUpdate> iterator() {
        return memberUpdateList.iterator();
    }






}
