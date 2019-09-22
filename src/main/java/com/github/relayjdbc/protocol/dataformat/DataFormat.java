package com.github.relayjdbc.protocol.dataformat;

public interface DataFormat {
    Encoder getProtocolEncoder();

    Decoder getProtocolDecoder();

    void setCompressionMode(int newCompressionMode);

    void setCompressionThreshold(int compressionThreshold);
}
