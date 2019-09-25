package com.github.relayjdbc.protocol.transport;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface TransportChannel extends AutoCloseable {

    void open() throws IOException;

    OutputStream getOutputStream() throws IOException;

    InputStream sendAndWaitForResponse() throws IOException;
}
