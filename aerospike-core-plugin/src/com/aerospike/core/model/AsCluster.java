package com.aerospike.core.model;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.Viewer;

import com.aerospike.core.CoreActivator;

public class AsCluster implements IAsEntity{
	private IProject project = null;
	NodeFolder nodes;
	NsFolder 	namespaces;
	PackageFolder packages;
	
	private Viewer viewer;
	public AsCluster(IProject project){
		this.project = project;
		this.namespaces = new NsFolder(this);
		this.nodes = new NodeFolder(this);
		this.packages = new PackageFolder(this);
	}
	public AsCluster(IProject project, Viewer viewer) {
		this(project);
		this.viewer = viewer;
	}
	public Object[] getChildren(){
		Object[] kids = new Object[]{
				this.namespaces,
				this.packages,
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
	public PackageFolder getPackages() {
		return packages;
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
}
