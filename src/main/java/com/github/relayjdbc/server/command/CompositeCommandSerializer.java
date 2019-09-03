package com.github.relayjdbc.server.command;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import com.github.relayjdbc.command.Command;
import com.github.relayjdbc.serial.UIDEx;

public class CompositeCommandSerializer extends Serializer<CompositeCommand> {

	@Override
	public void write(Kryo kryo, Output output, CompositeCommand object) {
		int size = object.size();
		output.writeInt(size);
		Command[] commands = object.getCommands();
		UIDEx[] uidexs = object.getUIDExs();
		for (int i=0; i<size; i++){
			kryo.writeObject(output, uidexs[i]);
			kryo.writeClassAndObject(output, commands[i]);			
		}
		
	}

	@Override
	public CompositeCommand read(Kryo kryo, Input input, Class<CompositeCommand> type) {
		int size = input.readInt();
		Command[] commands = new Command[size];
		UIDEx[] uidexs = new UIDEx[size];
		for (int i=0; i<size; i++){
			uidexs[i] = kryo.readObject(input, UIDEx.class);
			commands[i] = (Command) kryo.readClassAndObject(input);
		}
		return new CompositeCommand(commands, uidexs);
	}

}
