// VJDBC - Virtual JDBC
// Written by Hunter Payne
// Website: http://vjdbc.sourceforge.net

package com.github.relayjdbc;

import java.sql.SQLException;

/**
 * A factory for turning proxy network objects back into their full JDBC
 * form on the client.
 */
@Deprecated
public interface ProxyFactory {

    public Object makeJdbcObject(Object proxy) throws SQLException;
}
