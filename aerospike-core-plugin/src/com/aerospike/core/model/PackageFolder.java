package com.aerospike.core.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class PackageFolder implements IAsEntity {
	Map<String, Package> packageList = null;
	AsCluster parent;
	private String name;
	public PackageFolder(AsCluster parent) {
		this.parent = parent;
		this.name = "Packages";
		this.packageList = new HashMap<String, Package>();
	}

	public Object[] getChildren(){
		if (packageList == null)
			return new Package[0];

		return packageList.values().toArray();
	}
	public boolean hasChildren(){
		return (packageList != null && packageList.size() > 0);
	}

	public void clear(){
		if (this.packageList != null)
			this.packageList.clear();
	}

	public Package fetchPackage(String info){
		
		String name = Package.getNameFromInfo(info);
		Package pkg = this.packageList.get(name);
		if (pkg == null){
			pkg = new Package(this, info);
			this.packageList.put(name, pkg);
		}
		pkg.setPackageInfo(info);
		return pkg;
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
