package com.aerospike.core.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;

public class NsFolder implements IAsEntity{
	private AsCluster parent;
	private List<AsNameSpace> nsList = null;
	public NsFolder(AsCluster parent) {
		this.parent = parent;
		this.nsList = new ArrayList<AsNameSpace>();
	}

	@Override
	public String toString() {
		return "Name spaces";
	}

	public Object[] getChildren() {
		if (nsList != null){
			return nsList.toArray();
		} else {
			return null;
		}
	}
	
	public boolean hasChildren(){
		return (nsList != null && nsList.size() > 0);
	}
	
	public void add(AsNameSpace ns){
		if (!this.nsList.contains(ns))
			this.nsList.add(ns);
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
