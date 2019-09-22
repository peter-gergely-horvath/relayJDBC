package com.github.relayjdbc.protocol.dataformat.kryo;

import com.github.relayjdbc.Version;

import java.util.zip.Deflater;

import static com.github.relayjdbc.servlet.ServletCommandSinkIdentifier.PROTOCOL_KRYO;

public final class KryoDataFormatConstants {

    private KryoDataFormatConstants() {
        throw new AssertionError("no external instances allowed");
    }

    public static final String PROTOCOL_VERSION = Version.version+PROTOCOL_KRYO;
    public static final int DEFAULT_COMPRESSION_MODE = Deflater.NO_COMPRESSION;
    public static final int DEFAULT_COMPRESSION_THRESHOLD = 1500;
}
