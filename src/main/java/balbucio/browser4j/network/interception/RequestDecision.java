package balbucio.browser4j.network.interception;

import java.util.Map;

public class RequestDecision {
    private final boolean allowed;
    private final Map<String, String> modifiedHeaders;
    private final byte[] modifiedBody;

    private RequestDecision(boolean allowed, Map<String, String> modifiedHeaders, byte[] modifiedBody) {
        this.allowed = allowed;
        this.modifiedHeaders = modifiedHeaders;
        this.modifiedBody = modifiedBody;
    }

    public static RequestDecision allow() {
        return new RequestDecision(true, null, null);
    }

    public static RequestDecision block() {
        return new RequestDecision(false, null, null);
    }

    public static RequestDecision modify(Map<String, String> headers, byte[] body) {
        return new RequestDecision(true, headers, body);
    }

    public boolean isAllowed() {
        return allowed;
    }

    public Map<String, String> getModifiedHeaders() {
        return modifiedHeaders;
    }

    public byte[] getModifiedBody() {
        return modifiedBody;
    }
}

