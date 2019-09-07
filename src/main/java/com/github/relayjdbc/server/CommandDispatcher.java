package com.github.relayjdbc.server;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.github.relayjdbc.VJdbcProperties;
import com.github.relayjdbc.command.Command;
import com.github.relayjdbc.command.ConnectionContext;
import com.github.relayjdbc.protocol.ProtocolConstants;
import com.github.relayjdbc.serial.CallingContext;
import com.github.relayjdbc.server.command.CommandProcessor;
import com.github.relayjdbc.server.config.ConnectionConfiguration;
import com.github.relayjdbc.server.config.VJdbcConfiguration;
import com.github.relayjdbc.server.servlet.KryoServletCommandSink;
import com.github.relayjdbc.servlet.ServletCommandSinkIdentifier;
import com.github.relayjdbc.util.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class CommandDispatcher {

    private static Log _logger = LogFactory.getLog(CommandDispatcher.class);

    private final CommandProcessor commandProcessor;

    public CommandDispatcher(CommandProcessor processor) {
        this.commandProcessor = processor;
    }

    public void dispatch(InputStream inputStream, OutputStream os) {
        Input input = null;
        DeflatingOutput output = null;
        Kryo kryo = null;
        try {
            kryo = KryoFactory.getInstance().getKryo();

            input = new InflatingInput(inputStream);

            checkMessageHeader(input, kryo);

            String method = kryo.readObject(input, String.class);

            if (method == null) {
                throw new RuntimeException("Unknown or empty command received: " + method);
            }

            output = new DeflatingOutput(os);
            Object objectToReturn = null;

            try {
                // Some command to process ?
                if (method.equals(ServletCommandSinkIdentifier.PROCESS_COMMAND)) {
                    // Read parameter objects
                    Long connuid = kryo.readObjectOrNull(input, Long.class);
                    Long uid = kryo.readObjectOrNull(input, Long.class);
                    Command cmd = (Command) kryo.readClassAndObject(input);
                    CallingContext ctx = kryo.readObjectOrNull(input, CallingContext.class);
                    if (connuid != null) {
                        ConnectionContext connectionEntry = commandProcessor.getConnectionEntry(connuid);
                        if (connectionEntry != null) {
                            output.setCompressionMode(connectionEntry.getCompressionMode());
                            output.setThreshold(connectionEntry.getCompressionThreshold());
                        }
                    }
                    // Delegate execution to the CommandProcessor
                    objectToReturn = commandProcessor.process(connuid, uid, cmd, ctx);
                } else if (method.equals(ServletCommandSinkIdentifier.CONNECT_COMMAND)) {
                    String url = kryo.readObject(input, String.class);
                    Properties props = kryo.readObject(input, Properties.class);
                    Properties clientInfo = kryo.readObject(input, Properties.class);
                    CallingContext ctx = kryo.readObjectOrNull(input, CallingContext.class);

                    ConnectionConfiguration connectionConfiguration = VJdbcConfiguration.singleton().getConnection(url);

                    if (connectionConfiguration != null) {
                        Connection conn = connectionConfiguration.create(props);
                        Object userName = props.get(VJdbcProperties.USER_NAME);
                        if (userName != null) {
                            clientInfo.put(VJdbcProperties.USER_NAME, userName);
                        }
                        objectToReturn = commandProcessor.registerConnection(conn, connectionConfiguration, clientInfo, ctx);
                    } else {
                        objectToReturn = new SQLException("VJDBC-Connection " + url + " not found");
                    }
                }
            } catch (Throwable t) {
                // Wrap any exception so that it can be transported back to
                // the client
                objectToReturn = SQLExceptionHelper.wrap(t);
            }

            // Write the result in the response buffer
            kryo.writeClassAndObject(output, objectToReturn);
            output.flush();
            StreamCloser.close(output);
            output = null;

        } finally {
            StreamCloser.close(input);
            StreamCloser.close(output);
            if (kryo != null) {
                KryoFactory.getInstance().releaseKryo(kryo);
            }
        }
    }

    private void checkMessageHeader(Input input, Kryo kryo) {
        try {
            String magic = kryo.readObject(input, String.class);
            if (!ProtocolConstants.MAGIC.equals(magic)) {
                _logger.warn("Protocol error: magic not found, but was: " + magic);
                throw new RuntimeException("Protocol error: magic not found, but was: " + magic);
            }

            String clientVersion = kryo.readObject(input, String.class);
            if (!KryoServletCommandSink.PROTOCOL_VERSION.equals(clientVersion)) {
                String errorMsg = String.format("Protocol version mismatch: expected %s but was %s",
                        KryoServletCommandSink.PROTOCOL_VERSION, clientVersion);
                _logger.warn(errorMsg);
                throw new RuntimeException(errorMsg);
            }
        } catch (Exception ex) {
            _logger.warn("Handshake failed", ex);
            throw new RuntimeException("Protocol error: Handshake failed", ex);
        }

    }
}
