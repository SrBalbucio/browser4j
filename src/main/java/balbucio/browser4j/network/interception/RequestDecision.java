package balbucio.browser4j.network.interception;

public class RequestDecision {
    private final boolean allowed;

    private RequestDecision(boolean allowed) {
        this.allowed = allowed;
    }

    public static RequestDecision allow() {
        return new RequestDecision(true);
    }

    public static RequestDecision block() {
        return new RequestDecision(false);
    }

    public boolean isAllowed() {
        return allowed;
    }
}
