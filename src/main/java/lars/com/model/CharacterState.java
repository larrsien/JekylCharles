package lars.com.model;

public enum CharacterState {
    IDLE("idle"),
    CURIOUS("curious"),
    DRAGGING("dragging"),
    SLEEPING("sleeping"),
    BUG("bug");

    private final String stateName;

    CharacterState(String stateName) {
        this.stateName = stateName;
    }

    public String getStateName() {
        return stateName;
    }
}
