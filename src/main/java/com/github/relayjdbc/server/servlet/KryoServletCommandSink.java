package com.github.relayjdbc.server.servlet;

import com.github.relayjdbc.Version;
import com.github.relayjdbc.servlet.ServletCommandSinkIdentifier;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

public class KryoServletCommandSink extends AbstractServletCommandSink {

    private static final long serialVersionUID = 3257570624301249846L;


    private static Log logger = LogFactory.getLog(KryoServletCommandSink.class);

    public static final String PROTOCOL_VERSION = Version.version + ServletCommandSinkIdentifier.PROTOCOL_KRYO;

    public KryoServletCommandSink() {
    }

    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);

        logger.info("KryoServletCommandSink has been initialized");
    }

    public void destroy() {
    }

    protected void doPost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
            throws ServletException {

        handleRequest(httpServletRequest, httpServletResponse);
    }

    private void handleRequest(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
            throws ServletException {

        try {
            // for testing purposes emulate long ping times between client and server
            if (emulatePingTime > 0L) {
                Thread.sleep(emulatePingTime);
            }

            ServletInputStream inputStream = httpServletRequest.getInputStream();
            OutputStream outputStream = httpServletResponse.getOutputStream();

            commandDispatcher.dispatch(inputStream, outputStream);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ServletException(e);
        } catch (IOException e) {
            throw new ServletException(e);
        }
    }

}
