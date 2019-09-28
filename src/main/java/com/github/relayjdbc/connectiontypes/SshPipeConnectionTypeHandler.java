package com.github.relayjdbc.connectiontypes;

import com.github.relayjdbc.protocol.transport.ssh.SshPipeTransport;

import java.sql.DriverPropertyInfo;
import java.util.Properties;

class SshPipeConnectionTypeHandler extends ConnectionTypeHandler {

    public static final long serialVersionUID = 1;

    SshPipeConnectionTypeHandler() {
        super("sshpipe:");
    }

    protected SshPipeTransport getTransport(String url, Properties props) {
        return new SshPipeTransport(url, props);
    }

    @Override
    public DriverPropertyInfo[] getJdbcDriverPropertyInfo(String relayUrl, Properties info) {

        DriverPropertyInfo userName = new DriverPropertyInfo(SshPipeTransport.SSH_USERNAME,
                info.getProperty(SshPipeTransport.SSH_USERNAME));
        userName.required = true;
        userName.description = "The username for the SSH connection";

        DriverPropertyInfo password = new DriverPropertyInfo(SshPipeTransport.SSH_PASSWORD,
                info.getProperty(SshPipeTransport.SSH_PASSWORD));
        password.required = true;
        password.description = "The password for the SSH connection";

        DriverPropertyInfo command = new DriverPropertyInfo(SshPipeTransport.SSH_REMOTE_COMMAND,
                info.getProperty(SshPipeTransport.SSH_REMOTE_COMMAND));
        command.required = true;
        command.description = "The command to execute in the remote SSH session, "
                        + "that starts the RelayJDBC server in stdio/BASE64 encoded pipe mode";

        DriverPropertyInfo strictHostChecking
                = new DriverPropertyInfo(SshPipeTransport.STRICT_HOST_KEY_CHECKING,
                info.getProperty(SshPipeTransport.STRICT_HOST_KEY_CHECKING,
                        SshPipeTransport.JSchConstants.DEFAULT_STRICT_HOST_KEY_CHECKING));

        strictHostChecking.required = false;
        strictHostChecking.description =
                "Should host identity be validated against the certificates of the local JVM?";
        strictHostChecking.choices = new String[]{"yes", "no"};

        return new DriverPropertyInfo[] {
                userName,
                password,
                command,
                strictHostChecking
        };
    }



}
