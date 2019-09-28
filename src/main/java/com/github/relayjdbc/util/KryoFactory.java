package com.github.relayjdbc.util;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.serializers.JavaSerializer;
import com.github.relayjdbc.VJdbcException;
import com.github.relayjdbc.command.*;
import com.github.relayjdbc.parameters.*;
import com.github.relayjdbc.serial.*;
import com.github.relayjdbc.server.command.CompositeCommand;
import com.github.relayjdbc.server.command.CompositeCommandSerializer;

import java.sql.*;
import java.util.Properties;
import java.util.concurrent.ConcurrentLinkedQueue;

public class KryoFactory {

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
	
	private final ConcurrentLinkedQueue<Kryo> kryoCache = new ConcurrentLinkedQueue<>();

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
