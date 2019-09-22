package com.github.relayjdbc.protocol.transport.http;

import com.github.relayjdbc.servlet.RequestEnhancer;
import com.github.relayjdbc.protocol.transport.Transport;
import com.github.relayjdbc.protocol.transport.TransportChannel;

import java.io.IOException;
import java.net.URL;

public class HttpTransport implements Transport {

    private final URL _url;
    private final RequestEnhancer _requestEnhancer;

    public HttpTransport(URL url, RequestEnhancer requestEnhancer) {
        _url = url;
        _requestEnhancer = requestEnhancer;
    }

    @Override
    public TransportChannel getTransportChannel() throws IOException {
        HttpTransportChannel httpTransportChannel = new HttpTransportChannel(_url, _requestEnhancer);

        return httpTransportChannel;
    }

    public void close() {
        // do nothing
    }
}
