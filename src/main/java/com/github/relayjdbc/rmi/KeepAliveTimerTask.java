package com.github.relayjdbc.rmi;

import com.github.relayjdbc.command.Command;
import com.github.relayjdbc.command.CommandSinkListener;
import com.github.relayjdbc.command.DecoratedCommandSink;
import com.github.relayjdbc.command.PingCommand;
import com.github.relayjdbc.util.SQLExceptionHelper;

import java.sql.SQLException;
import java.util.TimerTask;

/**
 * This timer task will periodically notify the server with a dummy command, just to
 * keep the connection alive. This will prevent the RMI-Object to be garbage-collected when
 * there aren't any RMI-Calls for a specific time (lease value).
 */
public class KeepAliveTimerTask extends TimerTask implements CommandSinkListener {
    private static Command _dummyCommand = PingCommand.INSTANCE;
    private final DecoratedCommandSink _sink;
    private volatile boolean _ignoreNextPing = false;
    private boolean connectionAlive = true;

    public KeepAliveTimerTask(DecoratedCommandSink sink) {
        _sink = sink;
        _sink.setListener(this);
    }

    public void preExecution(Command cmd) {
        // Next ping can be ignored when there are commands processed
        // to the sink
        _ignoreNextPing = true;
    }

    public void postExecution(Command cmd) {
    }

    public void run() {
        try {
            if(_ignoreNextPing) {
                _ignoreNextPing = false;
            } else if (connectionAlive){
                _sink.process(null, _dummyCommand);
            }
        } catch(SQLException e) {
        	if (SQLExceptionHelper.CONNECTION_DOES_NOT_EXIST_STATE.equals(e.getSQLState())) {
        		// stop sending ping, because now it is useless
        		connectionAlive = false;
        	}
        	// Ignore exception, because there is nothing we can do here
        }
    }
}
