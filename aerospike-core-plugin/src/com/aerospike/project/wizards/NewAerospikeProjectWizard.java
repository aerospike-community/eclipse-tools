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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Arrays;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import com.aerospike.aql.AQLGenerator;
import com.aerospike.aql.AQLGenerator.Language;
import com.aerospike.core.CoreActivator;
import com.aerospike.core.nature.AerospikeNature;
import com.aerospike.core.preferences.PreferenceConstants;


public abstract class NewAerospikeProjectWizard extends Wizard implements INewWizard {
	protected NewAerospikeProjectWizardPage page;
	protected ISelection selection;
	protected IPreferenceStore store;
	protected IWorkbench workbench;
	protected Language language;

	/**
	 * Constructor for NewAerospikeProjectWizard.
	 */
	public NewAerospikeProjectWizard() {
		super();
		setNeedsProgressMonitor(true);
		this.store = CoreActivator.getDefault().getPreferenceStore();
	}

	/**
	 * Adding the page to the wizard.
	 */

	public void addPages() {
		page = new NewAerospikeProjectWizardPage(selection);
		page.setLanguage(this.language);
		addPage(page);
	}

	/**
	 * This method is called when 'Finish' button is pressed in
	 * the wizard. We will create an operation and run it
	 * using wizard as execution context.
	 */
	public boolean performFinish() {
		return generateProject();
	}
	
	
	protected abstract boolean generateProject();
	
	
	protected void createFolder(IContainer container) throws CoreException {
	    if (!container.exists()) {
	        createFolder(container.getParent());
	        ((IFolder)container).create(false, false, null);
	    }
	}
	protected String packageToFolder(String packageString) {
	    return packageString.replace('.', '/');
	}
	
	protected void addBuilder(IProject project, String id) throws CoreException {
	      IProjectDescription desc = project.getDescription();
	      ICommand[] commands = desc.getBuildSpec();
	      for (int i = 0; i < commands.length; ++i)
	         if (commands[i].getBuilderName().equals(id))
	            return;
	      //add builder to project
	      ICommand command = desc.newCommand();
	      command.setBuilderName(id);
	      ICommand[] nc = new ICommand[commands.length + 1];
	      // Add it before other builders.
	      System.arraycopy(commands, 0, nc, 1, commands.length);
	      nc[0] = command;
	      desc.setBuildSpec(nc);
	      project.setDescription(desc, null);
	   }
	protected IProject createProject(String name, IProgressMonitor progressMonitor, String... projectNatures) throws CoreException{
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = root.getProject(name);
		project.create(progressMonitor);
		project.open(progressMonitor);
		IProjectDescription description = project.getDescription();
		if (projectNatures != null){
			projectNatures = Arrays.copyOf(projectNatures, projectNatures.length + 1);
			projectNatures[projectNatures.length-1] = AerospikeNature.NATURE_ID;
		} else {
			projectNatures = new String[] {  AerospikeNature.NATURE_ID };
		}
		description.setNatureIds(projectNatures);
		project.setDescription(description, null);

		return project;
	}
	
	protected IFile createFile(IProject project, IFolder folder,
			String fileName,
			IProgressMonitor monitor, ST template) throws CoreException{
		monitor.beginTask("Creating " + fileName, 2);
		IFile file = null;
		if (folder != null && folder.exists())
			file =  folder.getFile(new Path(fileName));
		else
			file =  project.getFile(new Path(fileName));
		try {
			String contense = template.render();
			InputStream stream = new ByteArrayInputStream(contense.getBytes());
			if (file.exists()) {
				file.setContents(stream, true, true, monitor);
			} else {
				file.create(stream, true, monitor);
			}
			stream.close();
		} catch (IOException e) {
		}
		return file;

	}

	protected void doFinish(
			String containerName,
			String fileName,
			IProgressMonitor monitor)
					throws CoreException {
		// create a sample file
		monitor.beginTask("Creating " + fileName, 2);
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IResource resource = root.findMember(new Path(containerName));
		if (!resource.exists() || !(resource instanceof IContainer)) {
			throwCoreException("Container \"" + containerName + "\" does not exist.");
		}
		IContainer container = (IContainer) resource;
		final IFile file = container.getFile(new Path(fileName));
		try {
			InputStream stream = openContentStream();
			if (file.exists()) {
				file.setContents(stream, true, true, monitor);
			} else {
				file.create(stream, true, monitor);
			}
			stream.close();
		} catch (IOException e) {
		}
		monitor.worked(1);
		monitor.setTaskName("Opening file for editing...");
		getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				IWorkbenchPage page =
						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				try {
					IDE.openEditor(page, file, true);
				} catch (PartInitException e) {
				}
			}
		});
		monitor.worked(1);
	}

	/**
	 * We will initialize file contents with a sample text.
	 */

	protected InputStream openContentStream() {
		String contents =
				"This is the initial file contents for *.mpe file that should be word-sorted in the Preview page of the multi-page editor";
		return new ByteArrayInputStream(contents.getBytes());
	}

	protected void throwCoreException(String message) throws CoreException {
		IStatus status =
				new Status(IStatus.ERROR, "new-example-project-wizard", IStatus.OK, message, null);
		throw new CoreException(status);
	}

	/**
	 * We will accept the selection in the workbench to see if
	 * we can initialize from it.
	 * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
		this.workbench = workbench;
		
	}
}