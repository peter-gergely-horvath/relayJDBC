package com.github.relayjdbc.server.servlet;

import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;

public class HttpServerMain {

    public static void main(String[] args) throws Exception {

        if (args.length != 1) {
            throw new IllegalArgumentException("Arguments expected: <http server port>");
        }
        /*
        TODO: implement HTTPS client for this and then enable
        if (args.length < 1 || args.length > 2) {
            throw new IllegalArgumentException("Arguments expected: <http server port> [https server port]");
        }
        */

        String serverPortString = args[0];
        Integer serverPort = Integer.valueOf(serverPortString);

        Server server = new Server();

        ServletContextHandler context = new ServletContextHandler(server, "/",
                ServletContextHandler.SESSIONS|ServletContextHandler.NO_SECURITY);
        context.setResourceBase(".");

        context.addServlet(KryoServletCommandSink.class, "/")
                .setInitParameter(AbstractServletCommandSink.INIT_PARAMETER_CONFIG_RESOURCE,"/relayjdbc-config.xml");
        server.setHandler(context);

        // HTTP Configuration
        HttpConfiguration httpConfiguration = new HttpConfiguration();


        /*
        TODO: implement HTTPS client for this and then enable

        if (args.length == 2) {
            String secureServerPortString = args[1];
            Integer securesServerPort = Integer.valueOf(secureServerPortString);

            httpConfiguration.setSecureScheme("https");
            httpConfiguration.setSecurePort(securesServerPort);
        }
        */

        httpConfiguration.setSendXPoweredBy(false);
        httpConfiguration.setSendServerVersion(false);

        // HTTP Connector
        ServerConnector http = new ServerConnector(server,new HttpConnectionFactory(httpConfiguration));
        http.setPort(serverPort);
        server.addConnector(http);

        server.start();
        // Wait for the server thread to stop
        server.join();
    }
}
