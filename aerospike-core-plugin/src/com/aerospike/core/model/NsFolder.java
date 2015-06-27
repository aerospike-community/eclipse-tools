/* 
 * Copyright 2012-2015 Aerospike, Inc.
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
	
	public void clearSetData(){
		if (nsList != null){
			for (AsNameSpace ns : nsList.values()){
				ns.clear();
			}
		}
	}
}
