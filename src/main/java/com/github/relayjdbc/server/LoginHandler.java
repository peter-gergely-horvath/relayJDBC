// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package com.github.relayjdbc.server;

import com.github.relayjdbc.VJdbcException;

public interface LoginHandler {
    String checkLogin(String user, String password) throws VJdbcException;
}
