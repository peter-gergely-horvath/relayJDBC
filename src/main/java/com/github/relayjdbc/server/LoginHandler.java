package com.github.relayjdbc.server;

import com.github.relayjdbc.VJdbcException;

public interface LoginHandler {
    String checkLogin(String user, String password) throws VJdbcException;
}
