package balbucio.browser4j.browser.events;

public interface BrowserEventListener {
    void onLoadStart(String url);
    void onLoadEnd(String url, int httpStatusCode);
    void onLoadError(String url, int errorCode, String errorText);
    void onNavigation(String url);
}
