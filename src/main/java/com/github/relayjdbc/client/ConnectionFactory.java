package com.github.relayjdbc.client;

import com.github.relayjdbc.RelayDriver;
import com.github.relayjdbc.VJdbcProperties;
import com.github.relayjdbc.VirtualConnection;
import com.github.relayjdbc.command.*;
import com.github.relayjdbc.protocol.dataformat.kryo.KryoDataFormat;
import com.github.relayjdbc.protocol.dataformat.kryo.KryoDataFormatConstants;
import com.github.relayjdbc.serial.CallingContext;
import com.github.relayjdbc.serial.UIDEx;
import com.github.relayjdbc.servlet.RequestEnhancer;
import com.github.relayjdbc.servlet.RequestEnhancerFactory;
import com.github.relayjdbc.protocol.transport.Transport;
import com.github.relayjdbc.protocol.transport.http.HttpTransport;
import com.github.relayjdbc.protocol.transport.ssh.SshPipeTransport;
import com.github.relayjdbc.util.ClientInfo;
import com.github.relayjdbc.util.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public final class ConnectionFactory {

    private static final String SERVLET_IDENTIFIER = "servlet:";
    private static final String SSH_PIPE = "sshpipe:";

    // The value 1 signals that every remote call shall provide a calling context. This should only
    // be used for debugging purposes as sending of these objects is quite expensive.
    private static final int REQUEST_PROVIDE_CALL_CONTEXT = 1;

    private static Log _logger = LogFactory.getLog(ConnectionFactory.class);

    public Connection getConnection(String relayUrl, Properties props) throws Exception {
        CommandSink sink;

        String[] urlparts;

        if(relayUrl.startsWith(SERVLET_IDENTIFIER)) {
            urlparts = StringUtils.split(relayUrl.substring(SERVLET_IDENTIFIER.length()));
            _logger.info("Relay in Servlet-Mode, using URL " + urlparts[0]);
            sink = createServletCommandSink(urlparts[0], props);
        } else if(relayUrl.startsWith(SSH_PIPE)) {
            urlparts = StringUtils.split(relayUrl.substring(SSH_PIPE.length()));
            _logger.info("Relay in SSH-PIPE-Mode, using URL " + urlparts[0]);
            sink = createSshPipeCommandSink(urlparts[0], props);
        } else {
            throw new SQLException("Unknown transport identifier: " + relayUrl);
        }

        if(urlparts[1].length() > 0) {
            _logger.info("Connecting to datasource " + urlparts[1]);
        } else {
            _logger.info("Using default datasource");
        }

        // Connect with the sink
        UIDEx reg = sink.connect(
                urlparts[1],
                props,
                ClientInfo.getProperties(props.getProperty(VJdbcProperties.CLIENTINFO_PROPERTIES)),
                new CallingContext());

        CallingContextFactory ctxFactory;
        if(reg.getValue1() == REQUEST_PROVIDE_CALL_CONTEXT) {
            ctxFactory = new StandardCallingContextFactory();
        }
        else {
            ctxFactory = new NullCallingContextFactory();
        }

        DecoratedCommandSink decosink = new DecoratedCommandSink(reg, sink, ctxFactory);

        return new VirtualConnection(reg, decosink, props, RelayDriver.isCacheEnabled());
    }

    private static CommandSink createServletCommandSink(String url, Properties props) throws Exception {
        RequestEnhancer requestEnhancer = null;

        String requestEnhancerFactoryClassName = props.getProperty(VJdbcProperties.SERVLET_REQUEST_ENHANCER_FACTORY);

        if(requestEnhancerFactoryClassName != null) {
            _logger.debug("Found RequestEnhancerFactory class: " + requestEnhancerFactoryClassName);
            Class requestEnhancerFactoryClass = Class.forName(requestEnhancerFactoryClassName);
            RequestEnhancerFactory requestEnhancerFactory = (RequestEnhancerFactory)requestEnhancerFactoryClass.newInstance();
            _logger.debug("RequestEnhancerFactory successfully created");
            requestEnhancer = requestEnhancerFactory.create();
        }

        Transport transport = new HttpTransport(new URL(url), requestEnhancer);

        KryoDataFormat protocol = new KryoDataFormat(
                        KryoDataFormatConstants.DEFAULT_COMPRESSION_MODE,
                        KryoDataFormatConstants.DEFAULT_COMPRESSION_THRESHOLD);


        return new DefaultClient(transport, protocol);
    }

    private static CommandSink createSshPipeCommandSink(String url, Properties props) throws Exception {

        KryoDataFormat protocol = new KryoDataFormat(
                        KryoDataFormatConstants.DEFAULT_COMPRESSION_MODE,
                        KryoDataFormatConstants.DEFAULT_COMPRESSION_THRESHOLD);

        SshPipeTransport sshPipeTransport = new SshPipeTransport(url, props);

        return new DefaultClient(sshPipeTransport, protocol);
    }
}
