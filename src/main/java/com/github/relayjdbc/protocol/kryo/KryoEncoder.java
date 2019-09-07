package com.github.relayjdbc.protocol.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.github.relayjdbc.command.Command;
import com.github.relayjdbc.protocol.ProtocolConstants;
import com.github.relayjdbc.protocol.Encoder;
import com.github.relayjdbc.serial.CallingContext;
import com.github.relayjdbc.util.DeflatingOutput;
import com.github.relayjdbc.util.InflatingInput;
import com.github.relayjdbc.util.KryoFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import static com.github.relayjdbc.servlet.ServletCommandSinkIdentifier.CONNECT_COMMAND;
import static com.github.relayjdbc.servlet.ServletCommandSinkIdentifier.PROCESS_COMMAND;

class KryoEncoder extends KryoSupport implements Encoder {

    private final int _compressionMode;
    private final int _compressionThreshold;

    KryoEncoder(int _compressionMode, int _compressionThreshold, KryoFactory kryoFactory) {
        super(kryoFactory);

        this._compressionMode = _compressionMode;
        this._compressionThreshold = _compressionThreshold;
    }

    @Override
    public void writeConnect(OutputStream outputStream,
                             String database, Properties props, Properties clientInfo, CallingContext ctx)             {
        try(DeflatingOutput deflatingOutput = new DeflatingOutput(outputStream)) {

            kryo.writeObject(deflatingOutput, ProtocolConstants.MAGIC);
            kryo.writeObject(deflatingOutput, KryoProtocolConstants.PROTOCOL_VERSION);
            kryo.writeObject(deflatingOutput, CONNECT_COMMAND);

            kryo.writeObject(deflatingOutput, database);
            kryo.writeObject(deflatingOutput, props);
            kryo.writeObject(deflatingOutput, clientInfo);
            kryo.writeObjectOrNull(deflatingOutput, ctx, CallingContext.class);
            deflatingOutput.flush();
        }
    }

    @Override
    public void writePerform(OutputStream outputStream, Long connuid, Long uid, Command cmd, CallingContext ctx) {

        try(DeflatingOutput output = new DeflatingOutput(outputStream, _compressionMode, _compressionThreshold)) {
            kryo.writeObject(output, ProtocolConstants.MAGIC);
            kryo.writeObject(output, KryoProtocolConstants.PROTOCOL_VERSION);
            kryo.writeObject(output, PROCESS_COMMAND);

            kryo.writeObjectOrNull(output, connuid, Long.class);
            kryo.writeObjectOrNull(output, uid, Long.class);
            kryo.writeClassAndObject(output, cmd);
            kryo.writeObjectOrNull(output, ctx, CallingContext.class);
            output.flush();
        };
    }


}
