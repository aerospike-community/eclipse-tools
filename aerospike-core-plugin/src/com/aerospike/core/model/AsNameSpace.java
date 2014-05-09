package com.aerospike.core.model;

import java.util.HashMap;
import java.util.Map;

public class AsNameSpace implements IAsEntity{
	protected String name;
	protected Object parent;
	protected Map<String, AsSet> sets;

	public AsNameSpace(Object parent, String name) {
		this.name = name;
		this.parent = parent;
	}

	@Override
	public String toString() {
		return this.name;
	}
	@Override
	public boolean equals(Object obj) {
		return ((obj instanceof AsNameSpace) &&
				(obj.toString().equals(toString())));
	}

	public Object getParent(){
		return this.parent;
	}
	public boolean hasChildren() {
		return (sets != null && sets.size() > 0);
	}

	public void addSet(String setData){
		if (sets == null)
			sets = new HashMap<String, AsSet>();
		AsSet newSet = new AsSet(this, setData);
		AsSet existingSet = sets.get(newSet.getName());
		if (existingSet == null){
			sets.put(newSet.getName(), newSet);
		} else {
			existingSet.setInfo(setData);
		}
	}
	

	public Object[] getChildren() {
		return sets.values().toArray();
	}

	@Override
	public String getName() {
		return toString();
	}

}
