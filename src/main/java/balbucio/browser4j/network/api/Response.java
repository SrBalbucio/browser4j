package balbucio.browser4j.network.api;

public class Response {
    private final int statusCode;
    private final String contentType;
    private final byte[] body;

    private Response(int statusCode, String contentType, byte[] body) {
        this.statusCode = statusCode;
        this.contentType = contentType;
        this.body = body;
    }

    public static Response mock(int statusCode, String contentType, byte[] body) {
        return new Response(statusCode, contentType, body);
    }

    public int getStatusCode() { return statusCode; }
    public String getContentType() { return contentType; }
    public byte[] getBody() { return body; }
}
