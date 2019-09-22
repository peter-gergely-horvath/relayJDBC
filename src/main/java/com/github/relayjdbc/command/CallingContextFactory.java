package com.github.relayjdbc.command;

import com.github.relayjdbc.serial.CallingContext;

/**
 * A CallingContextFactory creates CallingContext objects. 
 * @author Mike
 */
public interface CallingContextFactory {
    CallingContext create();
}
