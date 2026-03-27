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
import java.nio.ByteBuffer;
import java.awt.Rectangle;
import org.cef.handler.CefRenderHandlerAdapter;
import balbucio.browser4j.browser.events.FrameCaptureListener;
import balbucio.browser4j.browser.input.InputController;
import balbucio.browser4j.core.runtime.BrowserRuntime;
import balbucio.browser4j.security.api.SecurityModuleImpl;
import balbucio.browser4j.security.handlers.PopupAndLifeSpanHandler;
import balbucio.browser4j.security.handlers.DownloadBlockerHandler;
import balbucio.browser4j.observability.MetricsTracker;
import balbucio.browser4j.network.api.NetworkModule;
import balbucio.browser4j.security.api.SecurityModule;

import java.util.ArrayList;
import java.util.List;

public class CefBrowserImpl implements Browser {
    private final CefBrowser cefBrowser;
    private final CefClient cefClient;
    private final List<BrowserEventListener> listeners;
    private final List<FrameCaptureListener> frameListeners;
    private final JSBridge jsBridge;
    private final NetworkHandlerImpl networkHandler;
    private final InputController inputController;
    private final MetricsTracker metricsTracker;
    private final SecurityModuleImpl securityModule;

    public CefBrowserImpl(CefApp cefApp) {
        this.listeners = new ArrayList<>();
        this.frameListeners = new ArrayList<>();

        this.cefClient = cefApp.createClient();

        setupHandlers(this.cefClient);

        this.jsBridge = new JSBridge(this.cefClient, this);
        this.metricsTracker = new MetricsTracker();
        this.securityModule = new SecurityModuleImpl();
        this.networkHandler = new NetworkHandlerImpl(this.cefClient, this.metricsTracker, this.securityModule);

        this.cefClient.addLifeSpanHandler(new PopupAndLifeSpanHandler(this.securityModule));
        this.cefClient.addDownloadHandler(new DownloadBlockerHandler());

        boolean osrEnabled = BrowserRuntime.getConfig().isOsrEnabled();
        this.cefBrowser = cefClient.createBrowser("about:blank", osrEnabled, false);
        this.inputController = new InputController(this.cefBrowser);
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

        client.addRenderHandler(new CefRenderHandlerAdapter() {
            @Override
            public void onPaint(CefBrowser browser, boolean popup, Rectangle[] dirtyRects, ByteBuffer buffer, int width, int height) {
                for (FrameCaptureListener listener : frameListeners) {
                    listener.onFrame(buffer, width, height);
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
    public void addFrameCaptureListener(FrameCaptureListener listener) {
        frameListeners.add(listener);
    }

    @Override
    public void removeFrameCaptureListener(FrameCaptureListener listener) {
        frameListeners.remove(listener);
    }

    @Override
    public void postMessage(String event, Object data) {
        jsBridge.postMessage(event, data);
    }

    @Override
    public NetworkModule network() {
        return networkHandler;
    }

    @Override
    public SecurityModule security() {
        return securityModule;
    }

    @Override
    public Object getNativeBrowser() {
        return cefBrowser;
    }

    @Override
    public InputController getInputController() {
        return inputController;
    }

    public BrowserView getView() {
        return new SwingBrowserView(cefBrowser);
    }
}
