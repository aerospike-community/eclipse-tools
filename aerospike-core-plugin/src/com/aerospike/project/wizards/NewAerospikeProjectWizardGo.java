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
package com.aerospike.project.wizards;

import java.lang.reflect.InvocationTargetException;
import java.net.URL;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import com.aerospike.core.CoreActivator;
import com.aerospike.core.preferences.PreferenceConstants;


public class NewAerospikeProjectWizardGo extends NewAerospikeProjectWizard {
	public NewAerospikeProjectWizardGo() {
		super();
		this.language = language.GO;
	}

	
	protected boolean generateProject(){
		/*
		<buildSpec>
			<buildCommand>
				<name>com.googlecode.goclipse.goBuilder</name>
				<arguments>
				</arguments>
			</buildCommand>
		</buildSpec>
		<natures>
			<nature>com.googlecode.goclipse.core.goNature</nature>
		</natures>
		 */
		URL url = this.getClass().getResource("go_project.stg");
		final STGroup projectSTG = new STGroupFile(url.getPath());
		final String projectName = page.getProjectName();
		final String author = page.getAuthor();
		final String email = page.getEmail();
		final String version = page.getVersion();
		final String packageString = page.getPackage();
		final String mainClass = page.getMainClassName();
		final String seedNode = page.getSeedNode();
		final String port = page.getPortString();
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					//Create the project
					IProject project = createProject(projectName, monitor, "com.googlecode.goclipse.core.goNature");
					project.setPersistentProperty(CoreActivator.SEED_NODE_PROPERTY, seedNode);
					project.setPersistentProperty(CoreActivator.PORT_PROPERTY, port);
					project.setPersistentProperty(CoreActivator.UDF_DIRECTORY, null);
					project.setPersistentProperty(CoreActivator.AQL_GENERATION_DIRECTORY, null);
					addBuilder(project, "com.googlecode.goclipse.goBuilder");
					
					// create source folders
					IFolder srcMainGo = project.getFolder("src");
					createFolder(srcMainGo);
					IFolder srcMainBin = project.getFolder("bin");
					createFolder(srcMainBin);
					// create aerospike folders
					IFolder srcUDF = project.getFolder(store.getString(PreferenceConstants.UDF_PATH));
					createFolder(srcUDF);
					IFolder srcGenerated = project.getFolder(store.getString(PreferenceConstants.GENERATION_PATH));
					createFolder(srcGenerated);
					IFolder srcAql = project.getFolder("aql");
					createFolder(srcAql);
					

					// create the .gitignore
					ST template = projectSTG.getInstanceOf("ignore");
					createFile(project, null, ".gitignore", monitor, template);
					// create the README
					template = projectSTG.getInstanceOf("readme");
					template.add("name", projectName);
					createFile(project, null, "README.md", monitor, template);
					
					// create main 
					IFolder mainFolder = srcMainGo.getFolder(packageToFolder(packageString));
					createFolder(mainFolder);
					template = projectSTG.getInstanceOf("module");
					template.add("name", mainClass);
					template.add("package", packageString);
					template.add("author", author);
					template.add("email", email);
					template.add("version", version);
					template.add("seedNode", seedNode);
					template.add("port", port);
					final IFile goMain = createFile(project, mainFolder, mainClass+".go", monitor, template);

					// open editor on main class
					monitor.setTaskName("Opening file for editing...");
					getShell().getDisplay().asyncExec(new Runnable() {
						public void run() {
							IWorkbenchPage page =
									PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
							try {
								IEditorPart editor = IDE.openEditor(page, goMain, true);
							} catch (PartInitException e) {
							}
						}
					});
					
					//project build
					project.build(IncrementalProjectBuilder.FULL_BUILD, monitor);
				} catch (CoreException e) {
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			}
		};
		try {
			getContainer().run(true, false, op);
		} catch (InterruptedException e) {
			return false;
		} catch (InvocationTargetException e) {
			Throwable realException = e.getTargetException();
			MessageDialog.openError(getShell(), "Error", realException.getMessage());
			return false;
		}
		return true;
	}
	
}