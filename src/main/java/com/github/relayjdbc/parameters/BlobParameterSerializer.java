package com.github.relayjdbc.parameters;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import com.github.relayjdbc.serial.SerialBlob;

public class BlobParameterSerializer extends Serializer<BlobParameter> {

	@Override
	public void write(Kryo kryo, Output output, BlobParameter object) {
		kryo.writeObjectOrNull(output, object.getValue(), SerialBlob.class);
	}

	@Override
	public BlobParameter read(Kryo kryo, Input input, Class<BlobParameter> type) {
		return new BlobParameter(kryo.readObjectOrNull(input, SerialBlob.class));
	}

}
