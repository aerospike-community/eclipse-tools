package com.aerospike.core.model;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IStartup;

import com.aerospike.core.CoreActivator;
import com.aerospike.core.nature.AerospikeNature;

public class StartUp implements IStartup {

	@Override
	public void earlyStartup() {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		/*
		 * refresh all Aerospike projects in workspace
		 */
		IProject[] projects = workspace.getRoot().getProjects();
		for (IProject project : projects){
			runClusterRefresh(project);
		}
		/*
		 * Listen for project open
		 */
		IResourceChangeListener listener = new IResourceChangeListener() {
			public void resourceChanged(IResourceChangeEvent event) {
				if (event == null || event.getDelta() == null) {
					return;
				}

				try {
					event.getDelta().accept(new IResourceDeltaVisitor() {
						public boolean visit(final IResourceDelta delta) throws CoreException {
							IResource resource = delta.getResource();
							if (((resource.getType() & IResource.PROJECT) != 0)
									&& resource.getProject().isOpen()
									&& delta.getKind() == IResourceDelta.CHANGED
									&& ((delta.getFlags() & IResourceDelta.OPEN) != 0)) {

								IProject project = (IProject)resource;
								runClusterRefresh(project);
							}
							return true;
						}
					});
				} catch (CoreException e) {
					CoreActivator.showError(e, "Cluster refresh error");
				}
			}
		};
		workspace.addResourceChangeListener(listener);


	}
	private void runClusterRefresh(IProject project){
		try {
			if (project.isOpen() && project.hasNature(AerospikeNature.NATURE_ID)){
				System.out.println("@@@@ " + project.getName());
				AsCluster cluster = CoreActivator.getCluster(project);
				if (cluster == null){
					cluster = new AsCluster(project);
					ClusterRefreshJob job = new ClusterRefreshJob(cluster);
					job.schedule(1000);
				}
			}
		} catch (CoreException e) {
			CoreActivator.showError(e, "Cluster refresh error");
		}
		
	}
}
