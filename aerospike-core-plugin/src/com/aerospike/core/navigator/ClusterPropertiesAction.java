package com.aerospike.core.navigator;

import java.util.Collections;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;

import com.aerospike.core.model.AsCluster;
import com.aerospike.core.model.IAsEntity;
import com.aerospike.core.properties.ClusterPropertyPage;

public class ClusterPropertiesAction implements IViewActionDelegate{
	ISelection selection;
	IWorkbenchWindow window;

	@Override
	public void run(IAction action) {
		StructuredSelection ss = (StructuredSelection)selection;
		
		Object firstOne = ss.getFirstElement();
		if (!(firstOne instanceof IAsEntity))
			return;
		IAsEntity selectedEntity = (IAsEntity) firstOne;
		final AsCluster cluster = getCluster(selectedEntity);
		IProject project = cluster.getProject();
		if (this.window == null){
		   IWorkbench wb = PlatformUI.getWorkbench();
		   this.window = wb.getActiveWorkbenchWindow();
		}
		PreferenceDialog dialog = PreferencesUtil.createPropertyDialogOn(window.getShell(), project, 
		    ClusterPropertyPage.ID, new String[] { ClusterPropertyPage.ID }, Collections.EMPTY_MAP);
		dialog.open();
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
		this.window = viewPart.getViewSite().getWorkbenchWindow();
	}

}
