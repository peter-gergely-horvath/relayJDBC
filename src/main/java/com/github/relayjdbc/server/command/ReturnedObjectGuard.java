package com.github.relayjdbc.server.command;

import com.github.relayjdbc.Registerable;
import com.github.relayjdbc.serial.UIDEx;
import com.github.relayjdbc.util.JavaVersionInfo;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;

/**
 * This guard object checks if the given object shall be passed to the client or if it shall
 * be put into the object pool with a UID.
 */
class ReturnedObjectGuard {

    public static UIDEx checkResult(Object obj) {
        if (obj instanceof Registerable) {
            return ((Registerable)obj).getReg();
        } else if(obj instanceof Statement) {
            try {
                Statement stmt = (Statement)obj;
                return new UIDEx(stmt.getQueryTimeout(), stmt.getMaxRows());
            } catch(SQLException e) {
                return new UIDEx();
            }
        } else if(obj instanceof DatabaseMetaData) {
            return new UIDEx();
        } else if(JavaVersionInfo.use14Api && obj instanceof Savepoint) {
            return new UIDEx();
        } else {
            return null;
        }
    }
}
