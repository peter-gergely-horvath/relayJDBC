package com.github.relayjdbc.server;

import com.github.relayjdbc.server.servlet.AbstractServletCommandSink;
import com.github.relayjdbc.server.servlet.KryoServletCommandSink;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;

public class Main {

    public static void main(String[] args) throws Exception {

        String serverPortString = args[0];
        Integer serverPort = Integer.valueOf(serverPortString);

        Server server = new Server();

        ServletContextHandler context = new ServletContextHandler(server, "/",
                ServletContextHandler.SESSIONS|ServletContextHandler.NO_SECURITY);
        context.setResourceBase(".");

        context.addServlet(KryoServletCommandSink.class, "/")
                .setInitParameter(AbstractServletCommandSink.INIT_PARAMETER_CONFIG_RESOURCE,"/vjdbc-config.xml");
        server.setHandler(context);

        // HTTP Configuration
        HttpConfiguration httpConfiguration = new HttpConfiguration();
        httpConfiguration.setSecureScheme("https");
        httpConfiguration.setSecurePort(8443);
        httpConfiguration.setSendXPoweredBy(true);
        httpConfiguration.setSendServerVersion(true);

        // HTTP Connector
        ServerConnector http = new ServerConnector(server,new HttpConnectionFactory(httpConfiguration));
        http.setPort(serverPort);
        server.addConnector(http);

        server.start();
        // Wait for the server thread to stop (optional)
        server.join();
    }
}
