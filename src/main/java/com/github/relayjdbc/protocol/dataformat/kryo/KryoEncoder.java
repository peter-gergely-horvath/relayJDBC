package com.github.relayjdbc.protocol.dataformat.kryo;

import com.github.relayjdbc.protocol.ProtocolConstants;
import com.github.relayjdbc.protocol.dataformat.Encoder;
import com.github.relayjdbc.protocol.messages.ExecuteCommandRequest;
import com.github.relayjdbc.protocol.messages.ConnectionRequest;
import com.github.relayjdbc.serial.CallingContext;
import com.github.relayjdbc.util.DeflatingOutput;
import com.github.relayjdbc.util.KryoFactory;

import java.io.OutputStream;

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
        try(DeflatingOutput output = new DeflatingOutput(outputStream)) {

            encodeHeader(output, ProtocolConstants.CONNECT_OPERATION);

            kryo.writeObject(output, connectionRequest.getDatabase());
            kryo.writeObject(output, connectionRequest.getProps());
            kryo.writeObject(output, connectionRequest.getClientInfo());
            kryo.writeObjectOrNull(output, connectionRequest.getCtx(), CallingContext.class);
            output.flush();
        }
    }

    @Override
    public void encode(OutputStream outputStream, ExecuteCommandRequest executeCommandRequest) {

        try(DeflatingOutput output = new DeflatingOutput(outputStream, _compressionMode, _compressionThreshold)) {

            encodeHeader(output, ProtocolConstants.PROCESS_OPERATION);

            kryo.writeObjectOrNull(output, executeCommandRequest.getConnuid(), Long.class);
            kryo.writeObjectOrNull(output, executeCommandRequest.getUid(), Long.class);
            kryo.writeClassAndObject(output, executeCommandRequest.getCmd());
            kryo.writeObjectOrNull(output, executeCommandRequest.getCtx(), CallingContext.class);
            output.flush();
        };
    }

    private void encodeHeader(DeflatingOutput output, String operation) {
        kryo.writeObject(output, ProtocolConstants.MAGIC);
        kryo.writeObject(output, KryoDataFormatConstants.PROTOCOL_VERSION);
        kryo.writeObject(output, operation);
    }


}
