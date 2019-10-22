package com.github.relayjdbc.client;

import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.Properties;

import com.github.relayjdbc.RelayJdbcProperties;
import com.github.relayjdbc.command.CommandSink;
import com.github.relayjdbc.protocol.dataformat.DataFormat;
import com.github.relayjdbc.protocol.dataformat.DecodeException;
import com.github.relayjdbc.protocol.dataformat.Decoder;
import com.github.relayjdbc.protocol.dataformat.Encoder;
import com.github.relayjdbc.protocol.messages.ExecuteCommandRequest;
import com.github.relayjdbc.protocol.messages.ConnectionRequest;
import com.github.relayjdbc.protocol.transport.Transport;
import com.github.relayjdbc.protocol.transport.TransportChannel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.github.relayjdbc.command.Command;
import com.github.relayjdbc.command.ConnectionSetClientInfoCommand;
import com.github.relayjdbc.serial.CallingContext;
import com.github.relayjdbc.serial.UIDEx;
import com.github.relayjdbc.server.config.ConfigurationException;
import com.github.relayjdbc.util.PerformanceConfig;
import com.github.relayjdbc.util.SQLExceptionHelper;

public class DefaultClient implements CommandSink {
    private static Log _logger = LogFactory.getLog(DefaultClient.class);

    private final DataFormat dataFormat;
    private final Transport transport;

    public DefaultClient(
            Transport transport,
            DataFormat dataFormat) {
        this.dataFormat = dataFormat;
        this.transport = transport;
    }

    public UIDEx connect(String database, Properties props, Properties clientInfo, CallingContext ctx) throws SQLException {

        try (TransportChannel transportChannel = transport.getTransportChannel();
             Encoder encoder = dataFormat.getProtocolEncoder();
             Decoder decoder = dataFormat.getProtocolDecoder()) {

            transportChannel.open();

            OutputStream outputStream = transportChannel.getOutputStream();

            ConnectionRequest connectionRequest = new ConnectionRequest(database, props, clientInfo, ctx);
            encoder.encode(outputStream, connectionRequest);

            InputStream inputStream = transportChannel.sendAndWaitForResponse();

            Object result = decodeResponse(decoder, inputStream);

            reconfigureProtocol((UIDEx) result);

            return (UIDEx) result;
        } catch (SQLException e) {
            throw e;
        } catch (Exception e) {
            throw SQLExceptionHelper.wrap(e);
        }
    }

    private Object decodeResponse(Decoder decoder, InputStream inputStream) throws SQLException {

        try {
            Object result = decoder.readObject(inputStream);

            if (result instanceof SQLException) {
                // This might be a SQLException which must be rethrown
                throw (SQLException) result;
            }

            return result;

        } catch (DecodeException e) {
            throw new SQLException("Failed to decode the response received. " +
                    "(Ensure the relayed driver JAR is added to the client as well)", e);
        }
    }

    private void reconfigureProtocol(UIDEx result) {
        try {
            int performanceProfile = result.getValue2();
            final int newCompressionMode = PerformanceConfig.getCompressionMode(performanceProfile);
            final int newCompressionThreshold = PerformanceConfig.getCompressionThreshold(performanceProfile);

            dataFormat.setCompressionMode(newCompressionMode);
            dataFormat.setCompressionThreshold(newCompressionThreshold);

        } catch (ConfigurationException e) {
            _logger.debug("Invalid compression mode", e);
        }
    }

    public Object process(Long connuid, Long uid, Command cmd, CallingContext ctx) throws SQLException {

        if (cmd instanceof ConnectionSetClientInfoCommand) {
            updatePerformanceConfig((ConnectionSetClientInfoCommand) cmd);
        }

        try (TransportChannel transportChannel = transport.getTransportChannel();
             Encoder encoder = dataFormat.getProtocolEncoder();
             Decoder decoder = dataFormat.getProtocolDecoder()) {

            transportChannel.open();

            OutputStream outputStream = transportChannel.getOutputStream();

            ExecuteCommandRequest executeCommandRequest = new ExecuteCommandRequest(connuid, uid, cmd, ctx);
            encoder.encode(outputStream, executeCommandRequest);

            InputStream inputStream = transportChannel.sendAndWaitForResponse();

            return decodeResponse(decoder, inputStream);

        } catch (SQLException e) {
            throw e;
        } catch (Exception e) {
            throw SQLExceptionHelper.wrap(e);
        }
    }

    @Override
    public void close() {
        transport.close();
    }

    private void updatePerformanceConfig(ConnectionSetClientInfoCommand cmd) {
        String name = cmd.getName();
        String value = cmd.getValue();
        if (RelayJdbcProperties.PERFORMANCE_PROFILE.equals(name)) {
            try {
                final int performanceProfile = Integer.parseInt(value);
                final int newCompressionMode = PerformanceConfig.getCompressionMode(performanceProfile);
                final int newCompressionThreshold = PerformanceConfig.getCompressionThreshold(performanceProfile);

                dataFormat.setCompressionMode(newCompressionMode);
                dataFormat.setCompressionThreshold(newCompressionThreshold);

            } catch (NumberFormatException e) {
                _logger.debug("Ignoring invalid number format for performance profile", e);
            } catch (ConfigurationException e) {
                _logger.debug("Ignoring invalid performance profile", e);
            }
        } else if (RelayJdbcProperties.COMPRESSION_MODE.equals(name)) {
            try {
                final int newCompressionMode = PerformanceConfig.parseCompressionMode(value);

                dataFormat.setCompressionMode(newCompressionMode);

            } catch (ConfigurationException e) {
                _logger.debug("Ignoring invalid compression mode", e);
            }
        } else if (RelayJdbcProperties.COMPRESSION_THRESHOLD.equals(name)) {
            try {
                final int newCompressionThreshold = PerformanceConfig.parseCompressionThreshold(value);

                dataFormat.setCompressionThreshold(newCompressionThreshold);
            } catch (ConfigurationException e) {
                _logger.debug("Ignoring invalid compression threshold", e);
            }
        }
    }
}
