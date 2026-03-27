package balbucio.browser4j.network.interception;

import org.cef.CefClient;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.handler.CefRequestHandlerAdapter;
import org.cef.handler.CefResourceRequestHandler;
import org.cef.handler.CefResourceRequestHandlerAdapter;
import org.cef.network.CefPostData;
import org.cef.network.CefPostDataElement;
import org.cef.network.CefRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NetworkHandlerImpl {
    private final List<RequestInterceptor> interceptors = new ArrayList<>();

    public NetworkHandlerImpl(CefClient client) {
        client.addRequestHandler(new CefRequestHandlerAdapter() {
            @Override
            public boolean onBeforeBrowse(CefBrowser browser, CefFrame frame, CefRequest request, boolean user_gesture, boolean is_redirect) {
                String url = request.getURL();
                String method = request.getMethod();

                for (RequestInterceptor interceptor : interceptors) {
                    RequestDecision decision = interceptor.intercept(url, method);
                    if (!decision.isAllowed()) {
                        return true; 
                    }
                }
                
                return false; 
            }

            @Override
            public CefResourceRequestHandler getResourceRequestHandler(CefBrowser browser, CefFrame frame, CefRequest request, boolean isNavigation, boolean isDownload, String requestInitiator, org.cef.misc.BoolRef disableDefaultHandling) {
                return new CefResourceRequestHandlerAdapter() {
                    @Override
                    public boolean onBeforeResourceLoad(CefBrowser browser, CefFrame frame, CefRequest request) {
                        String url = request.getURL();
                        String method = request.getMethod();

                        for (RequestInterceptor interceptor : interceptors) {
                            RequestDecision decision = interceptor.intercept(url, method);
                            
                            if (!decision.isAllowed()) {
                                return true;
                            }
                            
                            if (decision.getModifiedHeaders() != null) {
                                Map<String, String> currentHeaders = new HashMap<>();
                                request.getHeaderMap(currentHeaders);
                                currentHeaders.putAll(decision.getModifiedHeaders());
                                request.setHeaderMap(currentHeaders);
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
                };
            }
        });
    }

    public void addInterceptor(RequestInterceptor interceptor) {
        this.interceptors.add(interceptor);
    }

    public void removeInterceptor(RequestInterceptor interceptor) {
        this.interceptors.remove(interceptor);
    }
}
