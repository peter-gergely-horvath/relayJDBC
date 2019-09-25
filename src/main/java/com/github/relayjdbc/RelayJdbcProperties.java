package com.github.relayjdbc;

public final class RelayJdbcProperties {
    // System properties to transfer to the server when opening a connection 
    public static final String CLIENTINFO_PROPERTIES = "relayjdbc.clientinfo.properties";
    // Tables to be cached, property must be in the format "Table[:Refresh-Interval],Table..."
    public static final String CACHE_TABLES = "relayjdbc.cache.tables";
    // Login-Handler-Class which authenticates the user
    public static final String LOGIN_USER = "relayjdbc.login.user";
    public static final String LOGIN_PASSWORD = "relayjdbc.login.password";

    // Factory class that create Servlet-Request enhancers which can put additional Request-Properties
    // in HTTP-Requests
    public static final String SERVLET_REQUEST_ENHANCER_FACTORY = "relayjdbc.servlet.request_enhancer_factory";
    
    public static final String USER_NAME = "relayjdbc.user.name";
    
    public static final String COMPRESSION_MODE = "relayjdbc.compression.mode";
    public static final String COMPRESSION_THRESHOLD = "relayjdbc.compression.threshold";
    public static final String ROW_PACKET_SIZE = "relayjdbc.row.packet.size";
    public static final String PERFORMANCE_PROFILE = "relayjdbc.performance.profile";
}
