package com.github.relayjdbc.server.base64pipe;

import java.io.IOException;
import java.io.OutputStream;

public class CloseSuppressingOutputStreamWrapper extends OutputStream {

    private final OutputStream delegate;


    public CloseSuppressingOutputStreamWrapper(OutputStream delegate) {
        this.delegate = delegate;
    }

    @Override
    public void close() throws IOException {
        // close is suppressed
    }

    @Override
    public void write(int b) throws IOException {
        delegate.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        delegate.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        delegate.write(b, off, len);
    }

    @Override
    public void flush() throws IOException {
        delegate.flush();
    }


}
