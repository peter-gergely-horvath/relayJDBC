package com.github.relayjdbc.protocol.dataformat.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.github.relayjdbc.util.KryoFactory;

abstract class KryoSupport implements AutoCloseable {
    protected final Kryo kryo;
    protected final KryoFactory kryoFactory;

    KryoSupport(KryoFactory kryoFactory) {
        this.kryo = kryoFactory.getKryo();
        this.kryoFactory = KryoFactory.getInstance();
    }

    @Override
    public void close() throws Exception {
        kryoFactory.releaseKryo(kryo);
    }
}
