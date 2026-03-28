package balbucio.browser4j.bridge.messaging;

import balbucio.browser4j.browser.api.Browser;
import balbucio.browser4j.bridge.serialization.JsonSerializer;
import org.cef.CefClient;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.browser.CefMessageRouter;
import org.cef.callback.CefQueryCallback;
import org.cef.handler.CefMessageRouterHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class JSBridge {
    private static final Logger log = LoggerFactory.getLogger(JSBridge.class);
    private final Browser javaBrowser;
    private final CefMessageRouter msgRouter;
    private final List<MessageHandler> handlers = new ArrayList<>();
    private final JsonSerializer serializer = new JsonSerializer();

    public JSBridge(CefClient client, Browser javaBrowser) {
        this.javaBrowser = javaBrowser;
        
        // Define default window.bridge bindings
        CefMessageRouter.CefMessageRouterConfig config = new CefMessageRouter.CefMessageRouterConfig("bridge", "bridgeCancel");
        this.msgRouter = CefMessageRouter.create(config);
        
        msgRouter.addHandler(new CefMessageRouterHandlerAdapter() {
            @Override
            public boolean onQuery(CefBrowser browser, CefFrame frame, long queryId, String request, boolean persistent, CefQueryCallback callback) {
                try {
                    // Expecting JSON shape: { "event": "...", "data": ... }
                    JSMessage msg = serializer.deserialize(request, JSMessage.class);
                    if (msg != null && msg.event != null) {
                        for (MessageHandler h : handlers) {
                            h.onMessage(msg.event, msg.data);
                        }
                    }
                    callback.success("OK");
                } catch (Exception e) {
                    log.error("Failed handling JS query: " + request, e);
                    callback.failure(500, e.getMessage());
                }
                return true;
            }
        }, true);
        
        client.addMessageRouter(msgRouter);
    }
    
    public void addHandler(MessageHandler handler) {
        this.handlers.add(handler);
    }

    public void removeHandler(MessageHandler handler) {
        this.handlers.remove(handler);
    }

    public void postMessage(String event, Object data) {
        try {
            String jsonPayload = serializer.serialize(new JSMessage(event, data));
            String jsCode = String.format("window.dispatchEvent(new CustomEvent('java-message', { detail: %s }));", jsonPayload);
            CefBrowser cefBrowser = (CefBrowser) javaBrowser.getNativeBrowser();
            if (cefBrowser != null) {
                cefBrowser.executeJavaScript(jsCode, cefBrowser.getURL(), 0);
            }
        } catch (Exception e) {
            log.error("Failed to post message to JS", e);
        }
    }

    public void dispose() {
        if (msgRouter != null) {
            msgRouter.dispose();
        }
    }

    // DTO for serialization match
    private static class JSMessage {
        public String event;
        public Object data;
        
        public JSMessage() {}
        public JSMessage(String event, Object data) {
            this.event = event;
            this.data = data;
        }
    }
}
