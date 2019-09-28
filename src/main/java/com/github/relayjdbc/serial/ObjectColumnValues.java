package com.github.relayjdbc.serial;


public class ObjectColumnValues extends ColumnValues {

	public static final long serialVersionUID = 1;

	private Object[] values;
	
	ObjectColumnValues(Class componentType, Object[] values) {
		super(componentType);
		this.values = values;
		this.size = values.length;
	}


	public ObjectColumnValues(Class componentType, int initialSize) {
		super(componentType);
		values = new Object[initialSize];
	}


	@Override
	final void setIsNull(int index) {
		values[index] = null;
	}

	@Override
	final boolean isNull(int index) {
		return values[index]==null;
	}

	@Override
	final void setObject(int index, Object value) {
		assert value==null || componentType.isAssignableFrom(value.getClass());
		values[index] = value;
	}

	@Override
	final Object getObject(int index) {
		return values[index];
	}

	@Override
	final Object getValues() {
		return values;
	}
}
