package com.github.relayjdbc.server.stdio;

import com.github.relayjdbc.server.config.ConfigurationException;
import com.github.relayjdbc.server.stream.StreamServer;

import java.io.*;
import java.util.Base64;
import java.util.Properties;

public class StdIOServer {

    public static void main(String[] args) throws IOException, ConfigurationException {
        new StdIOServer().runApp(args);
    }

    protected void runApp(String[] args) throws IOException, ConfigurationException {
        if (!(args.length == 1 || args.length == 2)) {
            throw new RuntimeException("Expected arguments <configuration file> [properties file]");
        }

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
            OutputStream stdOut = getStdOut()) {

            while(!Thread.currentThread().isInterrupted()) {
                String lineRead = bufferedReader.readLine();
                if (lineRead == null) {
                    break;
                }

                byte[] incoming = Base64.getDecoder().decode(lineRead);

                ByteArrayInputStream inputStream = new ByteArrayInputStream(incoming);

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

                streamServer.handleRequest(inputStream, byteArrayOutputStream);

                byteArrayOutputStream.flush();
                byte[] responseBytes = byteArrayOutputStream.toByteArray();


                byte[] responseEncoded = Base64.getEncoder().encode(responseBytes);

                stdOut.write(responseEncoded);
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
