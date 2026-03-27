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
                
                // Add default flags for reliability in headless or critical desktop apps
                command_line.appendSwitch("disable-web-security"); // Depending on use case
                command_line.appendSwitch("ignore-certificate-errors");
                
                log.debug("Chromium flags set to: {}", command_line.getCommandLineString());
            }
        });
    }
}
