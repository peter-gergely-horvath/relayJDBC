// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package com.github.relayjdbc.test.junit.mysql;

import com.github.relayjdbc.test.junit.general.AddressTest;
import com.github.relayjdbc.test.junit.VJdbcTest;
import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import java.sql.Connection;
import java.sql.DriverManager;

public class MySqlDbAddressTest extends AddressTest {
    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite();

        Class.forName("com.mysql.jdbc.Driver");
        VJdbcTest.addAllTestMethods(suite, MySqlDbAddressTest.class);

        TestSetup wrapper = new TestSetup(suite) {
            protected void setUp() throws Exception {
                new MySqlDbAddressTest("").oneTimeSetup();
            }

            protected void tearDown() throws Exception {
                new MySqlDbAddressTest("").oneTimeTearDown();
            }
        };

        return wrapper;
    }

    public MySqlDbAddressTest(String s) {
        super(s);
    }

    protected Connection createNativeDatabaseConnection() throws Exception {
        Class.forName("com.mysql.jdbc.Driver");
        return DriverManager.getConnection("jdbc:mysql:///test", "root", "");
    }

    protected String getVJdbcDatabaseShortcut() {
        return "MySqlDB";
    }
}
