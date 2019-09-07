// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package com.github.relayjdbc.util;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Utility class for closing stream securely.
 * @author Mike
 *
 */
public final class StreamCloser {
    private StreamCloser() {
    }
    
    /**
     * Closes a Closeable.
     * @param closeable Closeable to close
     */
    public static void close(Closeable closeable) {
        if(closeable != null) {
            try {
                closeable.close();
            }
            catch(IOException e) {
            }
        }
    }
}
