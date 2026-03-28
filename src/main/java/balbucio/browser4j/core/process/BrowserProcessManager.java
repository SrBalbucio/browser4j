package balbucio.browser4j.core.process;

import balbucio.browser4j.core.config.BrowserRuntimeConfiguration;
import org.cef.CefSettings;
import org.cef.CefApp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BrowserProcessManager {
    private static final Logger log = LoggerFactory.getLogger(BrowserProcessManager.class);

    public static void configureArgs(CefSettings settings, BrowserRuntimeConfiguration config) {
        log.info("Configuring browser process arguments...");
        
        CefApp.addAppHandler(new org.cef.handler.CefAppHandlerAdapter(null) {
            @Override
            public void onBeforeCommandLineProcessing(String process_type, org.cef.callback.CefCommandLine command_line) {
                if (!config.isEnableGPU()) {
                    command_line.appendSwitch("disable-gpu");
                    command_line.appendSwitch("disable-gpu-compositing");
                }
                
                if (!config.isEnableSandbox()) {
                    command_line.appendSwitch("no-sandbox");
                    command_line.appendSwitch("disable-setuid-sandbox");
                }
                
                if (config.isOsrEnabled()) {
                    command_line.appendSwitch("disable-gpu-vsync");
                    if (config.getWindowlessFrameRate() <= 0) {
                        command_line.appendSwitch("disable-frame-rate-limit");
                    }
                }
                
                // Add default flags for reliability in headless or critical desktop apps
                command_line.appendSwitch("disable-web-security"); // Depending on use case
                command_line.appendSwitch("ignore-certificate-errors");

                if (config.getRemoteDebuggingPort() > 0) {
                    command_line.appendSwitchWithValue("remote-debugging-port", String.valueOf(config.getRemoteDebuggingPort()));
                }

                if (config.getUserAgent() != null && !config.getUserAgent().isEmpty()) {
                    command_line.appendSwitchWithValue("user-agent", config.getUserAgent());
                }

                if (config.getProxy() != null) {
                    String proxyServer = config.getProxy().getServerString();
                    if (proxyServer != null) {
                        command_line.appendSwitchWithValue("proxy-server", proxyServer);
                    }
                    if (config.getProxy().getBypassList() != null) {
                        command_line.appendSwitchWithValue("proxy-bypass-list", config.getProxy().getBypassList());
                    }
                }
                
                log.debug("Chromium flags set to: {}", command_line.getArguments().toString());
            }
        });
    }
}
