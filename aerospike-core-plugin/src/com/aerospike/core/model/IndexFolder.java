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

public class IndexFolder implements IAsEntity{

	private Object parent;
	private String name;
	private HashMap<String, Index> indexList;
	
	public IndexFolder(AsCluster parent) {
		this.parent = parent;
		this.name = "Indexes";
	}

	@Override
	public String toString() {
		return this.name;
	}

	@Override
	public String getName() {
		return toString();
	}
	@Override
	public Object getParent() {
		return this.parent;
	}

	@Override
	public boolean hasChildren() {
		return (indexList != null && indexList.size() > 0);
	}

	@Override
	public Object[] getChildren() {
		if (indexList == null)
			return new Index[0];

		return indexList.values().toArray();
		
	}
	public void clear(){
		if (this.indexList != null)
			this.indexList.clear();
	}
	public void add(String info){
		
		if (this.indexList == null)
			indexList = new HashMap<String, Index>();
		Index index = new Index(this, info);
		Index existingIndex = indexList.get(index.getName());
		if (existingIndex == null){
			indexList.put(index.getName(), index);
		} else {
			existingIndex.setIndexInfo(info);
		}
	}
	
}
