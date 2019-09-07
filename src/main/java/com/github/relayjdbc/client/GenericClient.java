// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package com.github.relayjdbc.client;

import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.Properties;

import com.github.relayjdbc.command.CommandSink;
import com.github.relayjdbc.protocol.Decoder;
import com.github.relayjdbc.protocol.Encoder;
import com.github.relayjdbc.protocol.Protocol;
import com.github.relayjdbc.transport.Transport;
import com.github.relayjdbc.transport.TransportChannel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.github.relayjdbc.VJdbcProperties;
import com.github.relayjdbc.command.Command;
import com.github.relayjdbc.command.ConnectionSetClientInfoCommand;
import com.github.relayjdbc.serial.CallingContext;
import com.github.relayjdbc.serial.UIDEx;
import com.github.relayjdbc.server.config.ConfigurationException;
import com.github.relayjdbc.util.PerformanceConfig;
import com.github.relayjdbc.util.SQLExceptionHelper;

public class GenericClient implements CommandSink {
    private static Log _logger = LogFactory.getLog(GenericClient.class);

    private final Protocol protocol;
    private final Transport transport;

    public GenericClient(
            Transport transport,
            Protocol protocol) throws SQLException {
        this.protocol = protocol;
        this.transport = transport;
    }

    public UIDEx connect(String database, Properties props, Properties clientInfo, CallingContext ctx) throws SQLException {

        try (TransportChannel transportChannel = transport.getTransportChannel();
             Encoder encoder = protocol.getProtocolEncoder();
             Decoder decoder = protocol.getProtocolDecoder()) {

            OutputStream outputStream = transportChannel.getOutputStream();

            encoder.writeConnect(outputStream, database, props, clientInfo, ctx);

            InputStream inputStream = transportChannel.sendAndWaitForResponse();

            Object result = decoder.readObject(inputStream);

            if (result instanceof SQLException) {
                // This might be a SQLException which must be rethrown
                throw (SQLException) result;
            }

            reconfigureProtocol((UIDEx) result);

            return (UIDEx) result;
        } catch (SQLException e) {
            throw e;
        } catch (Exception e) {
            throw SQLExceptionHelper.wrap(e);
        }
    }

    private void reconfigureProtocol(UIDEx result) {
        try {
            int performanceProfile = result.getValue2();
            final int newCompressionMode = PerformanceConfig.getCompressionMode(performanceProfile);
            final int newCompressionThreshold = PerformanceConfig.getCompressionThreshold(performanceProfile);

            protocol.setCompressionMode(newCompressionMode);
            protocol.setCompressionThreshold(newCompressionThreshold);

        } catch (ConfigurationException e) {
            _logger.debug("Invalid compression mode", e);
        }
    }

    public Object process(Long connuid, Long uid, Command cmd, CallingContext ctx) throws SQLException {

        if (cmd instanceof ConnectionSetClientInfoCommand) {
            updatePerformanceConfig((ConnectionSetClientInfoCommand) cmd);
        }

        try (TransportChannel transportChannel = transport.getTransportChannel();
             Encoder encoder = protocol.getProtocolEncoder();
             Decoder decoder = protocol.getProtocolDecoder()) {

            OutputStream outputStream = transportChannel.getOutputStream();

            encoder.writePerform(outputStream, connuid, uid, cmd, ctx);

            InputStream inputStream = transportChannel.sendAndWaitForResponse();

            Object result = decoder.readObject(inputStream);
            if (result instanceof SQLException) {
                throw (SQLException) result;
            } else {
                return result;
            }
        } catch (SQLException e) {
            throw e;
        } catch (Exception e) {
            throw SQLExceptionHelper.wrap(e);
        }
    }

    @Override
    public void close() {
        // no-op
    }

    private void updatePerformanceConfig(ConnectionSetClientInfoCommand cmd) {
        String name = cmd.getName();
        String value = cmd.getValue();
        if (VJdbcProperties.PERFORMANCE_PROFILE.equals(name)) {
            try {
                final int performanceProfile = Integer.parseInt(value);
                final int newCompressionMode = PerformanceConfig.getCompressionMode(performanceProfile);
                final int newCompressionThreshold = PerformanceConfig.getCompressionThreshold(performanceProfile);

                protocol.setCompressionMode(newCompressionMode);
                protocol.setCompressionThreshold(newCompressionThreshold);

            } catch (NumberFormatException e) {
                _logger.debug("Ignoring invalid number format for performance profile", e);
            } catch (ConfigurationException e) {
                _logger.debug("Ignoring invalid performance profile", e);
            }
        } else if (VJdbcProperties.COMPRESSION_MODE.equals(name)) {
            try {
                final int newCompressionMode = PerformanceConfig.parseCompressionMode(value);

                protocol.setCompressionMode(newCompressionMode);

            } catch (ConfigurationException e) {
                _logger.debug("Ignoring invalid compression mode", e);
            }
        } else if (VJdbcProperties.COMPRESSION_THRESHOLD.equals(name)) {
            try {
                final int newCompressionThreshold = PerformanceConfig.parseCompressionThreshold(value);

                protocol.setCompressionThreshold(newCompressionThreshold);
            } catch (ConfigurationException e) {
                _logger.debug("Ignoring invalid compression threshold", e);
            }
        }
    }
}
