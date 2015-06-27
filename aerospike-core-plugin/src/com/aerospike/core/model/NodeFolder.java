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

public class NodeFolder implements IAsEntity{
	private Map<String, AsNode> nodeList = null;
	private AsCluster parent;
	public NodeFolder(AsCluster parent) {
		this.parent = parent;
	}

	public Object[] getChildren(){
		if (nodeList == null)
			return new AsNode[0];
		return nodeList.values().toArray();
	}
	public boolean hasChildren(){
		return (nodeList != null && nodeList.size() > 0);
	}
	@Override
	public String toString() {
		return "Nodes";
	}
	

	@Override
	public String getName() {
		return toString();
	}

	public AsNode fetchNode(AsNode sourceNode) {
		if (this.nodeList == null)
			this.nodeList = new HashMap<String, AsNode>();
		AsNode asNode = this.nodeList.get(sourceNode.getName());
		if (asNode == null) {
			this.nodeList.put(sourceNode.getName(), sourceNode);
			asNode = sourceNode;
		} else {
			asNode.stats = sourceNode.stats;
		}
		return asNode;
	}
	@Override
	public Object getParent() {
		return this.parent;
	}

	public AsNode getRandomNode() {
		return (AsNode) getChildren()[0];
	}

}
