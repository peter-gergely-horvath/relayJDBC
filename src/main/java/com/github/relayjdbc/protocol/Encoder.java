package com.github.relayjdbc.protocol;

import com.github.relayjdbc.command.Command;
import com.github.relayjdbc.serial.CallingContext;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

public interface Encoder extends AutoCloseable {
    void writeConnect(OutputStream outputStream,
                      String database, Properties props, Properties clientInfo, CallingContext ctx);

    void writePerform(OutputStream outputStream, Long connuid, Long uid, Command cmd, CallingContext ctx);

}
