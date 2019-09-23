package com.github.relayjdbc.server;

import com.github.relayjdbc.server.base64pipe.Base64PipeServerMain;
import com.github.relayjdbc.server.servlet.HttpServerMain;

import java.io.File;

public final class Main {

    private static final int EXIT_CODE_SERVER_START_FAILED = 1;

    private static final String LOG_CONFIG_FILE_PATH_XML = "./relay-jdbc.log4j.xml";
    private static final String LOG_CONFIG_FILE_PATH_PROPERTIES = "./relay-jdbc.log4j.properties";

    private Main() {
        // no external instances
    }

    public static void main(String[] args) {

        try {
            configureLogging();

            if (args.length == 0) {
                throw new IllegalArgumentException("At least one argument, server type is required");
            }

            String serverTypeString = args[0];

            ServerType serverType = ServerType.fromString(serverTypeString);

            String[] serverMainArgs = dropServerType(args);

            switch (serverType) {
                case HTTP:
                    HttpServerMain.main(serverMainArgs);

                    break;


                case BASE64_PIPE:
                    Base64PipeServerMain.main(serverMainArgs);

                    break;


                default:
                    throw new IllegalArgumentException("Unknown server type specified: " + serverType);
            }


        } catch (Exception ex) {
            System.err.println("Server start failed");

            ex.printStackTrace();

            System.exit(EXIT_CODE_SERVER_START_FAILED);
        }
    }

    private static void configureLogging() {

        if (System.getProperty("log4j.configuration") != null) {
            // we accept user-specified external configuration files
            return;
        }

        if (new File(LOG_CONFIG_FILE_PATH_XML).exists()) {
            System.setProperty("log4j.configuration", "file:" + LOG_CONFIG_FILE_PATH_XML);
        } else {
            if (new File(LOG_CONFIG_FILE_PATH_PROPERTIES).exists()) {
                System.setProperty("log4j.configuration", "file:" + LOG_CONFIG_FILE_PATH_PROPERTIES);
            } else {
                throw new RuntimeException("Logging must be configured, either through log4j.configuration property or "
                                + "a log configuration file named '"
                                + LOG_CONFIG_FILE_PATH_XML +"' or '"
                                + LOG_CONFIG_FILE_PATH_PROPERTIES +"'");
            }
        }
    }

    private static String[] dropServerType(String[] args) {

        if(args.length == 1) {
            return new String[0];
        }

        String[] serverArgs = new String[args.length - 1];

        System.arraycopy(args, 1, serverArgs, 0, args.length - 1);

        return serverArgs;
    }
}
