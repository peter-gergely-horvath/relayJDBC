package com.github.relayjdbc.protocol.dataformat;

import com.github.relayjdbc.protocol.messages.ExecuteCommandRequest;
import com.github.relayjdbc.protocol.messages.ConnectionRequest;

import java.io.OutputStream;

public interface Encoder extends AutoCloseable {
    void encode(OutputStream outputStream, ConnectionRequest connectionRequest);

    void encode(OutputStream outputStream, ExecuteCommandRequest executeCommandRequest);

}
