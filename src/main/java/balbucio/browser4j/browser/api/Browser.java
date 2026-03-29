package balbucio.browser4j.browser.api;

import balbucio.browser4j.browser.events.BrowserEventListener;

public interface Browser {
    void loadURL(String url);

    /** Loads raw HTML content from a String. Supports large documents. */
    void loadHTML(String html);

    /** Loads raw HTML content read from an InputStream. The stream is fully consumed and closed. */
    void loadHTML(java.io.InputStream htmlStream) throws java.io.IOException;

    /** Loads a local HTML file by path using the native file:// protocol. */
    void loadFile(java.io.File file);

    /** Loads a local HTML file by path using the native file:// protocol. */
    default void loadFile(java.nio.file.Path path) { loadFile(path.toFile()); }

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
    balbucio.browser4j.browser.error.ErrorPageRegistry errors();
    
    java.util.concurrent.CompletableFuture<org.jsoup.nodes.Document> getDOM();
    java.util.concurrent.CompletableFuture<Boolean> isDRMProtected();
    
    void onConsoleMessage(java.util.function.Consumer<String> handler);
    
    // Internal use for bridge registration, can be downcasted or extended
    Object getNativeBrowser();

    balbucio.browser4j.browser.input.InputController getInputController();
}
