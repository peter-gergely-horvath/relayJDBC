package com.github.relayjdbc.util;

import java.sql.BatchUpdateException;
import java.sql.DataTruncation;
import java.sql.SQLClientInfoException;
import java.sql.SQLDataException;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.SQLInvalidAuthorizationSpecException;
import java.sql.SQLNonTransientConnectionException;
import java.sql.SQLNonTransientException;
import java.sql.SQLRecoverableException;
import java.sql.SQLSyntaxErrorException;
import java.sql.SQLTimeoutException;
import java.sql.SQLTransactionRollbackException;
import java.sql.SQLTransientConnectionException;
import java.sql.SQLTransientException;
import java.sql.SQLWarning;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.github.relayjdbc.server.command.CompositeCommand;
import com.github.relayjdbc.server.command.CompositeCommandSerializer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.JavaSerializer;

import com.github.relayjdbc.VJdbcException;
import com.github.relayjdbc.command.CallableStatementGetArrayCommand;
import com.github.relayjdbc.command.CallableStatementGetBlobCommand;
import com.github.relayjdbc.command.CallableStatementGetCharacterStreamCommand;
import com.github.relayjdbc.command.CallableStatementGetClobCommand;
import com.github.relayjdbc.command.CallableStatementGetNCharacterStreamCommand;
import com.github.relayjdbc.command.CallableStatementGetNClobCommand;
import com.github.relayjdbc.command.CallableStatementGetObjectCommand;
import com.github.relayjdbc.command.CallableStatementGetRefCommand;
import com.github.relayjdbc.command.CallableStatementGetSQLXMLCommand;
import com.github.relayjdbc.command.CallableStatementSetAsciiStreamCommand;
import com.github.relayjdbc.command.CallableStatementSetBinaryStreamCommand;
import com.github.relayjdbc.command.CallableStatementSetBlobCommand;
import com.github.relayjdbc.command.CallableStatementSetCharacterStreamCommand;
import com.github.relayjdbc.command.CallableStatementSetClobCommand;
import com.github.relayjdbc.command.CallableStatementSetNCharacterStreamCommand;
import com.github.relayjdbc.command.CallableStatementSetNClobCommand;
import com.github.relayjdbc.command.CallableStatementSetObjectCommand;
import com.github.relayjdbc.command.CallableStatementSetRowIdCommand;
import com.github.relayjdbc.command.CallableStatementSetSQLXMLCommand;
import com.github.relayjdbc.command.ConnectionCommitCommand;
import com.github.relayjdbc.command.ConnectionCommitCommandSerializer;
import com.github.relayjdbc.command.ConnectionCreateStatementCommand;
import com.github.relayjdbc.command.ConnectionCreateStatementCommandSerializer;
import com.github.relayjdbc.command.ConnectionGetAutoCommitCommand;
import com.github.relayjdbc.command.ConnectionGetMetaDataCommand;
import com.github.relayjdbc.command.ConnectionGetMetaDataCommandSerializer;
import com.github.relayjdbc.command.ConnectionPrepareCallCommand;
import com.github.relayjdbc.command.ConnectionPrepareStatementCommand;
import com.github.relayjdbc.command.ConnectionPrepareStatementExtendedCommand;
import com.github.relayjdbc.command.ConnectionReleaseSavepointCommand;
import com.github.relayjdbc.command.ConnectionRollbackWithSavepointCommand;
import com.github.relayjdbc.command.ConnectionSetAutoCommitCommand;
import com.github.relayjdbc.command.ConnectionSetClientInfoCommand;
import com.github.relayjdbc.command.DatabaseMetaDataGetDriverNameCommand;
import com.github.relayjdbc.command.DatabaseMetaDataGetUserNameCommand;
import com.github.relayjdbc.command.DatabaseMetaDataGetUserNameCommandSerializer;
import com.github.relayjdbc.command.DestroyCommand;
import com.github.relayjdbc.command.DestroyCommandSerializer;
import com.github.relayjdbc.command.NextRowPacketCommand;
import com.github.relayjdbc.command.NextRowPacketCommandSerializer;
import com.github.relayjdbc.command.PingCommand;
import com.github.relayjdbc.command.PingCommandSerializer;
import com.github.relayjdbc.command.PreparedStatementExecuteBatchCommand;
import com.github.relayjdbc.command.PreparedStatementExecuteCommand;
import com.github.relayjdbc.command.PreparedStatementQueryCommand;
import com.github.relayjdbc.command.PreparedStatementQueryCommandSerializer;
import com.github.relayjdbc.command.PreparedStatementUpdateCommand;
import com.github.relayjdbc.command.PreparedStatementUpdateCommandSerializer;
import com.github.relayjdbc.command.ReflectiveCommand;
import com.github.relayjdbc.command.ResultSetGetMetaDataCommand;
import com.github.relayjdbc.command.ResultSetProducerCommand;
import com.github.relayjdbc.command.StatementCancelCommand;
import com.github.relayjdbc.command.StatementExecuteBatchCommand;
import com.github.relayjdbc.command.StatementExecuteCommand;
import com.github.relayjdbc.command.StatementExecuteExtendedCommand;
import com.github.relayjdbc.command.StatementGetGeneratedKeysCommand;
import com.github.relayjdbc.command.StatementGetResultSetCommand;
import com.github.relayjdbc.command.StatementQueryCommand;
import com.github.relayjdbc.command.StatementSetFetchSizeCommand;
import com.github.relayjdbc.command.StatementSetFetchSizeCommandSerializer;
import com.github.relayjdbc.command.StatementUpdateCommand;
import com.github.relayjdbc.command.StatementUpdateCommandSerializer;
import com.github.relayjdbc.command.StatementUpdateExtendedCommand;
import com.github.relayjdbc.parameters.ArrayParameter;
import com.github.relayjdbc.parameters.ArrayParameterSerializer;
import com.github.relayjdbc.parameters.BigDecimalParameter;
import com.github.relayjdbc.parameters.BigDecimalParameterSerializer;
import com.github.relayjdbc.parameters.BlobParameter;
import com.github.relayjdbc.parameters.BlobParameterSerializer;
import com.github.relayjdbc.parameters.BooleanParameter;
import com.github.relayjdbc.parameters.BooleanParameterSerializer;
import com.github.relayjdbc.parameters.ByteArrayParameter;
import com.github.relayjdbc.parameters.ByteArrayParameterSerializer;
import com.github.relayjdbc.parameters.ByteParameter;
import com.github.relayjdbc.parameters.ByteParameterSerializer;
import com.github.relayjdbc.parameters.ByteStreamParameter;
import com.github.relayjdbc.parameters.ByteStreamParameterSerializer;
import com.github.relayjdbc.parameters.CharStreamParameter;
import com.github.relayjdbc.parameters.CharStreamParameterSerializer;
import com.github.relayjdbc.parameters.ClobParameter;
import com.github.relayjdbc.parameters.ClobParameterSerializer;
import com.github.relayjdbc.parameters.DateParameter;
import com.github.relayjdbc.parameters.DateParameterSerializer;
import com.github.relayjdbc.parameters.DoubleParameter;
import com.github.relayjdbc.parameters.DoubleParameterSerializer;
import com.github.relayjdbc.parameters.FloatParameter;
import com.github.relayjdbc.parameters.FloatParameterSerializer;
import com.github.relayjdbc.parameters.IntegerParameter;
import com.github.relayjdbc.parameters.IntegerParameterSerializer;
import com.github.relayjdbc.parameters.LongParameter;
import com.github.relayjdbc.parameters.LongParameterSerializer;
import com.github.relayjdbc.parameters.NStringParameter;
import com.github.relayjdbc.parameters.NStringParameterSerializer;
import com.github.relayjdbc.parameters.NullParameter;
import com.github.relayjdbc.parameters.NullParameterSerializer;
import com.github.relayjdbc.parameters.ObjectParameter;
import com.github.relayjdbc.parameters.ObjectParameterSerializer;
import com.github.relayjdbc.parameters.RefParameter;
import com.github.relayjdbc.parameters.RefParameterSerializer;
import com.github.relayjdbc.parameters.RowIdParameter;
import com.github.relayjdbc.parameters.RowIdParameterSerializer;
import com.github.relayjdbc.parameters.SQLXMLParameter;
import com.github.relayjdbc.parameters.SQLXMLParameterSerializer;
import com.github.relayjdbc.parameters.ShortParameter;
import com.github.relayjdbc.parameters.ShortParameterSerializer;
import com.github.relayjdbc.parameters.StringParameter;
import com.github.relayjdbc.parameters.StringParameterSerializer;
import com.github.relayjdbc.parameters.TimeParameter;
import com.github.relayjdbc.parameters.TimeParameterSerializer;
import com.github.relayjdbc.parameters.TimestampParameter;
import com.github.relayjdbc.parameters.TimestampParameterSerializer;
import com.github.relayjdbc.parameters.URLParameter;
import com.github.relayjdbc.parameters.URLParameterSerializer;
import com.github.relayjdbc.serial.BigDecimalColumnValues;
import com.github.relayjdbc.serial.BigDecimalColumnValuesSerializer;
import com.github.relayjdbc.serial.BooleanColumnValues;
import com.github.relayjdbc.serial.BooleanColumnValuesSerializer;
import com.github.relayjdbc.serial.ByteColumnValues;
import com.github.relayjdbc.serial.ByteColumnValuesSerializer;
import com.github.relayjdbc.serial.CallingContext;
import com.github.relayjdbc.serial.CallingContextSerializer;
import com.github.relayjdbc.serial.DoubleColumnValues;
import com.github.relayjdbc.serial.DoubleColumnValuesSerializer;
import com.github.relayjdbc.serial.FloatColumnValues;
import com.github.relayjdbc.serial.FloatColumnValuesSerializer;
import com.github.relayjdbc.serial.IntegerColumnValues;
import com.github.relayjdbc.serial.IntegerColumnValuesSerializer;
import com.github.relayjdbc.serial.LongColumnValues;
import com.github.relayjdbc.serial.LongColumnValuesSerializer;
import com.github.relayjdbc.serial.ObjectColumnValues;
import com.github.relayjdbc.serial.ObjectColumnValuesSerializer;
import com.github.relayjdbc.serial.RowPacket;
import com.github.relayjdbc.serial.RowPacketSerializer;
import com.github.relayjdbc.serial.SerialArray;
import com.github.relayjdbc.serial.SerialArraySerializer;
import com.github.relayjdbc.serial.SerialBlob;
import com.github.relayjdbc.serial.SerialClob;
import com.github.relayjdbc.serial.SerialDatabaseMetaData;
import com.github.relayjdbc.serial.SerialDatabaseMetaDataSerializer;
import com.github.relayjdbc.serial.SerialNClob;
import com.github.relayjdbc.serial.SerialRef;
import com.github.relayjdbc.serial.SerialRefSerializer;
import com.github.relayjdbc.serial.SerialResultSetMetaData;
import com.github.relayjdbc.serial.SerialRowId;
import com.github.relayjdbc.serial.SerialRowIdSerializer;
import com.github.relayjdbc.serial.ShortColumnValues;
import com.github.relayjdbc.serial.ShortColumnValuesSerializer;
import com.github.relayjdbc.serial.StreamingResultSet;
import com.github.relayjdbc.serial.UIDEx;
import com.github.relayjdbc.serial.UIDExSerializer;

public class KryoFactory {
	
    private final static Log logger = LogFactory.getLog(KryoFactory.class);

	private static final RowPacketSerializer ROW_PACKET_SERIALIZER = new RowPacketSerializer();
	private static final UIDExSerializer UIDEX_SERIALIZER = new UIDExSerializer();
	private static final CallingContextSerializer CALLING_CONTEXT_SERIALIZER = new CallingContextSerializer();
	private static final SerialArraySerializer SERIAL_ARRAY_SERIALIZER = new SerialArraySerializer();
	
	private static final SerialDatabaseMetaDataSerializer SERIAL_DATABASE_METADATA_SERIALIZER = new SerialDatabaseMetaDataSerializer();
	private static final SerialRefSerializer SERIAL_REF_SERIALIZER = new SerialRefSerializer();
	private static final SerialRowIdSerializer SERIAL_ROW_ID_SERIALIZER = new SerialRowIdSerializer();
	
	private static final ArrayParameterSerializer ARRAY_PARAMETER_SERIALIZER = new ArrayParameterSerializer();
	private static final BigDecimalParameterSerializer BIG_DECIMAL_PARAMETER_SERIALIZER = new BigDecimalParameterSerializer();
	private static final BlobParameterSerializer BLOB_PARAMETER_SERIALIZER = new BlobParameterSerializer();
	private static final BooleanParameterSerializer BOOLEAN_PARAMETER_SERIALIZER = new BooleanParameterSerializer();
	private static final ByteArrayParameterSerializer BYTE_ARRAY_PARAMETER_SERIALIZER = new ByteArrayParameterSerializer();
	private static final ByteParameterSerializer BYTE_PARAMETER_SERIALIZER = new ByteParameterSerializer();
	private static final ByteStreamParameterSerializer BYTE_STREAM_PARAMETER_SERIALIZER = new ByteStreamParameterSerializer();
	private static final CharStreamParameterSerializer CHAR_STREAM_PARAMETER_SERIALIZER = new CharStreamParameterSerializer();
	private static final ClobParameterSerializer CLOB_PARAMETER_SERIALIZER = new ClobParameterSerializer();
	private static final DateParameterSerializer DATE_PARAMETER_SERIALIZER = new DateParameterSerializer();
	private static final DoubleParameterSerializer DOUBLE_PARAMETER_SERIALIZER = new DoubleParameterSerializer();
	private static final FloatParameterSerializer FLOAT_PARAMETER_SERIALIZER = new FloatParameterSerializer();
	private static final IntegerParameterSerializer INTEGER_PARAMETER_SERIALIZER = new IntegerParameterSerializer();
	private static final LongParameterSerializer LONG_PARAMETER_SERIALIZER = new LongParameterSerializer();
	private static final NStringParameterSerializer N_STRING_PARAMETER_SERIALIZER = new NStringParameterSerializer();
	private static final NullParameterSerializer NULL_PARAMETER_SERIALIZER = new NullParameterSerializer();
	private static final ObjectParameterSerializer OBJECT_PARAMETER_SERIALIZER = new ObjectParameterSerializer();
	private static final RefParameterSerializer REF_PARAMETER_SERIALIZER = new RefParameterSerializer();
	private static final RowIdParameterSerializer ROW_ID_PARAMETER_SERIALIZER = new RowIdParameterSerializer();
	private static final SQLXMLParameterSerializer SQLXML_PARAMETER_SERIALIZER = new SQLXMLParameterSerializer();
	private static final ShortParameterSerializer SHORT_PARAMETER_SERIALIZER = new ShortParameterSerializer();
	private static final StringParameterSerializer STRING_PARAMETER_SERIALIZER = new StringParameterSerializer();
	private static final TimeParameterSerializer TIME_PARAMETER_SERIALIZER = new TimeParameterSerializer();
	private static final TimestampParameterSerializer TIMESTAMP_PARAMETER_SERIALIZER = new TimestampParameterSerializer();
	private static final URLParameterSerializer URL_PARAMETER_SERIALIZER = new URLParameterSerializer();
	
	private static final NextRowPacketCommandSerializer NEXT_ROW_PACKET_COMMAND_SERIALIZER = new NextRowPacketCommandSerializer();
	private static final DestroyCommandSerializer DESTROY_COMMAND_SERIALIZER = new DestroyCommandSerializer();
	private static final PreparedStatementQueryCommandSerializer PREPARED_STATEMENT_QUERY_COMMAND_SERIALIZER = new PreparedStatementQueryCommandSerializer();
	private static final PingCommandSerializer PING_COMMAND_SERIALIZER = new PingCommandSerializer();
	private static final ConnectionCommitCommandSerializer CONNECTION_COMMIT_COMMAND_SERIALIZER = new ConnectionCommitCommandSerializer();
	private static final DatabaseMetaDataGetUserNameCommandSerializer DATABASE_META_DATA_GET_USER_NAME_COMMAND_SERIALIZER = new DatabaseMetaDataGetUserNameCommandSerializer();
	private static final ConnectionCreateStatementCommandSerializer CONNECTION_CREATE_STATEMENT_COMMAND_SERIALIZER = new ConnectionCreateStatementCommandSerializer();
	private static final ConnectionGetMetaDataCommandSerializer CONNECTION_GET_META_DATA_COMMAND_SERIALIZER = new ConnectionGetMetaDataCommandSerializer();
	
	private static final StatementSetFetchSizeCommandSerializer STATEMENT_SET_FETCH_SIZE_COMMAND_SERIALIZER = new StatementSetFetchSizeCommandSerializer();
	private static final PreparedStatementUpdateCommandSerializer PREPARED_STATEMENT_UPDATE_COMMAND_SERIALIZER = new PreparedStatementUpdateCommandSerializer();
	private static final StatementUpdateCommandSerializer STATEMENT_UPDATE_COMMAND_SERIALIZER = new StatementUpdateCommandSerializer();
	
	private static final CompositeCommandSerializer COMPOSITE_COMMAND_SERIALIZER = new CompositeCommandSerializer();
	
	private static final BooleanColumnValuesSerializer BOOLEAN_COLUMN_VALUES_SERIALIZER = new BooleanColumnValuesSerializer();
	private static final ByteColumnValuesSerializer BYTE_COLUMN_VALUES_SERIALIZER = new ByteColumnValuesSerializer();
	private static final ShortColumnValuesSerializer SHORT_COLUMN_VALUES_SERIALIZER = new ShortColumnValuesSerializer();
	private static final IntegerColumnValuesSerializer INTEGER_COLUMN_VALUES_SERIALIZER = new IntegerColumnValuesSerializer();
	private static final LongColumnValuesSerializer LONG_COLUMN_VALUES_SERIALIZER = new LongColumnValuesSerializer();
	private static final FloatColumnValuesSerializer FLOAT_COLUMN_VALUES_SERIALIZER = new FloatColumnValuesSerializer();
	private static final DoubleColumnValuesSerializer DOUBLE_COLUMN_VALUES_SERIALIZER = new DoubleColumnValuesSerializer();
	private static final ObjectColumnValuesSerializer OBJECT_COLUMN_VALUES_SERIALIZER = new ObjectColumnValuesSerializer();
	private static final BigDecimalColumnValuesSerializer BIG_DECIMAL_COLUMN_VALUES_SERIALIZER = new BigDecimalColumnValuesSerializer();
	
	private final ConcurrentLinkedQueue<Kryo> kryoCache = new ConcurrentLinkedQueue<Kryo>();
	
	private ConcurrentMap<String, AtomicInteger> instanceCount = new ConcurrentHashMap<String, AtomicInteger>();
	/** 
	 * Utility class for gathering statistic number of instantiations via reflection
	 */
	class KryoSerializableCountingSerializer extends Serializer<KryoSerializable> {
		public void write (Kryo kryo, Output output, KryoSerializable object) {
			object.write(kryo, output);
		}

		public KryoSerializable read (Kryo kryo, Input input, Class<KryoSerializable> type) {
			count(type.getName());
			KryoSerializable object = kryo.newInstance(type);
			kryo.reference(object);
			object.read(kryo, input);
			return object;
		}
		
		private void count(String type){
			AtomicInteger i = new AtomicInteger(1);
			AtomicInteger v = instanceCount.putIfAbsent(type, i);
			if (v!=null){
				v.incrementAndGet();
			}			
		}
		
	}
	
	private final KryoSerializableCountingSerializer KRYO_SERIALIZABLE_COUNTING_SERIALIZER = new KryoSerializableCountingSerializer();
	
	public void dumpInstanceCount(){
		int size = instanceCount.size();
		if (size>0){			
			logger.debug("==== Instance count dump ====");
			ArrayList<Map.Entry<String,AtomicInteger>> list = new ArrayList<Map.Entry<String, AtomicInteger>>(instanceCount.entrySet());
			Collections.sort(list, new Comparator<Map.Entry<String,AtomicInteger>>() {
				@Override
				public int compare(Entry<String, AtomicInteger> o1, Entry<String, AtomicInteger> o2) {
					return Integer.valueOf(o1.getValue().get()).compareTo(Integer.valueOf(o2.getValue().get()));
				}
			});
			
			for (Map.Entry<String,AtomicInteger> me: list){
				logger.debug("class: "+me.getKey()+": "+me.getValue().get());
			}
		}
	}
	

	/**
	 * Instance holder see {@linkplain http://en.wikipedia.org/wiki/Singleton_pattern#The_solution_of_Bill_Pugh} for details.
	 *
	 */
	private static class Instance {
		private static final KryoFactory instance = new KryoFactory();
	}
	
	
	public static KryoFactory getInstance() {
		return Instance.instance;
	}
	
	private KryoFactory() {
	}
	
	private Kryo createKryo() {
		Kryo kryo = new Kryo();
		kryo.register(Properties.class);

		// java.sql exceptions
		kryo.register(BatchUpdateException.class, new JavaSerializer());
		kryo.register(DataTruncation.class, new JavaSerializer());
		kryo.register(SQLClientInfoException.class, new JavaSerializer());
		kryo.register(SQLDataException.class, new JavaSerializer());		
		kryo.register(SQLException.class, new JavaSerializer());
		kryo.register(SQLFeatureNotSupportedException.class, new JavaSerializer());
		kryo.register(SQLIntegrityConstraintViolationException.class, new JavaSerializer());
		kryo.register(SQLInvalidAuthorizationSpecException.class, new JavaSerializer());
		kryo.register(SQLNonTransientConnectionException.class, new JavaSerializer());
		kryo.register(SQLNonTransientException.class, new JavaSerializer());
		kryo.register(SQLRecoverableException.class, new JavaSerializer());
		kryo.register(SQLSyntaxErrorException.class, new JavaSerializer());
		kryo.register(SQLTimeoutException.class, new JavaSerializer());
		kryo.register(SQLTransactionRollbackException.class, new JavaSerializer());
		kryo.register(SQLTransientConnectionException.class, new JavaSerializer());
		kryo.register(SQLTransientException.class, new JavaSerializer());
		kryo.register(SQLWarning.class, new JavaSerializer());
		
		kryo.register(VJdbcException.class, new JavaSerializer());
		kryo.register(UIDEx.class, UIDEX_SERIALIZER);
		kryo.register(CallingContext.class, CALLING_CONTEXT_SERIALIZER);
		
		kryo.register(BooleanColumnValues.class, BOOLEAN_COLUMN_VALUES_SERIALIZER);
		kryo.register(ByteColumnValues.class, BYTE_COLUMN_VALUES_SERIALIZER);
		kryo.register(ShortColumnValues.class, SHORT_COLUMN_VALUES_SERIALIZER);
		kryo.register(IntegerColumnValues.class, INTEGER_COLUMN_VALUES_SERIALIZER);
		kryo.register(LongColumnValues.class, LONG_COLUMN_VALUES_SERIALIZER);
		kryo.register(FloatColumnValues.class, FLOAT_COLUMN_VALUES_SERIALIZER);
		kryo.register(DoubleColumnValues.class, DOUBLE_COLUMN_VALUES_SERIALIZER);
		kryo.register(ObjectColumnValues.class, OBJECT_COLUMN_VALUES_SERIALIZER);
		kryo.register(BigDecimalColumnValues.class, BIG_DECIMAL_COLUMN_VALUES_SERIALIZER);
		
		kryo.register(RowPacket.class, ROW_PACKET_SERIALIZER);
		kryo.register(SerialArray.class, SERIAL_ARRAY_SERIALIZER);
		kryo.register(SerialDatabaseMetaData.class, SERIAL_DATABASE_METADATA_SERIALIZER);
		kryo.register(SerialBlob.class);
		kryo.register(SerialClob.class);
		kryo.register(SerialNClob.class);
		kryo.register(SerialRef.class, SERIAL_REF_SERIALIZER);
		kryo.register(SerialResultSetMetaData.class);
		kryo.register(SerialRowId.class, SERIAL_ROW_ID_SERIALIZER);
		kryo.register(StreamingResultSet.class);
		
		// Commands
		kryo.register(CallableStatementGetArrayCommand.class);
		kryo.register(CallableStatementGetBlobCommand.class);
		kryo.register(CallableStatementGetCharacterStreamCommand.class);
		kryo.register(CallableStatementGetClobCommand.class);
		kryo.register(CallableStatementGetNCharacterStreamCommand.class);
		kryo.register(CallableStatementGetNClobCommand.class);
		kryo.register(CallableStatementGetObjectCommand.class);
		kryo.register(CallableStatementGetRefCommand.class);
		kryo.register(CallableStatementGetSQLXMLCommand.class);
		kryo.register(CallableStatementSetAsciiStreamCommand.class);
		kryo.register(CallableStatementSetBinaryStreamCommand.class);
		kryo.register(CallableStatementSetBlobCommand.class);
		kryo.register(CallableStatementSetCharacterStreamCommand.class);
		kryo.register(CallableStatementSetClobCommand.class);
		kryo.register(CallableStatementSetNCharacterStreamCommand.class);
		kryo.register(CallableStatementSetNClobCommand.class);
		kryo.register(CallableStatementSetObjectCommand.class);
		kryo.register(CallableStatementSetRowIdCommand.class);
		kryo.register(CallableStatementSetSQLXMLCommand.class);
		kryo.register(ConnectionCommitCommand.class, CONNECTION_COMMIT_COMMAND_SERIALIZER);
		kryo.register(ConnectionPrepareCallCommand.class);
		kryo.register(ConnectionPrepareStatementCommand.class);
		kryo.register(ConnectionPrepareStatementExtendedCommand.class);
		kryo.register(ConnectionReleaseSavepointCommand.class);
		kryo.register(ConnectionRollbackWithSavepointCommand.class);
		kryo.register(ConnectionSetClientInfoCommand.class);
		kryo.register(DatabaseMetaDataGetUserNameCommand.class, DATABASE_META_DATA_GET_USER_NAME_COMMAND_SERIALIZER);
		kryo.register(DestroyCommand.class, DESTROY_COMMAND_SERIALIZER);
		kryo.register(NextRowPacketCommand.class, NEXT_ROW_PACKET_COMMAND_SERIALIZER);
		kryo.register(PingCommand.class, PING_COMMAND_SERIALIZER);
		kryo.register(PreparedStatementExecuteBatchCommand.class);
		kryo.register(PreparedStatementExecuteCommand.class);
		kryo.register(PreparedStatementQueryCommand.class, PREPARED_STATEMENT_QUERY_COMMAND_SERIALIZER);
		kryo.register(PreparedStatementUpdateCommand.class, PREPARED_STATEMENT_UPDATE_COMMAND_SERIALIZER);
		kryo.register(ReflectiveCommand.class);
		kryo.register(ResultSetGetMetaDataCommand.class);
		kryo.register(ResultSetProducerCommand.class);
		kryo.register(StatementCancelCommand.class);
		kryo.register(StatementExecuteBatchCommand.class);
		kryo.register(StatementExecuteCommand.class);
		kryo.register(StatementExecuteExtendedCommand.class);
		kryo.register(StatementGetGeneratedKeysCommand.class);
		kryo.register(StatementGetResultSetCommand.class);
		kryo.register(StatementQueryCommand.class);
		kryo.register(StatementUpdateCommand.class, STATEMENT_UPDATE_COMMAND_SERIALIZER);
		kryo.register(StatementUpdateExtendedCommand.class);
		kryo.register(ConnectionCreateStatementCommand.class, CONNECTION_CREATE_STATEMENT_COMMAND_SERIALIZER);
		kryo.register(ConnectionGetAutoCommitCommand.class);
		kryo.register(ConnectionGetMetaDataCommand.class, CONNECTION_GET_META_DATA_COMMAND_SERIALIZER);
		kryo.register(ConnectionSetAutoCommitCommand.class);
		kryo.register(DatabaseMetaDataGetDriverNameCommand.class);
		kryo.register(StatementSetFetchSizeCommand.class, STATEMENT_SET_FETCH_SIZE_COMMAND_SERIALIZER);
		kryo.register(CompositeCommand.class, COMPOSITE_COMMAND_SERIALIZER);
		
		// Parameters
		kryo.register(ArrayParameter.class, ARRAY_PARAMETER_SERIALIZER);
		kryo.register(BigDecimalParameter.class, BIG_DECIMAL_PARAMETER_SERIALIZER);
		kryo.register(BlobParameter.class, BLOB_PARAMETER_SERIALIZER);
		kryo.register(BooleanParameter.class, BOOLEAN_PARAMETER_SERIALIZER);
		kryo.register(ByteArrayParameter.class, BYTE_ARRAY_PARAMETER_SERIALIZER);
		kryo.register(ByteParameter.class, BYTE_PARAMETER_SERIALIZER);
		kryo.register(ByteStreamParameter.class, BYTE_STREAM_PARAMETER_SERIALIZER);
		kryo.register(CharStreamParameter.class, CHAR_STREAM_PARAMETER_SERIALIZER);
		kryo.register(ClobParameter.class, CLOB_PARAMETER_SERIALIZER);
		kryo.register(DateParameter.class, DATE_PARAMETER_SERIALIZER);
		kryo.register(DoubleParameter.class, DOUBLE_PARAMETER_SERIALIZER);
		kryo.register(FloatParameter.class, FLOAT_PARAMETER_SERIALIZER);
		kryo.register(IntegerParameter.class, INTEGER_PARAMETER_SERIALIZER);
		kryo.register(LongParameter.class, LONG_PARAMETER_SERIALIZER);
		kryo.register(NStringParameter.class, N_STRING_PARAMETER_SERIALIZER);
		kryo.register(NullParameter.class, NULL_PARAMETER_SERIALIZER);
		kryo.register(ObjectParameter.class, OBJECT_PARAMETER_SERIALIZER);
		kryo.register(RefParameter.class, REF_PARAMETER_SERIALIZER);
		kryo.register(RowIdParameter.class, ROW_ID_PARAMETER_SERIALIZER);
		kryo.register(SQLXMLParameter.class, SQLXML_PARAMETER_SERIALIZER);
		kryo.register(ShortParameter.class, SHORT_PARAMETER_SERIALIZER);
		kryo.register(StringParameter.class, STRING_PARAMETER_SERIALIZER);
		kryo.register(TimeParameter.class, TIME_PARAMETER_SERIALIZER);
		kryo.register(TimestampParameter.class, TIMESTAMP_PARAMETER_SERIALIZER);
		kryo.register(URLParameter.class, URL_PARAMETER_SERIALIZER);
		
		return kryo;
	}
	
	public Kryo getKryo() {
		Kryo kryo = kryoCache.poll();
		if (kryo==null){
			kryo = createKryo();
		}		
		return kryo;
	}
	
	public void releaseKryo(Kryo kryo){
		kryoCache.add(kryo);
	}	
}
