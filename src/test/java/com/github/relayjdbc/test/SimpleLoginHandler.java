package com.github.relayjdbc.test;

import com.github.relayjdbc.VJdbcException;
import com.github.relayjdbc.server.LoginHandler;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class SimpleLoginHandler implements LoginHandler {
    private Properties _properties = new Properties();

    public SimpleLoginHandler() throws IOException {
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("com/github/relayjdbc/test/user.properties");
        _properties.load(is);
    }

    public String checkLogin(String user, String password) throws VJdbcException {
        if (user != null) {
            String pw = _properties.getProperty(user);

            if (pw != null) {
                if (!pw.equals(password)) {
                    throw new VJdbcException("Password for user " + user + " is wrong");
                }
                return user;
            } else {
                throw new VJdbcException("Unknown user " + user);
            }
        } else {
            throw new VJdbcException("User is null");
        }
    }
}
