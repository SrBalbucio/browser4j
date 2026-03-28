package balbucio.browser4j.browser.api;

import balbucio.browser4j.network.proxy.ProxyConfig;

public class BrowserOptions {
    private final String userAgent;
    private final ProxyConfig proxy;
    private final String profilePath;

    private BrowserOptions(Builder builder) {
        this.userAgent = builder.userAgent;
        this.proxy = builder.proxy;
        this.profilePath = builder.profilePath;
    }

    public String getUserAgent() { return userAgent; }
    public ProxyConfig getProxy() { return proxy; }
    public String getProfilePath() { return profilePath; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String userAgent;
        private ProxyConfig proxy;
        private String profilePath;

        public Builder userAgent(String userAgent) { this.userAgent = userAgent; return this; }
        public Builder proxy(ProxyConfig proxy) { this.proxy = proxy; return this; }
        public Builder profile(String profilePath) { this.profilePath = profilePath; return this; }

        public BrowserOptions build() { return new BrowserOptions(this); }
    }
}
