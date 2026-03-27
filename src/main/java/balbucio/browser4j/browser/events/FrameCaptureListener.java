package balbucio.browser4j.browser.events;

import java.nio.ByteBuffer;

public interface FrameCaptureListener {
    void onFrame(ByteBuffer buffer, int width, int height);
}
