package com.aerospike.core.model;

public class NameValuePair implements IAsEntity{
	public String name;
	public Object value;
	private Object parent;
	public NameValuePair(Object parent, String name, Object value) {
		super();
		this.name = name;
		this.value = value;
		this.parent = parent;
	}
	public String getName() {
		return name;
	}
	public Object getValue() {
		return value;
	}
	
	@Override
	public String toString() {
		return name + "|" + value.toString();
	}
	@Override
	public boolean hasChildren() {
		return false;
	}
	@Override
	public Object[] getChildren() {
		return null;
	}
	@Override
	public Object getParent() {
		return this.parent;
	}
	
}
