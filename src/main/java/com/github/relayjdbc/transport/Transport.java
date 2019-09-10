package com.github.relayjdbc.transport;

import java.io.IOException;

public interface Transport {
    TransportChannel getTransportChannel() throws IOException;

    default void close() {
        // do nothing
    }
}
