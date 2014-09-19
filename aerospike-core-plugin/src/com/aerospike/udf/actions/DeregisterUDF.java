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
package com.aerospike.udf.actions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleView;

import com.aerospike.client.AerospikeClient;
import com.aerospike.client.AerospikeException;
import com.aerospike.client.Info;
import com.aerospike.client.cluster.Node;
import com.aerospike.core.CoreActivator;
import com.aerospike.core.model.ClusterRefreshJob;
import com.aerospike.core.model.Module;
import com.aerospike.core.views.ResultsConsoleView;

/**
 * Our sample action implements workbench action delegate.
 * The action proxy will be created by the workbench and
 * shown in the UI. When the user tries to use the action,
 * this delegate will be created and execution will be 
 * delegated to it.
 * @see IWorkbenchWindowActionDelegate
 */
public class DeregisterUDF implements IWorkbenchWindowActionDelegate {
	private ISelection selection;
	private Shell shell;
	/**
	 * The constructor.
	 */
	public DeregisterUDF() {
	}
	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		this.shell = targetPart.getSite().getShell();
	}

	/**
	 * The action has been activated. The argument of the
	 * method represents the 'real' action sitting
	 * in the workbench UI.
	 * @see IWorkbenchWindowActionDelegate#run
	 */
	public void run(IAction action) {
		if (selection != null && selection instanceof TreeSelection){
			TreeSelection ts = (TreeSelection)selection;
			Object element = ts.getFirstElement();
			if (element instanceof Module){
				Module udfModule = (Module)element;
				try {
					AerospikeClient client = CoreActivator.getClient(udfModule.getPackage());
					if (client == null){
						CoreActivator.log(IStatus.WARNING, "Aerospike client is null");
						return;
					}
					final ResultsConsoleView results = new ResultsConsoleView();
					// find the Aerospike console and display it
					IWorkbench wb = PlatformUI.getWorkbench();
					IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
					IWorkbenchPage page = win.getActivePage();
					IConsoleView view = (IConsoleView) page.showView(IConsoleConstants.ID_CONSOLE_VIEW);
					view.display(results.getConsole());
					Node[] nodes = client.getNodes();
					StringBuilder message = new StringBuilder();
					for (Node node : nodes){
						String msg = Info.request(node, "udf-remove:filename=" + udfModule.getName());
						if (msg.contains("error")){
							message.append("Could not delete module: " + udfModule.getName());
							CoreActivator.showError("Could remove UDF module: " + udfModule.getName());
							return;
						} else {

							message.append("UDF ");
							message.append(udfModule.getName());
							message.append(" removed from:\n ");
							message.append("\t");
							message.append(node.getHost());
							message.append(" ");
							message.append(node.getName());
							message.append("\n");
						}
					}


					results.report(message.toString());
					MessageDialog.openInformation(
							shell,
							"Aerospike UDF",
							message.toString());
					ClusterRefreshJob job = new ClusterRefreshJob(udfModule.getCluster());
					job.schedule();
				} catch (CoreException e) {
					CoreActivator.showError(e, "Could not register UDF in file: " + udfModule.getName());
				} catch (NumberFormatException e) {
					CoreActivator.showError(e, "Could not register UDF in file: " + udfModule.getName());
				} catch (AerospikeException e) {
					CoreActivator.showError(e, "Could not register UDF in file: " + udfModule.getName());
				}

			} 
		}
	}

	/**
	 * Selection in the workbench has been changed. We 
	 * can change the state of the 'real' action here
	 * if we want, but this can only happen after 
	 * the delegate has been created.
	 * @see IWorkbenchWindowActionDelegate#selectionChanged
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
	}

	/**
	 * We can use this method to dispose of any system
	 * resources we previously allocated.
	 * @see IWorkbenchWindowActionDelegate#dispose
	 */
	public void dispose() {
	}

	/**
	 * We will cache window object in order to
	 * be able to provide parent shell for the message dialog.
	 * @see IWorkbenchWindowActionDelegate#init
	 */
	public void init(IWorkbenchWindow window) {
	}
}