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

}
