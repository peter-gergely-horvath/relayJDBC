package com.github.relayjdbc.protocol;

import java.io.InputStream;

public interface Decoder extends AutoCloseable{
    Object readObject(InputStream is);
}
