package com.github.relayjdbc.protocol.transport;

import java.io.IOException;

public interface Transport {
    TransportChannel getTransportChannel() throws IOException;

    void close();
}
