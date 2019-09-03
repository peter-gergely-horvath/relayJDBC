package com.github.relayjdbc.parameters;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import com.github.relayjdbc.serial.SerialRef;

public class RefParameterSerializer extends Serializer<RefParameter> {

	@Override
	public void write(Kryo kryo, Output output, RefParameter object) {
		kryo.writeObjectOrNull(output, object.getValue(), SerialRef.class);
	}

	@Override
	public RefParameter read(Kryo kryo, Input input, Class<RefParameter> type) {
		return new RefParameter(kryo.readObjectOrNull(input, SerialRef.class));
	}

}
