// Relay - Virtual JDBC
// Written by Michael Link
// Website: http://Relay.sourceforge.net

package com.github.relayjdbc;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;

import com.github.relayjdbc.client.ConnectionFactory;
import com.github.relayjdbc.util.SQLExceptionHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public final class RelayDriver implements Driver {

    private static final String RELAY_DRIVER_JDBC_URL_PREFIX = "jdbc:relayjdbc:";

    private static Log logger = LogFactory.getLog(RelayDriver.class);

    private static boolean cacheEnabled;

    static {
        try {
            DriverManager.registerDriver(new RelayDriver());
            logger.info("RelayDriver JDBC-Driver successfully registered");
            try {
                Class.forName("org.hsqldb.jdbcDriver").newInstance();
                logger.info("HSQL-Driver loaded, caching activated");
                cacheEnabled = true;
            } catch (ClassNotFoundException e) {
                logger.info("Couldn't load HSQL-Driver, caching deactivated");
                cacheEnabled = false;
            } catch (Exception e) {
                logger.error("Unexpected exception occured on loading the HSQL-Driver");
                cacheEnabled = false;
            }
        } catch (Exception e) {
            logger.fatal("Couldn't register RelayDriver JDBC-Driver !", e);
            throw new RuntimeException("Couldn't register RelayDriver JDBC-Driver !", e);
        }
    }

    private final ConnectionFactory connectionFactory;

    public RelayDriver() {
        this(new ConnectionFactory());
    }

    RelayDriver(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public static boolean isCacheEnabled() {
        return cacheEnabled;
    }

    public Connection connect(String urlstr, Properties props) throws SQLException {
        if (!acceptsURL(urlstr)) {
            return null;
        }

        String relayUrl = urlstr.substring(RELAY_DRIVER_JDBC_URL_PREFIX.length());

        logger.info("Relay-URL: " + relayUrl);

        try {
            return connectionFactory.getConnection(relayUrl, props);
        } catch (Exception e) {
            logger.error(e);
            throw SQLExceptionHelper.wrap(e);
        }
    }

    public boolean acceptsURL(String url) throws SQLException {
        return url.startsWith(RELAY_DRIVER_JDBC_URL_PREFIX);
    }

    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
        return new DriverPropertyInfo[0];
    }

    public int getMajorVersion() {
        return Version.majorIntVersion;
    }

    public int getMinorVersion() {
        return Version.minorIntVersion;
    }

    public boolean jdbcCompliant() {
        return true;
    }

    public Logger getParentLogger() {
        return Logger.getLogger("com.github.relayjdbc");
    }
}
