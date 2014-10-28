/* 
 * Copyright 2012-2014 Aerospike, Inc.
 *
 * Portions may be licensed to Aerospike, Inc. under one or more contributor
 * license agreements.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.aerospike.core.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class ModuleFolder implements IAsEntity {
	Map<String, Module> packageList = null;
	AsCluster parent;
	private String name;
	public ModuleFolder(AsCluster parent) {
		this.parent = parent;
		this.name = "Modules";
		this.packageList = new HashMap<String, Module>();
	}

	public Object[] getChildren(){
		if (packageList == null)
			return new Module[0];

		return packageList.values().toArray();
	}
	public boolean hasChildren(){
		return (packageList != null && packageList.size() > 0);
	}

	public void clear(){
		if (this.packageList != null)
			this.packageList.clear();
	}

	public Module fetchPackage(String info){
		
		String name = Module.getNameFromInfo(info);
		Module pkg = this.packageList.get(name);
		if (pkg == null){
			pkg = new Module(this, info);
			this.packageList.put(name, pkg);
		}
		pkg.setPackageInfo(info);
		return pkg;
	}

	@Override
	public String toString() {
		return "Modules";
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
