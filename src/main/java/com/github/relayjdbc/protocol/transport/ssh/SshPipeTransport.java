package com.github.relayjdbc.protocol.transport.ssh;

import com.github.relayjdbc.server.base64pipe.CloseSuppressingInputStreamWrapper;
import com.github.relayjdbc.server.base64pipe.CloseSuppressingOutputStreamWrapper;
import com.github.relayjdbc.protocol.transport.Transport;
import com.github.relayjdbc.protocol.transport.TransportChannel;
import com.github.relayjdbc.util.StreamCloser;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

public class SshPipeTransport implements Transport {

    private static final String SSH_USERNAME = "ssh.username";
    private static final String SSH_PASSWORD = "ssh.password";

    private static final String SSH_REMOTE_COMMAND = "ssh.remoteCommand";

    private static final String STRICT_HOST_KEY_CHECKING = "ssh.strictHostKeyChecking";


    private static final class JSchConstants {

        private static final String JSCH_STRICT_HOST_KEY_CHECKING = "StrictHostKeyChecking";
        private static final String DISABLE_STRICT_HOST_KEY_CHECKING = "no";
    }


    private final String url;
    private final Properties properties;

    private final Session session;

    private final ChannelExec channel;
    private final SshTransportChannel sshTransportChannel;
    private final InputStream inputStream;
    private final OutputStream outputStream;

    public SshPipeTransport(String urlString, Properties properties) {
        try {
            this.url = urlString;
            this.properties = properties;

            final String user = getRequiredProperty(properties, SSH_USERNAME);
            final String password = getRequiredProperty(properties, SSH_PASSWORD);
            final String remoteCommand = getRequiredProperty(properties, SSH_REMOTE_COMMAND);

            String strictHostCheckingProperty =
                    properties.getProperty(STRICT_HOST_KEY_CHECKING, JSchConstants.DISABLE_STRICT_HOST_KEY_CHECKING);


            final String host = url.split(":")[0];
            final int port = Integer.parseInt(url.split(":")[1]);


            JSch jsch = new JSch();
            session = jsch.getSession(user, host, port);

            session.setPassword(password);

            Properties config = new Properties();

            config.put(JSchConstants.JSCH_STRICT_HOST_KEY_CHECKING, strictHostCheckingProperty);
            session.setConfig(config);

            session.connect();

            channel = (ChannelExec) session.openChannel("exec");

            channel.setCommand(remoteCommand);

            inputStream = channel.getInputStream();
            outputStream = channel.getOutputStream();

            sshTransportChannel = new SshTransportChannel(
                    new CloseSuppressingInputStreamWrapper(inputStream),
                    new CloseSuppressingOutputStreamWrapper(outputStream));

            channel.connect();

        } catch (JSchException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getRequiredProperty(Properties properties, String propertyName) {
        String propertyValue = properties.getProperty(propertyName);
        if (propertyValue == null) {
            throw new RuntimeException("Required property is not found: " + propertyName);
        }
        return propertyValue;
    }

    @Override
    public void close() {
        StreamCloser.close(inputStream);
        StreamCloser.close(outputStream);

        channel.disconnect();
        session.disconnect();
    }

    @Override
    public TransportChannel getTransportChannel() throws IOException {
        return sshTransportChannel;
    }

}
