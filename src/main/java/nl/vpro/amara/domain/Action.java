package nl.vpro.amara.domain;

/**
 * @author Michiel Meeuwissen
 * @since 1.2
 */
public class Action {

    private String action;
    private String label;
    private Boolean complete;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Boolean getComplete() {
        return complete;
    }

    public void setComplete(Boolean complete) {
        this.complete = complete;
    }

    @Override
    public String toString() {
        return action + ":" + label + ":" + complete;

    }
}
