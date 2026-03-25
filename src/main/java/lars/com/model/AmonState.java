package lars.com.model;

public enum AmonState {
    IDLE("idle"), // 3 штуки
    CURIOUS("curious"), // 5 штук
    DRAGGING("dragging"), // 1 штука
    SLEEPING("sleeping"), // 1 штука
    BUG("bug"); // 3 штуки

    private final String stateName;

    AmonState(String stateName) {
        this.stateName = stateName;
    }

    public String getStateName() {
        return stateName;
    }
}
