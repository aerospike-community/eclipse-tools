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
	
	public void mergeSet(String setData){
		if (sets == null)
			sets = new HashMap<String, AsSet>();
		AsSet newSet = new AsSet(this, setData);
		AsSet existingSet = sets.get(newSet.getName());
		if (existingSet == null){
			sets.put(newSet.getName(), newSet);
		} else {
			existingSet.mergeSetInfo(setData);
		}
	}

	public Object[] getChildren() {
		return sets.values().toArray();
	}

	@Override
	public String getName() {
		return toString();
	}
	
	public void clear(){
		if (this.sets != null){
			for (AsSet set : this.sets.values()){
				set.clear();
			}
		}
	}

}
