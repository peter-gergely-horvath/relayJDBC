package com.github.relayjdbc.transport.ssh;

import com.github.relayjdbc.server.base64pipe.CloseSuppressingInputStreamWrapper;
import com.github.relayjdbc.server.base64pipe.CloseSuppressingOutputStreamWrapper;
import com.github.relayjdbc.transport.Transport;
import com.github.relayjdbc.transport.TransportChannel;
import com.github.relayjdbc.util.StreamCloser;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.*;
import java.util.Objects;
import java.util.Properties;

public class SshPipeTransport implements Transport {

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

            JSch jsch = new JSch();

            String user = properties.getProperty("ssh.username");
            String host = url.split(":")[0];
            int port = Integer.parseInt(url.split(":")[1]);

            session = jsch.getSession(user, host, port);
            session.setPassword(properties.getProperty("ssh.password"));


            // disabling StrictHostKeyChecking may help to make connection but makes it insecure
            // see http://stackoverflow.com/questions/30178936/jsch-sftp-security-with-session-setconfigstricthostkeychecking-no
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);

            session.connect();

            channel = (ChannelExec) session.openChannel("exec");

            String remoteCommand = properties.getProperty("ssh.remoteCommand");
            Objects.requireNonNull(remoteCommand, "ssh.remoteCommand");
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
