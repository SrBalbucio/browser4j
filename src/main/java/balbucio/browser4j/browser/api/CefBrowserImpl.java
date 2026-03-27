package balbucio.browser4j.browser.api;

import balbucio.browser4j.browser.events.BrowserEventListener;
import org.cef.CefApp;
import org.cef.CefClient;
import org.cef.browser.CefBrowser;
import org.cef.handler.CefDisplayHandlerAdapter;
import org.cef.handler.CefLoadHandlerAdapter;
import org.cef.network.CefRequest;
import balbucio.browser4j.bridge.messaging.JSBridge;
import balbucio.browser4j.network.interception.NetworkHandlerImpl;
import balbucio.browser4j.network.interception.RequestInterceptor;
import balbucio.browser4j.ui.abstraction.BrowserView;
import balbucio.browser4j.ui.swing.SwingBrowserView;

import java.util.ArrayList;
import java.util.List;

public class CefBrowserImpl implements Browser {
    private final CefBrowser cefBrowser;
    private final CefClient cefClient;
    private final List<BrowserEventListener> listeners;
    private final JSBridge jsBridge;
    private final NetworkHandlerImpl networkHandler;

    public CefBrowserImpl(CefApp cefApp) {
        this.listeners = new ArrayList<>();
        
        this.cefClient = cefApp.createClient();
        
        setupHandlers(this.cefClient);
        
        this.jsBridge = new JSBridge(this.cefClient, this);
        this.networkHandler = new NetworkHandlerImpl(this.cefClient);
        
        // offscreen rendering = false, transparent = false
        this.cefBrowser = cefClient.createBrowser("about:blank", false, false);
    }
    
    public static Browser create(CefApp cefApp) {
        return new CefBrowserImpl(cefApp);
    }

    private void setupHandlers(CefClient client) {
        client.addLoadHandler(new CefLoadHandlerAdapter() {
            @Override
            public void onLoadStart(CefBrowser browser, org.cef.browser.CefFrame frame, CefRequest.TransitionType transitionType) {
                if (frame.isMain()) {
                    for (BrowserEventListener listener : listeners) {
                        listener.onLoadStart(frame.getURL());
                    }
                }
            }

            @Override
            public void onLoadEnd(CefBrowser browser, org.cef.browser.CefFrame frame, int httpStatusCode) {
                if (frame.isMain()) {
                    for (BrowserEventListener listener : listeners) {
                        listener.onLoadEnd(frame.getURL(), httpStatusCode);
                    }
                }
            }

            @Override
            public void onLoadError(CefBrowser browser, org.cef.browser.CefFrame frame, ErrorCode errorCode, String errorText, String failedUrl) {
                if (frame.isMain()) {
                    for (BrowserEventListener listener : listeners) {
                        listener.onLoadError(failedUrl, errorCode.getCode(), errorText);
                    }
                }
            }
        });

        client.addDisplayHandler(new CefDisplayHandlerAdapter() {
            @Override
            public void onAddressChange(CefBrowser browser, org.cef.browser.CefFrame frame, String url) {
                if (frame.isMain()) {
                    for (BrowserEventListener listener : listeners) {
                        listener.onNavigation(url);
                    }
                }
            }
        });
    }

    @Override
    public void loadURL(String url) {
        cefBrowser.loadURL(url);
    }

    @Override
    public void loadHTML(String html) {
        // A temporary data URL injection since CefBrowser core does not have a "loadHTML" natively taking just string 
        // without URL or base context depending on version, data URI works reliably.
        String dataUrl = "data:text/html;charset=utf-8," + java.net.URLEncoder.encode(html, java.nio.charset.StandardCharsets.UTF_8);
        cefBrowser.loadURL(dataUrl);
    }

    @Override
    public void reload() {
        cefBrowser.reload();
    }

    @Override
    public void goBack() {
        if (cefBrowser.canGoBack()) {
            cefBrowser.goBack();
        }
    }

    @Override
    public void goForward() {
        if (cefBrowser.canGoForward()) {
            cefBrowser.goForward();
        }
    }

    @Override
    public void close() {
        cefBrowser.close(true);
        jsBridge.dispose();
        cefClient.dispose();
    }

    @Override
    public void addEventListener(BrowserEventListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeEventListener(BrowserEventListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void postMessage(String event, Object data) {
        jsBridge.postMessage(event, data);
    }

    @Override
    public void onRequest(RequestInterceptor interceptor) {
        networkHandler.addInterceptor(interceptor);
    }

    @Override
    public Object getNativeBrowser() {
        return cefBrowser;
    }
    
    public BrowserView getView() {
        return new SwingBrowserView(cefBrowser);
    }
}
