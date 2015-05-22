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

public class NameValuePair implements IAsEntity{
	public String name;
	public Object value;
	private Object parent;
	public NameValuePair(Object parent, String name, Object value) {
		super();
		this.name = name;
		this.value = value;
		this.parent = parent;
	}
	public String getName() {
		return name;
	}
	public Object getValue() {
		return value;
	}
	
	@Override
	public String toString() {
		return name + "|" + value.toString();
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
	
	public void clear(){
		if (this.value != null && (this.value instanceof Long)){
			this.value = 0L;
		} else if (this.value != null && (this.value instanceof String)){
			this.value = "";
		} else if (this.value != null && (this.value instanceof Integer)){
			this.value = 0;
		} else if (this.value != null && (this.value instanceof Float)){
			this.value = 0.0;
		}
	}
}
