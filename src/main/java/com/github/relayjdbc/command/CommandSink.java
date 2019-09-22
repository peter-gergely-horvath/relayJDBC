package com.github.relayjdbc.command;

import com.github.relayjdbc.serial.CallingContext;
import com.github.relayjdbc.serial.UIDEx;

import java.sql.SQLException;
import java.util.Properties;

/**
 * Interface which each CommandSink must implement to be used for
 * VJDBC client-server communication.
 */
public interface CommandSink {
    UIDEx connect(String database, Properties props, Properties clientInfo, CallingContext ctx) throws SQLException;

    Object process(Long connuid, Long uid, Command cmd, CallingContext ctx) throws SQLException;
    
    void close();
}
