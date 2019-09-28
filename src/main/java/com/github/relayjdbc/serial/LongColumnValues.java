package com.github.relayjdbc.serial;

import java.math.BigDecimal;
import java.sql.SQLException;

public class LongColumnValues extends ColumnValues {

	public static final long serialVersionUID = 1;

	private long[] values = null;
	private int[] nullFlags = null;
	
	LongColumnValues(long[] values, int[] nullFlags) {
		super(Long.TYPE);
		this.values = values;
		this.nullFlags = nullFlags;
		this.size = values.length;
	}

	public LongColumnValues(int initialSize) {
		super(Long.TYPE);
		values = new long[initialSize];
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
	final void setLong(int index, long value) {
		values[index] = value;
	}

	@Override
	final long getLong(int index) {
		return values[index];
	}

	@Override
	final Object getObject(int index) {
		return Long.valueOf(getLong(index));
	}
	
	@Override
	final Object getValues() {
		return values;
	}

	final int[] getNullFlags(){
		return nullFlags;
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
		return Long.toString(values[index]);
	}

	@Override
	final BigDecimal getBigDecimal(int index) throws SQLException {
		return new BigDecimal(values[index]);
	}

}
