package com.github.relayjdbc.protocol.kryo;

import com.github.relayjdbc.protocol.Decoder;
import com.github.relayjdbc.util.InflatingInput;
import com.github.relayjdbc.util.KryoFactory;

import java.io.InputStream;

class KryoDecoder extends KryoSupport implements Decoder {

    KryoDecoder(KryoFactory kryoFactory) {
        super(kryoFactory);
    }

    @Override
    public Object readObject(InputStream is) {
        try (InflatingInput input = new InflatingInput(is)) {
            return kryo.readClassAndObject(input);
        }
    }
}
