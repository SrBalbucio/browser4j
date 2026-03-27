package balbucio.browser4j.observability;

import balbucio.browser4j.core.config.BrowserRuntimeConfiguration;
import balbucio.browser4j.core.runtime.BrowserRuntime;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class MetricsTracker {
    private final ConcurrentHashMap<Long, Long> requestStartTimes = new ConcurrentHashMap<>();
    private final AtomicLong totalRequests = new AtomicLong(0);

    public void markRequestStart(long identifier) {
        requestStartTimes.put(identifier, System.currentTimeMillis());
        totalRequests.incrementAndGet();
    }

    public void markRequestEnd(long identifier, String url, int statusCode, long bytesLoaded) {
        Long startTime = requestStartTimes.remove(identifier);
        if (startTime != null) {
            long duration = System.currentTimeMillis() - startTime;
            
            try {
                BrowserRuntimeConfiguration config = BrowserRuntime.getConfig();
                if (config != null && config.getMetricHandler() != null) {
                    config.getMetricHandler().accept(new BrowserMetric(url, duration, statusCode, bytesLoaded));
                }
            } catch (IllegalStateException e) {
                // Runtime not initialized yet
            }
        }
    }

    public long getTotalRequests() {
        return totalRequests.get();
    }
}
