package balbucio.browser4j.network.interception;

import balbucio.browser4j.network.api.NetworkModule;
import balbucio.browser4j.network.api.RequestHandler;
import balbucio.browser4j.network.api.ResponseHandler;
import balbucio.browser4j.security.api.SecurityModuleImpl;
import balbucio.browser4j.observability.MetricsTracker;

import org.cef.CefClient;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.handler.*;
import org.cef.network.CefPostData;
import org.cef.network.CefPostDataElement;
import org.cef.network.CefRequest;
import org.cef.network.CefResponse;
import org.cef.network.CefURLRequest;
import org.cef.callback.CefCallback;
import org.cef.misc.IntRef;
import org.cef.misc.StringRef;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NetworkHandlerImpl implements NetworkModule {
    private final List<RequestHandler> requestHandlers = new ArrayList<>();
    private final List<ResponseHandler> responseHandlers = new ArrayList<>();
    private final MetricsTracker metricsTracker;
    private final SecurityModuleImpl securityModule;
    private balbucio.browser4j.cache.interception.CacheInterceptor cacheInterceptor;

    public NetworkHandlerImpl(CefClient client, MetricsTracker metricsTracker, SecurityModuleImpl securityModule) {
        this.metricsTracker = metricsTracker;
        this.securityModule = securityModule;
        
        client.addRequestHandler(new CefRequestHandlerAdapter() {
            @Override
            public boolean onBeforeBrowse(CefBrowser browser, CefFrame frame, CefRequest request, boolean user_gesture, boolean is_redirect) {
                if (securityModule.isUrlBlocked(request.getURL())) {
                    return true; 
                }
                return false; 
            }

            @Override
            public CefResourceRequestHandler getResourceRequestHandler(CefBrowser browser, CefFrame frame, CefRequest request, boolean isNavigation, boolean isDownload, String requestInitiator, org.cef.misc.BoolRef disableDefaultHandling) {
                return new CefResourceRequestHandlerAdapter() {

                    @Override
                    public boolean onBeforeResourceLoad(CefBrowser browser, CefFrame frame, CefRequest request) {
                        metricsTracker.markRequestStart(request.getIdentifier());
                        
                        String url = request.getURL();
                        String method = request.getMethod();

                        for (RequestHandler handler : requestHandlers) {
                            RequestDecision decision = handler.handle(url, method);
                            
                            if (!decision.isAllowed()) return true;
                            
                            if (decision.getModifiedHeaders() != null) {
                                Map<String, String> hdrs = new HashMap<>();
                                request.getHeaderMap(hdrs);
                                hdrs.putAll(decision.getModifiedHeaders());
                                request.setHeaderMap(hdrs);
                            }
                            
                            if (decision.getModifiedBody() != null) {
                                CefPostData postData = CefPostData.create();
                                CefPostDataElement element = CefPostDataElement.create();
                                element.setToBytes(decision.getModifiedBody().length, decision.getModifiedBody());
                                postData.addElement(element);
                                request.setPostData(postData);
                            }
                        }
                        return false;
                    }

                    @Override
                    public boolean onResourceResponse(CefBrowser browser, CefFrame frame, CefRequest request, CefResponse response) {
                        if (frame.isMain()) {
                            try {
                                balbucio.browser4j.core.config.BrowserRuntimeConfiguration config = balbucio.browser4j.core.runtime.BrowserRuntime.getConfig();
                                if (config != null && config.isEnableSecurity()) {
                                    response.setHeaderByName("Content-Security-Policy", "script-src 'self'", true);
                                }
                            } catch (Exception e) {}
                        }
                        if (cacheInterceptor != null) {
                            return cacheInterceptor.onResourceResponse(browser, frame, request, response);
                        }
                        return false;
                    }

                    @Override
                    public CefResourceHandler getResourceHandler(CefBrowser browser, CefFrame frame, CefRequest request) {
                        if (cacheInterceptor != null) {
                            CefResourceHandler handler = cacheInterceptor.getResourceHandler(browser, frame, request);
                            if (handler != null) return handler;
                        }
                        for (RequestHandler handler : requestHandlers) {
                            RequestDecision decision = handler.handle(request.getURL(), request.getMethod());
                            if (decision.getMockedResponse() != null) {
                                return new MockResourceHandler(decision.getMockedResponse());
                            }
                        }
                        return null;
                    }

                    @Override
                    public void onResourceLoadComplete(CefBrowser browser, CefFrame frame, CefRequest request, CefResponse response, CefURLRequest.Status status, long receivedContentLength) {
                        metricsTracker.markRequestEnd(request.getIdentifier(), request.getURL(), response.getStatus(), receivedContentLength);
                    }
                };
            }
        });
    }

    @Override
    public void onRequest(RequestHandler handler) {
        requestHandlers.add(handler);
    }

    @Override
    public void onResponse(ResponseHandler handler) {
        responseHandlers.add(handler);
    }

    @Override
    public void setCacheInterceptor(balbucio.browser4j.cache.interception.CacheInterceptor interceptor) {
        this.cacheInterceptor = interceptor;
    }
}
