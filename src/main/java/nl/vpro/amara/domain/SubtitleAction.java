package nl.vpro.amara.domain;

/**
 * @author joost
 */
public class SubtitleAction {

    final public static String ACTION_COMPLETE = "complete";
    final public static String ACTION_APPROVE = "approve";

    public String action; // only used to approve nl subtitle

    public SubtitleAction() {
    }

    public SubtitleAction(String action) {
        this.action = action;
    }

}
