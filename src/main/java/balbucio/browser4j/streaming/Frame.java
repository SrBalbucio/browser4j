package balbucio.browser4j.streaming;

import java.nio.ByteBuffer;

public class Frame {
    private final ByteBuffer buffer;
    private final int width;
    private final int height;
    private final long captureTimestampMs;

    public Frame(ByteBuffer buffer, int width, int height, long captureTimestampMs) {
        this.buffer = buffer;
        this.width = width;
        this.height = height;
        this.captureTimestampMs = captureTimestampMs;
    }

    public ByteBuffer getBuffer() { return buffer; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public long getCaptureTimestampMs() { return captureTimestampMs; }
}
