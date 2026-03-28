package balbucio.browser4j.network.protocol;

import balbucio.browser4j.network.interception.Response;

public interface ProtocolHandler {
    Response handle(String url);
}
