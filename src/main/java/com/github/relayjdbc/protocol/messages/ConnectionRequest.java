package com.github.relayjdbc.protocol.messages;

import com.github.relayjdbc.serial.CallingContext;

import java.util.Properties;

public class ConnectionRequest {
    private final String database;
    private final Properties props;
    private final Properties clientInfo;
    private final CallingContext ctx;

    public ConnectionRequest(String database, Properties props, Properties clientInfo, CallingContext ctx) {
        this.database = database;
        this.props = props;
        this.clientInfo = clientInfo;
        this.ctx = ctx;
    }

    public String getDatabase() {
        return database;
    }

    public Properties getProps() {
        return props;
    }

    public Properties getClientInfo() {
        return clientInfo;
    }

    public CallingContext getCtx() {
        return ctx;
    }
}
