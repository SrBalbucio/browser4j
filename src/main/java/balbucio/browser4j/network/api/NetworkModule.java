package balbucio.browser4j.network.api;

public interface NetworkModule {
    void onRequest(RequestHandler handler);
    void onResponse(ResponseHandler handler);
    void setCacheInterceptor(balbucio.browser4j.cache.interception.CacheInterceptor interceptor);
}
