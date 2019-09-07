package com.github.relayjdbc.transport.http;

import com.github.relayjdbc.servlet.RequestEnhancer;
import com.github.relayjdbc.transport.Transport;
import com.github.relayjdbc.transport.TransportChannel;

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
}
