package com.aerospike.core.model;

import java.util.HashSet;
import java.util.Set;

public class PackageFolder implements IAsEntity {
	Set<Package> packageList = null;
	AsCluster parent;
	private String name;
	public PackageFolder(AsCluster parent) {
		this.parent = parent;
		this.name = "Packages";
	}

	public Object[] getChildren(){
		if (packageList == null)
			return new Package[0];

		return packageList.toArray();
	}
	public boolean hasChildren(){
		return (packageList != null && packageList.size() > 0);
	}
	
	public void clear(){
		if (this.packageList != null)
			this.packageList.clear();
	}
	public void add(Package pkg){
		if (this.packageList == null)
			this.packageList = new HashSet<Package>();
		this.packageList.add(pkg);
	}

	@Override
	public String toString() {
		return "Packages";
	}

	@Override
	public String getName() {
		return toString();
	}
	@Override
	public Object getParent() {
		return this.parent;
	}

}
