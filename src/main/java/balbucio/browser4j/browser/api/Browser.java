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
    
    balbucio.browser4j.network.api.NetworkModule network();
    balbucio.browser4j.security.api.SecurityModule security();
    balbucio.browser4j.devtools.DevToolsModule devtools();
    balbucio.browser4j.network.cookies.CookieManager cookies();
    balbucio.browser4j.storage.api.StorageModule storage();
    
    java.util.concurrent.CompletableFuture<org.jsoup.nodes.Document> getDOM();
    
    void onConsoleMessage(java.util.function.Consumer<String> handler);
    
    // Internal use for bridge registration, can be downcasted or extended
    Object getNativeBrowser();

    balbucio.browser4j.browser.input.InputController getInputController();
}
