package com.github.relayjdbc.serial;

import java.math.BigDecimal;
import java.sql.SQLException;

public class FloatColumnValues extends ColumnValues {

	public static final long serialVersionUID = 1;

	private float[] values = null;
	private int[] nullFlags = null;

	FloatColumnValues(float[] values, int[] nullFlags) {
		super(Float.TYPE);
		this.values = values;
		this.nullFlags = nullFlags;
		this.size = values.length;
	}

	public FloatColumnValues(int initialSize) {
		super(Float.TYPE);
		values = new float[initialSize];
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
	final void setFloat(int index, float value) {
		values[index] = value;
	}

	@Override
	final float getFloat(int index) {
		return values[index];
	}

	@Override
	final Object getObject(int index) {
		return Float.valueOf(getFloat(index));
	}

	@Override
	final Object getValues() {
		return values;
	}
	
	final int[] getNullFlags() {
		return nullFlags;
	}
	
	@Override
	final double getDouble(int index) throws SQLException {
		return values[index];
	}

	@Override
	final String getString(int index) throws SQLException {
		return Float.toString(values[index]);
	}

	@Override
	final BigDecimal getBigDecimal(int index) throws SQLException {
		return new BigDecimal(values[index]);
	}

}
