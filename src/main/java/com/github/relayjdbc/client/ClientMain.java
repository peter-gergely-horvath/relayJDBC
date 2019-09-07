package com.github.relayjdbc.client;

import java.sql.*;

public class ClientMain {

    public static void main(String[] args) throws SQLException {

        Connection connection = DriverManager.getConnection("jdbc:relayjdbc:servlet:http://localhost:12345/,test");

        DatabaseMetaData metaData = connection.getMetaData();

        System.out.format("Connected to %s %n", metaData.getDatabaseProductName());
    }
}
