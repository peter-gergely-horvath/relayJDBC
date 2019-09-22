package com.github.relayjdbc.protocol.messages;

import com.github.relayjdbc.command.Command;
import com.github.relayjdbc.serial.CallingContext;

public class ExecuteCommandRequest {
    private final Long connuid;
    private final Long uid;
    private final Command cmd;
    private final CallingContext ctx;

    public ExecuteCommandRequest(Long connuid, Long uid, Command cmd, CallingContext ctx) {
        this.connuid = connuid;
        this.uid = uid;
        this.cmd = cmd;
        this.ctx = ctx;
    }

    public Long getConnuid() {
        return connuid;
    }

    public Long getUid() {
        return uid;
    }

    public Command getCmd() {
        return cmd;
    }

    public CallingContext getCtx() {
        return ctx;
    }
}
