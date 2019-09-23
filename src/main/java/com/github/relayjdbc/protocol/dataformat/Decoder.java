package com.github.relayjdbc.protocol.dataformat;

import java.io.InputStream;

public interface Decoder extends AutoCloseable{
    Object readObject(InputStream is) throws DecodeException;
}
