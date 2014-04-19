package com.aerospike.core.model;

public interface IAsEntity {
	public String getName();
	public boolean hasChildren();
	public Object[] getChildren();
	public Object getParent();
}
