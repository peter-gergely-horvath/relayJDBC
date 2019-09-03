// VJDBC - Virtual JDBC
// Written by Hunter Payne
// Website: http://vjdbc.sourceforge.net

package com.github.relayjdbc;

import com.github.relayjdbc.serial.UIDEx;

/**
 * Indicates that an object knows the id it wants to be registered under
 */
public interface Registerable {

    public UIDEx getReg();
}
