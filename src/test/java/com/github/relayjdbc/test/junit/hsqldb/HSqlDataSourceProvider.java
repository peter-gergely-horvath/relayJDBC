// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package com.github.relayjdbc.test.junit.hsqldb;

import com.github.relayjdbc.server.DataSourceProvider;

import javax.sql.DataSource;
import java.sql.SQLException;

public class HSqlDataSourceProvider implements DataSourceProvider {
    public DataSource getDataSource() throws SQLException {
        return new HSqlDataSource();
    }
}
