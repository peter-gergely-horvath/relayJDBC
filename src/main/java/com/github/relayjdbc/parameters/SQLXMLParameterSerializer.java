package com.github.relayjdbc.parameters;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import com.github.relayjdbc.serial.SerialSQLXML;

public class SQLXMLParameterSerializer extends Serializer<SQLXMLParameter> {

	@Override
	public void write(Kryo kryo, Output output, SQLXMLParameter object) {
		kryo.writeObjectOrNull(output, object.getValue(), SerialSQLXML.class);
	}

	@Override
	public SQLXMLParameter read(Kryo kryo, Input input, Class<SQLXMLParameter> type) {
		return new SQLXMLParameter(kryo.readObjectOrNull(input, SerialSQLXML.class));
	}

}
