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
package com.aerospike.core.navigator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.aerospike.core.CoreActivator;
import com.aerospike.core.model.AsCluster;
import com.aerospike.core.model.AsNameSpace;
import com.aerospike.core.model.AsSet;
import com.aerospike.core.model.IAsEntity;
import com.aerospike.core.model.NodeFolder;
import com.aerospike.core.model.NsFolder;
import com.aerospike.core.model.ModuleFolder;



public class ClusterContentProvider implements  ITreeContentProvider{
	private static final Object[] NO_CHILDREN = new Object[0];
	private Viewer viewer;
	public ClusterContentProvider(){
		super();
	}
	@Override
	public void dispose() {
		this.viewer = null;

	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.viewer = viewer;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof IProject){
			IProject project = ((IProject)parentElement);
			try {
				AsCluster cluster = (AsCluster) project.getSessionProperty(CoreActivator.CLUSTER_PROPERTY);
				if (cluster == null){
					cluster = new AsCluster(project, viewer);
					project.setSessionProperty(CoreActivator.CLUSTER_PROPERTY, cluster);
				}
				return new Object[]{cluster};
			} catch (CoreException e) {
				CoreActivator.showError(e, "Cannot store Cluster property");
			}
		} else if (parentElement instanceof IAsEntity){
			return ((IAsEntity)parentElement).getChildren();
		}

		return NO_CHILDREN;
	}

	@Override
	public Object getParent(Object element) {

		if (element instanceof IAsEntity){
			return ((IAsEntity)element).getParent();
		}
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof IProject){
			return true;
		} else if (element instanceof IAsEntity){
			return ((IAsEntity)element).hasChildren();
		}
		return false;
	}

}
