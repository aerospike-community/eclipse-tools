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
package com.aerospike.core.nature;

import java.util.Iterator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.internal.dialogs.PropertyDialog;

import com.aerospike.core.CoreActivator;
import com.aerospike.core.properties.ClusterPropertyPage;

public class AddRemoveAerospikeNatureHandler extends AbstractHandler implements IObjectActionDelegate{

	private ISelection selection;
	private IAction action;
	private IWorkbenchPart part;

	public Object execute(ExecutionEvent event) throws ExecutionException {
		this.selection = HandlerUtil.getCurrentSelection(event);
		toggleNature();
		return null;
	}


	/**
	 * Toggles sample nature on a project
	 *
	 * @param project
	 *            to have sample nature added or removed
	 */
	private void toggleNature()  {
		if (selection instanceof IStructuredSelection) {
			for (Iterator<?> it = ((IStructuredSelection) selection).iterator(); it
					.hasNext();) {
				Object element = it.next();
				IProject project = null;
				if (element instanceof IProject) {
					project = (IProject) element;
				} else if (element instanceof IAdaptable) {
					project = (IProject) ((IAdaptable) element)
							.getAdapter(IProject.class);
				}
				if (project != null) {
					try {
						IProjectDescription description = project.getDescription();
						String[] natures = description.getNatureIds();

						for (int i = 0; i < natures.length; ++i) {
							if (AerospikeNature.NATURE_ID.equals(natures[i])) {
								// Remove the nature
								String[] newNatures = new String[natures.length - 1];
								System.arraycopy(natures, 0, newNatures, 0, i);
								System.arraycopy(natures, i + 1, newNatures, i, natures.length - i - 1);
								description.setNatureIds(newNatures);
								project.setDescription(description, null);
								return;
							}
						}
						// Add the nature
						String[] newNatures = new String[natures.length + 1];
						System.arraycopy(natures, 0, newNatures, 0, natures.length);
						newNatures[natures.length] = AerospikeNature.NATURE_ID;
						description.setNatureIds(newNatures);
						project.setDescription(description, null);
						// Show property page
						ClusterPropertyPage page = new ClusterPropertyPage();
						page.setElement((IAdaptable) element);
						PreferenceManager mgr = new PreferenceManager();
						IPreferenceNode node = new PreferenceNode("1", page);
						mgr.addToRoot(node);
						Shell shell = this.part.getSite().getShell();
						PropertyDialog dialog = new PropertyDialog(shell, mgr, this.selection);
						dialog.create();
						dialog.setMessage(page.getTitle());
						dialog.open();
					} catch (CoreException e) {
						CoreActivator.showError(e, "Could not change Aerospike Nature");
					}
				}
			}
		}
	}

	@Override
	public void run(IAction action) {
		toggleNature();

	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
		this.action = action;

	}

	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		this.action = action;
		this.part = targetPart;

	}

}