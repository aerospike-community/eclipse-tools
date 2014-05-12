package com.aerospike.core.model;

import java.util.HashMap;
import java.util.Map;

public class NsFolder implements IAsEntity{
	private AsCluster parent;
	private Map<String, AsNameSpace> nsList = null;
	public NsFolder(AsCluster parent) {
		this.parent = parent;
		this.nsList = new HashMap<String, AsNameSpace>();
	}

	@Override
	public String toString() {
		return "Name spaces";
	}

	public Object[] getChildren() {
		if (nsList != null){
			return nsList.values().toArray();
		} else {
			return null;
		}
	}
	
	public boolean hasChildren(){
		return (nsList != null && nsList.size() > 0);
	}
//	
//	public void add(AsNameSpace ns){
//		if (!this.nsList.contains(ns))
//			this.nsList.add(ns);
//	}
	
	public AsNameSpace fetchNameSpace(String name) {
		AsNameSpace ns = this.nsList.get(name);
		if (ns == null){
			ns = new AsNameSpace(this, name);
			this.nsList.put(ns.getName(), ns);
		}
		return ns;
	}
	
	public void clear(){
		this.nsList.clear();
	}

	@Override
	public String getName() {
		return this.getName();
	}
	@Override
	public Object getParent() {
		return this.parent;
	}

}
