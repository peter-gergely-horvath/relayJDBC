package com.github.relayjdbc.command;

/**
 * Interface for objects which are interested what is happening in the command sink.
 */
public interface CommandSinkListener {
    void preExecution(Command cmd);

    void postExecution(Command cmd);
}
