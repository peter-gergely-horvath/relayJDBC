package com.github.relayjdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.github.relayjdbc.serial.StreamingResultSet;
import com.github.relayjdbc.serial.UIDEx;
import com.github.relayjdbc.util.ClientInfo;
import com.github.relayjdbc.util.SQLExceptionHelper;
import com.github.relayjdbc.command.CommandPool;
import com.github.relayjdbc.command.DecoratedCommandSink;
import com.github.relayjdbc.command.DestroyCommand;
import com.github.relayjdbc.command.JdbcInterfaceType;
import com.github.relayjdbc.command.ParameterTypeCombinations;
import com.github.relayjdbc.command.StatementCancelCommand;
import com.github.relayjdbc.command.StatementExecuteBatchCommand;
import com.github.relayjdbc.command.StatementExecuteCommand;
import com.github.relayjdbc.command.StatementExecuteExtendedCommand;
import com.github.relayjdbc.command.StatementGetGeneratedKeysCommand;
import com.github.relayjdbc.command.StatementGetResultSetCommand;
import com.github.relayjdbc.command.StatementQueryCommand;
import com.github.relayjdbc.command.StatementSetFetchSizeCommand;
import com.github.relayjdbc.command.StatementUpdateCommand;
import com.github.relayjdbc.command.StatementUpdateExtendedCommand;

public class VirtualStatement extends VirtualBase implements Statement {
    protected Connection _connection;
    protected List _batchCollector = new ArrayList();
    protected int _maxRows = -1;
    protected int _queryTimeout = -1;
    protected StreamingResultSet _currentResultSet;
    protected int _resultSetType;
    protected boolean _isClosed = false;
    protected boolean _isCloseOnCompletion = false;
    protected final boolean fastUpdate;

    public VirtualStatement(UIDEx reg, Connection connection, DecoratedCommandSink theSink, int resultSetType) {
        super(reg, theSink);
        // Remember the connection
        _connection = connection;
        // Remember ResultSetType
        _resultSetType = resultSetType;
        // Get the optional additional information which was delivered from the server
        // upon creation of the Statement object.
        if (reg.getValue1() != Integer.MIN_VALUE) {
            _queryTimeout = reg.getValue1();
        }
        if (reg.getValue2() != Integer.MIN_VALUE) {
            _maxRows = reg.getValue2();
        }
        // We no longer need the additional values for information, so reset
        // them so they are no longer serialized
        reg.resetValues();
        fastUpdate = "true".equals(ClientInfo.getProperties(null).getProperty(ClientInfo.RELAY_FAST_UPDATE));
    }

    protected void finalize() throws Throwable {
        if (!_isClosed) {
            close();
        }
    }

    public ResultSet executeQuery(String sql) throws SQLException {
        try {
            StreamingResultSet srs = (StreamingResultSet) _sink.process(_objectUid, new StatementQueryCommand(sql,
                    _resultSetType), true);
            srs.setStatement(this);
            srs.setCommandSink(_sink);
            _currentResultSet = srs;
            return srs;
        } catch (SQLException e) {
            throw e;
        } catch (Exception e) {
            throw SQLExceptionHelper.wrap(e);
        }
    }

    public int executeUpdate(String sql) throws SQLException {
        if (fastUpdate) {
	    	_sink.queue(_objectUid, new StatementUpdateCommand(sql), true);
			return 0;
        } else {
        	return _sink.processWithIntResult(_objectUid, new StatementUpdateCommand(sql));
        }
    }

    public void close() throws SQLException {
        try {
        	_sink.queue(_objectUid, DestroyCommand.INSTANCE, true);
        } catch (IllegalArgumentException e){
        	// _objectUid is invalid, which means that 
        	// this statement is not created yet on server and we want to close it
        	// so ignore this error
        }
        _isClosed = true;
    }

    public int getMaxFieldSize() throws SQLException {
        return _sink.processWithIntResult(_objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.STATEMENT,
                "getMaxFieldSize"));
    }

    public void setMaxFieldSize(int max) throws SQLException {
        _sink.process(_objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.STATEMENT, "setMaxFieldSize",
                new Object[] { Integer.valueOf(max) }, ParameterTypeCombinations.INT));
    }

    public int getMaxRows() throws SQLException {
        if (_maxRows < 0) {
            int result = _sink.processWithIntResult(_objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.STATEMENT,
                    "getMaxRows"));
            _maxRows = result;
        }

        return _maxRows;
    }

    public void setMaxRows(int max) throws SQLException {
        _sink.process(_objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.STATEMENT, "setMaxRows",
                new Object[] { Integer.valueOf(max) }, ParameterTypeCombinations.INT));
        _maxRows = max;
    }

    public void setEscapeProcessing(boolean enable) throws SQLException {
        _sink.process(_objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.STATEMENT, "setEscapeProcessing",
                new Object[] { enable ? Boolean.TRUE : Boolean.FALSE }, ParameterTypeCombinations.BOL));
    }

    public int getQueryTimeout() throws SQLException {
        if (_queryTimeout < 0) {
            int result = _sink.processWithIntResult(_objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.STATEMENT,
                    "getQueryTimeout"));
            _queryTimeout = result;
        }

        return _queryTimeout;
    }

    public void setQueryTimeout(int seconds) throws SQLException {
        _sink.process(_objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.STATEMENT, "setQueryTimeout",
                new Object[] { Integer.valueOf(seconds) }, ParameterTypeCombinations.INT));
        _queryTimeout = seconds;
    }

    public void cancel() throws SQLException {
        _sink.process(_objectUid, new StatementCancelCommand());
    }

    public SQLWarning getWarnings() throws SQLException {
        return (SQLWarning) _sink.process(_objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.STATEMENT, "getWarnings"));
    }

    public void clearWarnings() throws SQLException {
        _sink.process(_objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.STATEMENT, "clearWarnings"));
    }

    public void setCursorName(String name) throws SQLException {
        _sink.process(_objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.STATEMENT, "setCursorName",
                new Object[] { name }, ParameterTypeCombinations.STR));
    }

    public boolean execute(String sql) throws SQLException {
        // Reset the current ResultSet before executing this command
        _currentResultSet = null;

        return _sink.processWithBooleanResult(_objectUid, new StatementExecuteCommand(sql));
    }

    public ResultSet getResultSet() throws SQLException {
        if (_currentResultSet == null) {
            try {
                _currentResultSet = (StreamingResultSet) _sink.process(_objectUid,
                        new StatementGetResultSetCommand(), true);
                _currentResultSet.setStatement(this);
                _currentResultSet.setCommandSink(_sink);
            } catch (Exception e) {
                throw SQLExceptionHelper.wrap(e);
            }
        }

        return _currentResultSet;
    }

    public int getUpdateCount() throws SQLException {
        return _sink.processWithIntResult(_objectUid, CommandPool
                .getReflectiveCommand(JdbcInterfaceType.STATEMENT, "getUpdateCount"));
    }

    public boolean getMoreResults() throws SQLException {
        try {
            return _sink.processWithBooleanResult(_objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.STATEMENT,
                    "getMoreResults"));
        } finally {
            _currentResultSet = null;
        }
    }

    public void setFetchDirection(int direction) throws SQLException {
        _sink.process(_objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.STATEMENT, "setFetchDirection",
                new Object[] { Integer.valueOf(direction) }, ParameterTypeCombinations.INT));
    }

    public int getFetchDirection() throws SQLException {
        return _sink.processWithIntResult(_objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.STATEMENT,
                "getFetchDirection"));
    }

    public void setFetchSize(int rows) throws SQLException {
    	_sink.queue(_objectUid, new StatementSetFetchSizeCommand(rows), true);
    }

    public int getFetchSize() throws SQLException {
        return _sink.processWithIntResult(_objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.STATEMENT, "getFetchSize"));
    }

    public int getResultSetConcurrency() throws SQLException {
        return _sink.processWithIntResult(_objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.STATEMENT,
                "getResultSetConcurrency"));
    }

    public int getResultSetType() throws SQLException {
        return _sink.processWithIntResult(_objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.STATEMENT,
                "getResultSetType"));
    }

    public void addBatch(String sql) throws SQLException {
        _batchCollector.add(sql);
    }

    public void clearBatch() throws SQLException {
        _batchCollector.clear();
    }

    public int[] executeBatch() throws SQLException {
        String[] sql = (String[]) _batchCollector.toArray(new String[_batchCollector.size()]);
        int[] result = (int[]) _sink.process(_objectUid, new StatementExecuteBatchCommand(sql));
        _batchCollector.clear();
        return result;
    }

    public Connection getConnection() throws SQLException {
        return _connection;
    }

    public boolean getMoreResults(int current) throws SQLException {
        return _sink.processWithBooleanResult(_objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.STATEMENT,
                "getMoreResults", new Object[] { Integer.valueOf(current) }, ParameterTypeCombinations.INT));
    }

    public ResultSet getGeneratedKeys() throws SQLException {
        try {
            StreamingResultSet srs = (StreamingResultSet) _sink.process(_objectUid,
                    new StatementGetGeneratedKeysCommand(), true);
            srs.setStatement(this);
            srs.setCommandSink(_sink);
            return srs;
        } catch (Exception e) {
            throw SQLExceptionHelper.wrap(e);
        }
    }

    public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        return _sink.processWithIntResult(_objectUid, new StatementUpdateExtendedCommand(sql, autoGeneratedKeys));
    }

    public int executeUpdate(String sql, int columnIndexes[]) throws SQLException {
        return _sink.processWithIntResult(_objectUid, new StatementUpdateExtendedCommand(sql, columnIndexes));
    }

    public int executeUpdate(String sql, String columnNames[]) throws SQLException {
        return _sink.processWithIntResult(_objectUid, new StatementUpdateExtendedCommand(sql, columnNames));
    }

    public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
        return _sink.processWithBooleanResult(_objectUid, new StatementExecuteExtendedCommand(sql, autoGeneratedKeys));
    }

    public boolean execute(String sql, int columnIndexes[]) throws SQLException {
        return _sink.processWithBooleanResult(_objectUid, new StatementExecuteExtendedCommand(sql, columnIndexes));
    }

    public boolean execute(String sql, String columnNames[]) throws SQLException {
        return _sink.processWithBooleanResult(_objectUid, new StatementExecuteExtendedCommand(sql, columnNames));
    }

    public int getResultSetHoldability() throws SQLException {
        return _sink.processWithIntResult(_objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.STATEMENT,
                "getResultSetHoldability"));
    }

    /* start JDBC4 support */
    public boolean isClosed() throws SQLException {
        return _isClosed;
    }

    public void setPoolable(boolean poolable) throws SQLException {
        _sink.process(_objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.STATEMENT, "setPoolable",
                new Object[]{poolable ? Boolean.TRUE : Boolean.FALSE},
                ParameterTypeCombinations.BOL));
    }

    public boolean isPoolable() throws SQLException {
        return _sink.processWithBooleanResult(_objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.STATEMENT,
                "isPoolable"));
    }

    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return iface.isAssignableFrom(VirtualStatement.class);
    }

    public <T> T unwrap(Class<T> iface) throws SQLException {
        return (T)this;
    }
    /* end JDBC4 support */

    /* start JDK7 support */
    public void closeOnCompletion() throws SQLException {
        _isCloseOnCompletion = true;
    }

    public boolean isCloseOnCompletion() throws SQLException {
        return _isCloseOnCompletion;
    }
    /* end JDK7 support */
}
