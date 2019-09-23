package com.github.relayjdbc.server;

import com.github.relayjdbc.server.base64pipe.Base64PipeServerMain;
import com.github.relayjdbc.server.servlet.HttpServerMain;

public final class Main {

    private static final int EXIT_CODE_SERVER_START_FAILED = 1;

    private Main() {
        // no external instances
    }

    public static void main(String[] args) {

        try {
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

    private static String[] dropServerType(String[] args) {

        if(args.length == 1) {
            return new String[0];
        }

        String[] serverArgs = new String[args.length - 1];

        System.arraycopy(args, 1, serverArgs, 0, args.length - 1);

        return serverArgs;
    }
}
