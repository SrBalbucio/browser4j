package balbucio.browser4j.core.config;

import java.io.File;
import java.util.function.Consumer;
import balbucio.browser4j.observability.BrowserMetric;

public class BrowserRuntimeConfiguration {

    private final String cachePath;
    private final String userDataPath;
    private final boolean enableGPU;
    private final boolean enableSandbox;
    private final boolean osrEnabled;
    private final int windowlessFrameRate;
    private final boolean enableNetworkInterception;
    private final boolean enableSecurity;
    private final Consumer<BrowserMetric> metricHandler;
    private final int remoteDebuggingPort;
    private final String userAgent;
    private final balbucio.browser4j.network.proxy.ProxyConfig proxy;

    private BrowserRuntimeConfiguration(Builder builder) {
        this.cachePath = builder.cachePath;
        this.userDataPath = builder.userDataPath;
        this.enableGPU = builder.enableGPU;
        this.enableSandbox = builder.enableSandbox;
        this.osrEnabled = builder.osrEnabled;
        this.windowlessFrameRate = builder.windowlessFrameRate;
        this.enableNetworkInterception = builder.enableNetworkInterception;
        this.enableSecurity = builder.enableSecurity;
        this.metricHandler = builder.metricHandler;
        this.remoteDebuggingPort = builder.remoteDebuggingPort;
        this.userAgent = builder.userAgent;
        this.proxy = builder.proxy;
    }

    public String getCachePath() { return cachePath; }
    public String getUserDataPath() { return userDataPath; }
    public boolean isEnableGPU() { return enableGPU; }
    public boolean isEnableSandbox() { return enableSandbox; }
    public boolean isOsrEnabled() { return osrEnabled; }
    public int getWindowlessFrameRate() { return windowlessFrameRate; }
    public boolean isEnableNetworkInterception() { return enableNetworkInterception; }
    public boolean isEnableSecurity() { return enableSecurity; }
    public Consumer<BrowserMetric> getMetricHandler() { return metricHandler; }
    public int getRemoteDebuggingPort() { return remoteDebuggingPort; }
    public String getUserAgent() { return userAgent; }
    public balbucio.browser4j.network.proxy.ProxyConfig getProxy() { return proxy; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String cachePath = new File(".cache").getAbsolutePath();
        private String userDataPath = new File(".userdata").getAbsolutePath();
        private boolean enableGPU = true;
        private boolean enableSandbox = false;
        private boolean osrEnabled = false;
        private int windowlessFrameRate = 60;
        private boolean enableNetworkInterception = true;
        private boolean enableSecurity = true;
        private Consumer<BrowserMetric> metricHandler = null;
        private int remoteDebuggingPort = 9222;
        private String userAgent = null;
        private balbucio.browser4j.network.proxy.ProxyConfig proxy = null;

        public Builder cachePath(String cachePath) {
            this.cachePath = cachePath;
            return this;
        }

        public Builder userDataPath(String userDataPath) {
            this.userDataPath = userDataPath;
            return this;
        }

        public Builder enableGPU(boolean enableGPU) {
            this.enableGPU = enableGPU;
            return this;
        }

        public Builder enableSandbox(boolean enableSandbox) {
            this.enableSandbox = enableSandbox;
            return this;
        }
        
        public Builder osrEnabled(boolean osrEnabled) {
            this.osrEnabled = osrEnabled;
            return this;
        }

        public Builder windowlessFrameRate(int windowlessFrameRate) {
            this.windowlessFrameRate = windowlessFrameRate;
            return this;
        }

        public Builder enableNetworkInterception(boolean enableNetworkInterception) {
            this.enableNetworkInterception = enableNetworkInterception;
            return this;
        }

        public Builder enableSecurity(boolean enableSecurity) {
            this.enableSecurity = enableSecurity;
            return this;
        }

        public Builder metricHandler(Consumer<BrowserMetric> metricHandler) {
            this.metricHandler = metricHandler;
            return this;
        }

        public Builder remoteDebuggingPort(int remoteDebuggingPort) {
            this.remoteDebuggingPort = remoteDebuggingPort;
            return this;
        }

        public Builder userAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        public Builder proxy(balbucio.browser4j.network.proxy.ProxyConfig proxy) {
            this.proxy = proxy;
            return this;
        }

        public BrowserRuntimeConfiguration build() {
            return new BrowserRuntimeConfiguration(this);
        }
    }
}
