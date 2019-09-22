package com.github.relayjdbc.test.junit.hsqldb;

import com.github.relayjdbc.test.junit.general.AddressTest;
import com.github.relayjdbc.test.junit.VJdbcTest;
import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import java.sql.Connection;
import java.sql.DriverManager;

public class HSqlDbAddressTestDataSource extends AddressTest {
    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite();
        
        VJdbcTest.addAllTestMethods(suite, HSqlDbAddressTestDataSource.class);
        
        TestSetup wrapper = new TestSetup(suite) {
            protected void setUp() throws Exception {
                new HSqlDbAddressTestDataSource("").oneTimeSetup();
            }

            protected void tearDown() throws Exception {
                new HSqlDbAddressTestDataSource("").oneTimeTearDown();
            }
        };

        return wrapper;
    }

    public HSqlDbAddressTestDataSource(String s) {
        super(s);
    }

    protected Connection createNativeDatabaseConnection() throws Exception {
        Class.forName("org.hsqldb.jdbcDriver");
        return DriverManager.getConnection("jdbc:hsqldb:hsql://localhost/HSqlDb", "sa", "");
    }
    
    protected String getVJdbcDatabaseShortcut() {
        return "HSqlDB-DataSource";
    }
}
