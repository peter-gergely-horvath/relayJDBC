package com.github.relayjdbc.protocol.dataformat.kryo;

import com.esotericsoftware.kryo.KryoException;
import com.github.relayjdbc.protocol.dataformat.DecodeException;
import com.github.relayjdbc.protocol.dataformat.Decoder;
import com.github.relayjdbc.util.InflatingInput;
import com.github.relayjdbc.util.KryoFactory;

import java.io.InputStream;

class KryoDecoder extends KryoSupport implements Decoder {

    KryoDecoder(KryoFactory kryoFactory) {
        super(kryoFactory);
    }

    @Override
    public Object readObject(InputStream is) throws DecodeException {
        try (InflatingInput input = new InflatingInput(is)) {
            return kryo.readClassAndObject(input);
        } catch (KryoException ex) {
            throw new DecodeException("Failed to decode: " + ex.getMessage(), ex);
        }
    }
}
