package balbucio.browser4j.browser.api;

import balbucio.browser4j.browser.events.BrowserEventListener;

public interface Browser {
    void loadURL(String url);
    void loadHTML(String html);
    void reload();
    void goBack();
    void goForward();
    void close();

    void addEventListener(BrowserEventListener listener);
    void removeEventListener(BrowserEventListener listener);

    void addFrameCaptureListener(balbucio.browser4j.browser.events.FrameCaptureListener listener);
    void removeFrameCaptureListener(balbucio.browser4j.browser.events.FrameCaptureListener listener);

    void postMessage(String event, Object data);
    
    void onRequest(balbucio.browser4j.network.interception.RequestInterceptor interceptor);
    
    // Internal use for bridge registration, can be downcasted or extended
    Object getNativeBrowser();

    balbucio.browser4j.browser.input.InputController getInputController();
}
