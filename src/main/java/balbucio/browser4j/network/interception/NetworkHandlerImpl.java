package balbucio.browser4j.network.interception;

import org.cef.CefClient;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.handler.CefRequestHandlerAdapter;
import org.cef.network.CefRequest;

import java.util.ArrayList;
import java.util.List;

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
                        // Returning true in onBeforeBrowse cancels the navigation
                        return true; 
                    }
                }
                
                // Allow navigation
                return false; 
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
