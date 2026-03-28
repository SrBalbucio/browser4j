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
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class JSBridge {
    private static final Logger log = LoggerFactory.getLogger(JSBridge.class);
    private final Browser javaBrowser;
    private final CefMessageRouter msgRouter;
    private final List<MessageHandler> handlers = new ArrayList<>();
    private final JsonSerializer serializer = new JsonSerializer();
    private final Map<String, CompletableFuture<Object>> pendingEvaluations = new ConcurrentHashMap<>();

    public JSBridge(CefClient client, Browser javaBrowser) {
        this.javaBrowser = javaBrowser;
        
        // Internal handler for async js evaluation results
        this.addHandler((event, data) -> {
            if ("__bridge_eval_success".equals(event)) {
                if (data instanceof Map) {
                    Map<?, ?> map = (Map<?, ?>) data;
                    String id = (String) map.get("id");
                    if (id != null) {
                        CompletableFuture<Object> future = pendingEvaluations.remove(id);
                        if (future != null) {
                            future.complete(map.get("result"));
                        }
                    }
                }
            } else if ("__bridge_eval_error".equals(event)) {
                if (data instanceof Map) {
                    Map<?, ?> map = (Map<?, ?>) data;
                    String id = (String) map.get("id");
                    if (id != null) {
                        CompletableFuture<Object> future = pendingEvaluations.remove(id);
                        if (future != null) {
                            future.completeExceptionally(new Exception(String.valueOf(map.get("error"))));
                        }
                    }
                }
            }
        });

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

    public CompletableFuture<Object> evaluateJavaScript(String jsCode) {
        CompletableFuture<Object> future = new CompletableFuture<>();
        String callbackId = java.util.UUID.randomUUID().toString();
        pendingEvaluations.put(callbackId, future);

        String wrappedCode = "(async function() {\n" +
                "    try {\n" +
                "        let __res = await (async function() {\n" +
                "            " + jsCode + "\n" +
                "        })();\n" +
                "        window.bridge({request: JSON.stringify({event: '__bridge_eval_success', data: {id: '" + callbackId + "', result: __res}}), onSuccess: function(){}, onFailure: function(){}});\n" +
                "    } catch(e) {\n" +
                "        window.bridge({request: JSON.stringify({event: '__bridge_eval_error', data: {id: '" + callbackId + "', error: String(e)}}), onSuccess: function(){}, onFailure: function(){}});\n" +
                "    }\n" +
                "})();";

        CefBrowser cefBrowser = (CefBrowser) javaBrowser.getNativeBrowser();
        if (cefBrowser != null) {
            cefBrowser.executeJavaScript(wrappedCode, cefBrowser.getURL(), 0);
        } else {
            future.completeExceptionally(new Exception("Native browser not available"));
        }

        return future;
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
