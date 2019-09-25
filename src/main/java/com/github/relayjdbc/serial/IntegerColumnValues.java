package com.github.relayjdbc.serial;

import java.math.BigDecimal;
import java.sql.SQLException;

public class IntegerColumnValues extends ColumnValues {

	private int[] values = null;
	private int[] nullFlags = null;

	public IntegerColumnValues(int initialSize) {
		super(Integer.TYPE);
		values = new int[initialSize];
		nullFlags = new int[(initialSize >> 5) + 1];
	}

	IntegerColumnValues(int[] values, int[] nullFlags) {
		super(Integer.TYPE);
		this.values = values;
		this.nullFlags = nullFlags;
		this.size = values.length;		
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
	final void setInt(int index, int value) {
		values[index] = value;
	}

	@Override
	final int getInt(int index) {
		return values[index];
	}

	@Override
	final Object getObject(int index) {
		return Integer.valueOf(getInt(index));
	}
	
	@Override
	final Object getValues() {
		return values;
	}

	final int[] getNullFlags() {
		return nullFlags;
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
		return Integer.toString(values[index]);
	}

	@Override
	final BigDecimal getBigDecimal(int index) throws SQLException {
		return new BigDecimal(values[index]);
	}

}
