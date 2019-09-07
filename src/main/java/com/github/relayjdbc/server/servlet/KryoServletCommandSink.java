// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package com.github.relayjdbc.server.servlet;

import com.github.relayjdbc.Version;
import com.github.relayjdbc.servlet.ServletCommandSinkIdentifier;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

public class KryoServletCommandSink extends AbstractServletCommandSink {

    private static final long serialVersionUID = 3257570624301249846L;
    private static Log _logger = LogFactory.getLog(KryoServletCommandSink.class);

    public static final String PROTOCOL_VERSION = Version.version + ServletCommandSinkIdentifier.PROTOCOL_KRYO;


    public KryoServletCommandSink() {
    }

    public void destroy() {
    }

    protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException {
        handleRequest(httpServletRequest, httpServletResponse);
    }

    protected void doPost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException {
        handleRequest(httpServletRequest, httpServletResponse);
    }

    private void handleRequest(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException {
        try {
            // for testing purposes emulate long ping times between client and server
            if (emulatePingTime > 0L) {
                Thread.sleep(emulatePingTime);
            }

            ServletInputStream inputStream = httpServletRequest.getInputStream();
            OutputStream os = httpServletResponse.getOutputStream();

            commandDispatcher.dispatch(inputStream, os);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ServletException(e);
        } catch (IOException e) {
            throw new ServletException(e);
        }
    }

}
