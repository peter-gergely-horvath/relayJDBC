package com.github.relayjdbc.server.servlet;

import com.github.relayjdbc.server.CommandDispatcher;
import com.github.relayjdbc.server.command.CommandProcessor;
import com.github.relayjdbc.server.config.ConfigurationException;
import com.github.relayjdbc.server.config.VJdbcConfiguration;
import com.github.relayjdbc.util.StreamCloser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public abstract class AbstractServletCommandSink extends HttpServlet {

    private static Log _logger = LogFactory.getLog(AbstractServletCommandSink.class);

    public static final String INIT_PARAMETER_CONFIG_RESOURCE = "config-resource";
    protected static final String INIT_PARAMETER_CONFIG_VARIABLES = "config-variables";
    protected static final String DEFAULT_CONFIG_RESOURCE = "/WEB-INF/relayjdbc-config.xml";

    protected CommandProcessor _processor;
    protected long emulatePingTime = 0L;
    protected CommandDispatcher commandDispatcher;

    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);

        String configResource = servletConfig.getInitParameter(INIT_PARAMETER_CONFIG_RESOURCE);

        // Use default location when nothing is configured
        if (configResource == null) {
            configResource = DEFAULT_CONFIG_RESOURCE;
        }

        ServletContext ctx = servletConfig.getServletContext();

        _logger.info("Trying to get config resource " + configResource + "...");
        InputStream configResourceInputStream = ctx.getResourceAsStream(configResource);
        if (null == configResourceInputStream) {
            try {
                configResourceInputStream =
                        new FileInputStream(ctx.getRealPath(configResource));
            } catch (FileNotFoundException fnfe) {
            }
        }

        if (configResourceInputStream == null) {
            try {
                // check if configuration is already initialized
                VJdbcConfiguration.singleton(); // throws RuntimeException if not initialized
            } catch (RuntimeException e) {
                String msg = "relayjdbc-configuration " + configResource + " not found !";
                _logger.error(msg);
                throw new ServletException(msg);
            }
        } else {

            // Are config variables specifiec ?
            String configVariables = servletConfig.getInitParameter(INIT_PARAMETER_CONFIG_VARIABLES);
            Properties configVariablesProps = null;

            if (configVariables != null) {
                _logger.info("... using variables specified in " + configVariables);

                InputStream configVariablesInputStream = null;

                try {
                    configVariablesInputStream = ctx.getResourceAsStream(configVariables);
                    if (null == configVariablesInputStream) {
                        configVariablesInputStream =
                                new FileInputStream(ctx.getRealPath(configVariables));
                    }

                    configVariablesProps = new Properties();
                    configVariablesProps.load(configVariablesInputStream);
                } catch (IOException e) {
                    String msg = "Reading of configuration variables failed";
                    _logger.error(msg, e);
                    throw new ServletException(msg, e);
                } finally {
                    StreamCloser.close(configVariablesInputStream);
                }
            }

            try {
                _logger.info("Initialize relayjdbc-configuration");
                VJdbcConfiguration.init(configResourceInputStream, configVariablesProps);
            } catch (ConfigurationException e) {
                _logger.error("Initialization failed", e);
                throw new ServletException("VJDBC-Initialization failed", e);
            } finally {
                StreamCloser.close(configResourceInputStream);
            }
        }

        commandDispatcher = new CommandDispatcher(CommandProcessor.getInstance());
        emulatePingTime = VJdbcConfiguration.singleton().getEmulatePingTime();
    }
}
