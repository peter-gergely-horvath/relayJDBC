package com.github.relayjdbc.protocol.kryo;

import com.github.relayjdbc.Version;

import java.util.zip.Deflater;

import static com.github.relayjdbc.servlet.ServletCommandSinkIdentifier.PROTOCOL_KRYO;

public class KryoProtocolConstants {
    public static final String PROTOCOL_VERSION = Version.version+PROTOCOL_KRYO;
    public static final int DEFAULT_COMPRESSION_MODE = Deflater.NO_COMPRESSION;
    public static final int DEFAULT_COMPRESSION_THRESHOLD = 1500;
}
