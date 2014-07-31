/* 
 * Copyright 2012-2014 Aerospike, Inc.
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
