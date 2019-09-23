package com.github.relayjdbc.server.base64pipe;

import com.github.relayjdbc.server.config.ConfigurationException;
import com.github.relayjdbc.server.stream.StreamServer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.util.Base64;
import java.util.Properties;

public class Base64PipeServerMain {

    private static Log logger = LogFactory.getLog(Base64PipeServerMain.class);

    public static void main(String[] args) throws IOException, ConfigurationException {
        new Base64PipeServerMain().runApp(args);
    }

    protected void runApp(String[] args) throws IOException, ConfigurationException {
        if (!(args.length == 1 || args.length == 2)) {
            throw new RuntimeException("Expected arguments <configuration file> [properties file]");
        }

        logger.info("Configuring server from: " + args[0]);

        InputStream configFileInputStream = getConfigFileStream(args[0]);

        Properties properties = new Properties();
        if (args.length == 2) {
            try(FileInputStream propertiesInputStream = new FileInputStream(args[0])) {
                properties.load(propertiesInputStream);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        StreamServer streamServer = new StreamServer(configFileInputStream, properties);

        try(BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(getStdIn()));
            PrintWriter response = new PrintWriter(getStdOut())) {

            while(!Thread.currentThread().isInterrupted()) {
                logger.info("Awaiting console input");
                String lineRead = bufferedReader.readLine();
                logger.info("Read line from input: " + lineRead);

                if (lineRead == null) {
                    break;
                }

                byte[] incoming = Base64.getDecoder().decode(lineRead);

                ByteArrayInputStream inputStream = new ByteArrayInputStream(incoming);

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

                streamServer.handleRequest(inputStream, byteArrayOutputStream);

                byteArrayOutputStream.flush();
                byte[] responseBytes = byteArrayOutputStream.toByteArray();

                String responseEncoded = Base64.getEncoder().encodeToString(responseBytes);

                logger.info("Writing response: " + responseEncoded);
                response.println(responseEncoded);
                response.flush();
            }
        }

    }

    protected InputStream getConfigFileStream(String fileToLoad) throws FileNotFoundException {
        return new FileInputStream(fileToLoad);
    }

    protected OutputStream getStdOut() {
        return new CloseSuppressingOutputStreamWrapper(System.out);
    }

    protected InputStream getStdIn() {
        return new CloseSuppressingInputStreamWrapper(System.in);
    }
}
