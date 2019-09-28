package com.github.relayjdbc.serial;

import java.math.BigDecimal;
import java.sql.SQLException;

public class ByteColumnValues extends ColumnValues {

	public static final long serialVersionUID = 1;

	private byte[] values;
	private int[] nullFlags = null;

	ByteColumnValues(byte[] values, int[] nullFlags) {
		super(Byte.TYPE);
		this.values = values;
		this.nullFlags = nullFlags;
		this.size = values.length;
	}
	
	ByteColumnValues(int initialSize) {
		super(Byte.TYPE);
		values = new byte[initialSize];
		nullFlags = new int[(initialSize >> 5) + 1];
	}

	@Override
	final void setIsNull(int index) {
		int i = index >> 5;
		int m = 1 << (index & 31);
		nullFlags[i] = nullFlags[i] | m;
	}

	@Override
	final boolean isNull(int index) {
		int i = index >> 5;
		int m = 1 << (index & 31);
		return (nullFlags[i] & m)!=0;
	}

	@Override
	final void setByte(int index, byte value) {
		values[index] = value;
	}

	@Override
	final byte getByte(int index) {
		return values[index];
	}

	@Override
	final Object getObject(int index) {
		return Byte.valueOf(getByte(index));
	}
	
	@Override
	final Object getValues() {
		return values;
	}

	final int[] getNullFlags(){
		return nullFlags;
	}
	
	@Override
	final short getShort(int index) throws SQLException {
		return values[index];
	}

	@Override
	final int getInt(int index) throws SQLException {
		return values[index];
	}

	@Override
	final long getLong(int index) throws SQLException {
		return values[index];
	}

	@Override
	final float getFloat(int index) throws SQLException {
		return values[index];
	}

	@Override
	final double getDouble(int index) throws SQLException {
		return values[index];
	}

	@Override
	final String getString(int index) throws SQLException {
		return Short.toString(values[index]);
	}

	@Override
	final BigDecimal getBigDecimal(int index) throws SQLException {
		return new BigDecimal(values[index]);
	}
	
}
