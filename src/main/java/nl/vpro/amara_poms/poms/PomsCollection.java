package nl.vpro.amara_poms.poms;

import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.vpro.amara_poms.Config;
import nl.vpro.domain.media.update.GroupUpdate;
import nl.vpro.domain.media.update.MemberUpdate;
import nl.vpro.rs.media.MediaRestClient;


/**
 * @author joost
 */
public class PomsCollection implements Iterable<MemberUpdate> {

    private static final Logger LOG = LoggerFactory.getLogger(PomsCollection.class);
    static final int ERROR_COLLECTION_NOT_FOUND = 1;
    static final int ERROR_BROADCAST_NOT_FOUND = 3;

    private String collectionName;

    private GroupUpdate group;

    private Iterable<MemberUpdate> memberUpdateArrayList;
    private final int errorCode;

    public PomsCollection(String collectionName) {
        this.collectionName = collectionName;
        this.errorCode = getBroadcastsFromPOMS();
    }


    public GroupUpdate getGroup() {
        return group;
    }

    @Override
    public Iterator<MemberUpdate> iterator() {
        return memberUpdateArrayList.iterator();

    }

    public int getErrorCode() {
        return errorCode;
    }

    /**
     * Get collection from POMS
     *
     * @return 0 if found, error code != 0  if not found
     */
    private int getBroadcastsFromPOMS() {
        int returnValue = 0;
        MediaRestClient client = Config.getPomsClient();

        group = client.getGroup(collectionName); // get meta info for collection

        if (group == null) {
            returnValue = ERROR_COLLECTION_NOT_FOUND;
        } else {
            memberUpdateArrayList = client.getGroupMembers(collectionName); // get group numbers
        }

        return returnValue;
    }




}
