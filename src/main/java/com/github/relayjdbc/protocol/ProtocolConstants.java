package com.github.relayjdbc.protocol;


public class ProtocolConstants {

	private ProtocolConstants() {
		// no instances allowed
	}

	public static final String MAGIC = "RelayJdbc";

    public static final String CONNECT_OPERATION = "connect";
    public static final String PROCESS_OPERATION = "process";

	public static final String PROTOCOL_KRYO = "k";
}
