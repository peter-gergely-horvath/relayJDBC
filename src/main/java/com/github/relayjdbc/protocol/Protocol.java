package com.github.relayjdbc.protocol;

public interface Protocol {
    Encoder getProtocolEncoder();

    Decoder getProtocolDecoder();

    void setCompressionMode(int newCompressionMode);

    void setCompressionThreshold(int compressionThreshold);
}
