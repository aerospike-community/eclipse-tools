package com.aerospike.core.model;

public class Function implements IAsEntity
{
	private String name;
	private Package parent;

	public Function(Package parent, String name) {
		super();
		this.name = name;
		this.parent = parent;
	}

	@Override
	public String getName() {
		return this.name;
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
	@Override
	public String toString() {
		return getName();
	}
}
