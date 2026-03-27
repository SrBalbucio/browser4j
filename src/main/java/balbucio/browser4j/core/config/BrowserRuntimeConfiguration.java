package balbucio.browser4j.core.config;

import java.io.File;

public class BrowserRuntimeConfiguration {

    private final String cachePath;
    private final String userDataPath;
    private final boolean enableGPU;
    private final boolean enableSandbox;
    private final boolean osrEnabled;
    private final int windowlessFrameRate;

    private BrowserRuntimeConfiguration(Builder builder) {
        this.cachePath = builder.cachePath;
        this.userDataPath = builder.userDataPath;
        this.enableGPU = builder.enableGPU;
        this.enableSandbox = builder.enableSandbox;
        this.osrEnabled = builder.osrEnabled;
        this.windowlessFrameRate = builder.windowlessFrameRate;
    }

    public String getCachePath() { return cachePath; }
    public String getUserDataPath() { return userDataPath; }
    public boolean isEnableGPU() { return enableGPU; }
    public boolean isEnableSandbox() { return enableSandbox; }
    public boolean isOsrEnabled() { return osrEnabled; }
    public int getWindowlessFrameRate() { return windowlessFrameRate; }

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

        public BrowserRuntimeConfiguration build() {
            return new BrowserRuntimeConfiguration(this);
        }
    }
}
