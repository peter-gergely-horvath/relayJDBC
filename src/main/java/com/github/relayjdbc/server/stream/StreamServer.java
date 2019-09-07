package com.github.relayjdbc.server.stream;

import com.github.relayjdbc.server.CommandDispatcher;
import com.github.relayjdbc.server.command.CommandProcessor;
import com.github.relayjdbc.server.config.ConfigurationException;
import com.github.relayjdbc.server.config.VJdbcConfiguration;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

public class StreamServer {

    private final CommandDispatcher commandDispatcher;

    public StreamServer(InputStream configResourceInputStream, Properties configVariablesProps) throws ConfigurationException {

        VJdbcConfiguration.init(configResourceInputStream, configVariablesProps);

        commandDispatcher = new CommandDispatcher(CommandProcessor.getInstance());

    }

    public void handleRequest(InputStream inputStream, OutputStream outputStream) {

        commandDispatcher.dispatch(inputStream, outputStream);


    }



}
