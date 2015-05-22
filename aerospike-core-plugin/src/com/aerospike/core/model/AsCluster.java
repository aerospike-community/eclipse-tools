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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.Viewer;

import com.aerospike.client.cluster.Node;
import com.aerospike.core.CoreActivator;

public class AsCluster implements IAsEntity{
	private IProject project = null;
	NodeFolder nodes;
	NsFolder 	namespaces;
	ModuleFolder packages;
	IndexFolder indexes;
	
	private Viewer viewer;
	public AsCluster(IProject project){
		this.project = project;
		this.namespaces = new NsFolder(this);
		this.nodes = new NodeFolder(this);
		this.packages = new ModuleFolder(this);
		this.indexes = new IndexFolder(this);
		try {
			project.setSessionProperty(CoreActivator.CLUSTER, this);
		} catch (CoreException e) {
			CoreActivator.showError(e, "cannot set cluster property on prohect");
		}
	}
	public AsCluster(IProject project, Viewer viewer) {
		this(project);
		this.viewer = viewer;
	}
	public Object[] getChildren(){
		Object[] kids = new Object[]{
				this.namespaces,
				this.packages,
				this.indexes,
				this.nodes};
		return kids;
	}
	public boolean hasChildren(){
		return true;
	}

	@Override
	public String toString() {
		return "Cluster";
	}
	@Override
	public String getName() {
		return toString();
	}
	public IProject getProject() {
		return this.project;
	}
	public NodeFolder getNodes() {
		return nodes;
	}
	public NsFolder getNamespaces() {
		return namespaces;
	}
	public ModuleFolder getPackages() {
		return packages;
	}
	public IndexFolder getIndexes() {
		return indexes;
	}
	@Override
	public Object getParent() {
		return this.project;
	}

	public String getSeedHost(){
		String seedHost = CoreActivator.getSeedHost(project);
		return seedHost;
	}

	public int getPort(){
		int port = 3000;
		if (project != null){
			port = CoreActivator.getPort(project);
		}
		return port;
	}

	public Viewer getViewer(){
		return this.viewer;
	}

	public AsNode addNode(String nodesString) {
		AsNode newNode = new AsNode(this.nodes,nodesString);
		return this.nodes.fetchNode(newNode);
	}
	public AsNode addNode(Node node) {
		AsNode newNode = new AsNode(this.nodes, node);
		return this.nodes.fetchNode(newNode);
	}
	public String getRandomNodeID() {
		AsNode node = this.nodes.getRandomNode();
		return node.nodeID;
	}
}
