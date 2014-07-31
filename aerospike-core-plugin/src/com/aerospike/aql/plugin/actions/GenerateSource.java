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
package com.aerospike.aql.plugin.actions;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleView;

import com.aerospike.aql.AQL;
import com.aerospike.aql.plugin.views.AQLResult;
import com.aerospike.core.CoreActivator;
import com.aerospike.core.model.AsCluster;
import com.aerospike.core.preferences.PreferenceConstants;

public class GenerateSource implements IWorkbenchWindowActionDelegate {
	private static final String COULD_NOT_GENERATE_CODE_FROM_SQL_FILE = "Could not generate code from SQL file: ";
	private IWorkbenchWindow window;
	private ISelection selection;
	private Shell shell;
	private IWorkbenchPart targetPart;
	/**
	 * The constructor.
	 */
	public GenerateSource() {
	}

	public void run(IAction action) {
		if (selection != null && selection instanceof TreeSelection){
			TreeSelection ts = (TreeSelection)selection;
			Object element = ts.getFirstElement();
			if (element instanceof IFile && ((IFile)element).getFileExtension().equalsIgnoreCase("aql")){
				final IFile sqlFile = (IFile)element;
				if (sqlFile == null)
					return;
				try {
					final List<String> errorList = new ArrayList<String>();
					final String actionID = action.getId();
					final AQLResult results = new AQLResult();
					// find the Aerospike console and display it
					IWorkbench wb = PlatformUI.getWorkbench();
					IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
					IWorkbenchPage page = win.getActivePage();
					IConsoleView view = (IConsoleView) page.showView(IConsoleConstants.ID_CONSOLE_VIEW);
					view.display(results.getConsole());
					// set generation language
					String extension;
					final com.aerospike.aql.AQL.Language language;
						if (actionID.equals("com.aerospike.aql.plugin.actions.GenerateSource.java.popup")){
							language = com.aerospike.aql.AQL.Language.JAVA;
							extension = ".java";
						} else if (actionID.equals("com.aerospike.aql.plugin.actions.GenerateSource.c.popup")){
							language = com.aerospike.aql.AQL.Language.C;
							extension = ".c";
						} else if (actionID.equals("com.aerospike.aql.plugin.actions.GenerateSource.csharp.popup")){
							language = com.aerospike.aql.AQL.Language.CSHARP;
							extension = ".csharp";
						} else {
							return;
						}
					IProject project = sqlFile.getProject();
					IPath outputPath;
					String sqlFileName = sqlFile.getName();
					String outputFileName = sqlFileName.substring(0, sqlFileName.lastIndexOf('.')) + extension ;

					final AsCluster cluster = (AsCluster) project.getSessionProperty(CoreActivator.CLUSTER_PROPERTY);
					String outputDirectoryString = project.getPersistentProperty(CoreActivator.AQL_GENERATION_DIRECTORY);
					if (outputDirectoryString == null || outputDirectoryString.isEmpty()){
						outputPath = project.getLocation().append(outputFileName);
					} else {
						IPath dirPath = project.getLocation().append(outputDirectoryString);
						if (!dirPath.toFile().exists())
							dirPath.toFile().mkdirs();
						outputPath = dirPath.append(outputFileName);
					}
					final File outputFile = outputPath.toFile();
					IPath location = sqlFile.getLocation();
					final File file = location.toFile();
					final IFile outputIFile = project.getWorkspace().getRoot().getFileForLocation(outputPath);
					
					
					Job job = new Job("Generate source code from AQL: " + sqlFile.getName()) {

						@Override
						protected IStatus run(IProgressMonitor monitor) {
							AQL aql = new AQL();
							try {
								String seedNode = "";
								int port = 3000;
								if (cluster!=null){
									seedNode = cluster.getSeedHost();
									port = cluster.getPort();
								} else {
									IPreferenceStore store = CoreActivator.getDefault().getPreferenceStore();
									seedNode = store.getString(PreferenceConstants.SEED_NODE);
									port = store.getInt(PreferenceConstants.PORT);
								}
								aql.compileAndGenerate(file, outputFile, language, seedNode, port);
								results.report("Completed generation for " + sqlFile.getName());
								outputIFile.getParent().refreshLocal(IResource.DEPTH_ONE, null);
								return Status.OK_STATUS;
							} catch (Exception e) {
								CoreActivator.showError(e, COULD_NOT_GENERATE_CODE_FROM_SQL_FILE + sqlFile.getName());
								return Status.CANCEL_STATUS;
							}
						}
					};
					job.setUser(true);
					job.schedule();
				} catch (PartInitException e) {
					CoreActivator.showError(e, COULD_NOT_GENERATE_CODE_FROM_SQL_FILE + sqlFile.getName());
				} catch (CoreException e) {
					CoreActivator.showError(e, COULD_NOT_GENERATE_CODE_FROM_SQL_FILE + sqlFile.getName());
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
		this.window = window;
	}
	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		this.targetPart = targetPart;
		this.shell = targetPart.getSite().getShell();
	}

}