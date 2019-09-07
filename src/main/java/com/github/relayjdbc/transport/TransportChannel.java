package com.github.relayjdbc.transport;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface TransportChannel extends AutoCloseable {
    OutputStream getOutputStream() throws IOException;

    InputStream sendAndWaitForResponse() throws IOException;
}
