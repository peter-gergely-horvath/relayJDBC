package com.github.relayjdbc.connectiontypes;

import com.github.relayjdbc.client.DefaultClient;
import com.github.relayjdbc.command.CommandSink;
import com.github.relayjdbc.protocol.dataformat.DataFormat;
import com.github.relayjdbc.protocol.dataformat.kryo.KryoDataFormat;
import com.github.relayjdbc.protocol.dataformat.kryo.KryoDataFormatConstants;
import com.github.relayjdbc.protocol.transport.Transport;
import com.github.relayjdbc.util.StringUtils;

import java.io.Serializable;
import java.sql.DriverPropertyInfo;
import java.util.Properties;

public abstract class ConnectionTypeHandler implements Serializable {

    private final String connectionTypePrefix;

    protected ConnectionTypeHandler(String connectionTypePrefix) {
        this.connectionTypePrefix = connectionTypePrefix;
    }

    boolean canHandle(String url) {
        return url.startsWith(connectionTypePrefix);
    }

    public String getDataSourceName(String url) {
        String[] urlAndDataSourceName = StringUtils.split(url.substring(connectionTypePrefix.length()));

        return urlAndDataSourceName[1];
    }

    public final CommandSink getCommandSink(String relayUrl, Properties props) throws Exception {
        String[] urlAndDataSourceName = StringUtils.split(relayUrl.substring(connectionTypePrefix.length()));

        DataFormat dataFormat = getDataFormat(urlAndDataSourceName[0], props);
        Transport transport = getTransport(urlAndDataSourceName[0], props);

        return new DefaultClient(transport, dataFormat);
    }

    protected DataFormat getDataFormat(String url, Properties props) {
        return new KryoDataFormat(
                    KryoDataFormatConstants.DEFAULT_COMPRESSION_MODE,
                    KryoDataFormatConstants.DEFAULT_COMPRESSION_THRESHOLD);
    }

    protected abstract Transport getTransport(String url, Properties props) throws Exception;

    public abstract DriverPropertyInfo[] getJdbcDriverPropertyInfo(String relayUrl, Properties info);
}
