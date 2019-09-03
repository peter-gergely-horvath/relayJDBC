//VJDBC - Virtual JDBC
//Written by Michael Link
//Website: http://vjdbc.sourceforge.net

package com.github.relayjdbc.serial;
@Deprecated
interface ArrayAccess {
    Object getValue(Object array, int index, boolean[] nullFlags);
}
