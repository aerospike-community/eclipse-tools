package com.aerospike.core.navigator;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;

import com.aerospike.core.model.AsCluster;
import com.aerospike.core.model.ClusterRefreshJob;
import com.aerospike.core.model.IAsEntity;

public class RefreshCluster implements IViewActionDelegate {
	ISelection selection;
	IWorkbenchWindow window;
	@Override
	public void run(IAction action) {
		// Refresh cluster
		StructuredSelection ss = (StructuredSelection)selection;
		
		Object firstOne = ss.getFirstElement();
		if (!(firstOne instanceof IAsEntity))
			return;
		IAsEntity selectedEntity = (IAsEntity) firstOne;
		final AsCluster cluster = getCluster(selectedEntity);
		
		ClusterRefreshJob job = new ClusterRefreshJob(cluster);
		job.schedule();
	}
	public AsCluster getCluster(IAsEntity selectedEntity) {
		IAsEntity topEntity = selectedEntity;
		while (!(topEntity instanceof AsCluster))
			topEntity = (IAsEntity) topEntity.getParent();

		return (AsCluster) topEntity;
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;

	}

	@Override
	public void init(IViewPart viewPart) {
		
	}

}
