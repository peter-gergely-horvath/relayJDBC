package com.github.relayjdbc.protocol.dataformat.kryo;

import com.github.relayjdbc.protocol.dataformat.Decoder;
import com.github.relayjdbc.protocol.dataformat.Encoder;
import com.github.relayjdbc.protocol.dataformat.DataFormat;
import com.github.relayjdbc.util.KryoFactory;

import java.util.concurrent.atomic.AtomicInteger;

public class KryoDataFormat implements DataFormat {

    private final AtomicInteger compressionMode;
    private final AtomicInteger compressionThreshold;

    public KryoDataFormat(int compressionMode, int compressionThreshold) {
        this.compressionMode = new AtomicInteger(compressionMode);
        this.compressionThreshold = new AtomicInteger(compressionThreshold);
    }

    @Override
    public Encoder getProtocolEncoder() {
        return new KryoEncoder(compressionMode.get(), compressionThreshold.get(), KryoFactory.getInstance());
    }

    @Override
    public Decoder getProtocolDecoder() {
        return new KryoDecoder(KryoFactory.getInstance());
    }


    @Override
    public void setCompressionMode(int newCompressionMode) {
        this.compressionMode.set(newCompressionMode);
    }

    @Override
    public void setCompressionThreshold(int newCompressionThreshold) {
        this.compressionThreshold.set(newCompressionThreshold);
    }

}
