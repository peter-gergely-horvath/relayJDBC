package com.github.relayjdbc.protocol.dataformat.kryo;

import com.github.relayjdbc.protocol.dataformat.ProtocolConstants;
import com.github.relayjdbc.protocol.dataformat.Encoder;
import com.github.relayjdbc.protocol.messages.ExecuteCommandRequest;
import com.github.relayjdbc.protocol.messages.ConnectionRequest;
import com.github.relayjdbc.serial.CallingContext;
import com.github.relayjdbc.util.DeflatingOutput;
import com.github.relayjdbc.util.KryoFactory;

import java.io.OutputStream;

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
    public void encode(OutputStream outputStream, ConnectionRequest connectionRequest)             {
        try(DeflatingOutput deflatingOutput = new DeflatingOutput(outputStream)) {

            kryo.writeObject(deflatingOutput, ProtocolConstants.MAGIC);
            kryo.writeObject(deflatingOutput, KryoDataFormatConstants.PROTOCOL_VERSION);
            kryo.writeObject(deflatingOutput, CONNECT_COMMAND);

            kryo.writeObject(deflatingOutput, connectionRequest.getDatabase());
            kryo.writeObject(deflatingOutput, connectionRequest.getProps());
            kryo.writeObject(deflatingOutput, connectionRequest.getClientInfo());
            kryo.writeObjectOrNull(deflatingOutput, connectionRequest.getCtx(), CallingContext.class);
            deflatingOutput.flush();
        }
    }

    @Override
    public void encode(OutputStream outputStream, ExecuteCommandRequest executeCommandRequest) {

        try(DeflatingOutput output = new DeflatingOutput(outputStream, _compressionMode, _compressionThreshold)) {
            kryo.writeObject(output, ProtocolConstants.MAGIC);
            kryo.writeObject(output, KryoDataFormatConstants.PROTOCOL_VERSION);
            kryo.writeObject(output, PROCESS_COMMAND);

            kryo.writeObjectOrNull(output, executeCommandRequest.getConnuid(), Long.class);
            kryo.writeObjectOrNull(output, executeCommandRequest.getUid(), Long.class);
            kryo.writeClassAndObject(output, executeCommandRequest.getCmd());
            kryo.writeObjectOrNull(output, executeCommandRequest.getCtx(), CallingContext.class);
            output.flush();
        };
    }


}
