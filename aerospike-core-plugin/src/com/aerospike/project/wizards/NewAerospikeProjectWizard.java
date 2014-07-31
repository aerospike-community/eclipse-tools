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
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;
import org.eclipse.jdt.launching.environments.IExecutionEnvironmentsManager;
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

import com.aerospike.core.CoreActivator;
import com.aerospike.core.nature.AerospikeNature;
import com.aerospike.core.preferences.PreferenceConstants;


public class NewAerospikeProjectWizard extends Wizard implements INewWizard {
	public static final String ID = "com.aerospike.eclipse.wizards.NewAerospikeProjectWizard";
	private NewAerospikeProjectWizardPage page;
	private ISelection selection;
	private IPreferenceStore store;

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
		addPage(page);
	}

	/**
	 * This method is called when 'Finish' button is pressed in
	 * the wizard. We will create an operation and run it
	 * using wizard as execution context.
	 */
	public boolean performFinish() {
		return generateJavaProject();
	}
	
	protected boolean generateJavaProject(){
		URL url = this.getClass().getResource("project.stg");
		final STGroup projectSTG = new STGroupFile(url.getPath());
		final String projectName = page.getProjectName();
		final String author = page.getAuthor();
		final String email = page.getEmail();
		final String artifactId = page.getArtifiactId();
		final String version = page.getVersion();
		final String packageString = page.getPackage();
		final String mainClass = page.getMainClassName();
		final String seedNode = page.getSeedNode();
		final String port = page.getPortString();
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					//Create the project
					IProject project = createProject(projectName, monitor);
					project.setPersistentProperty(CoreActivator.SEED_NODE_PROPERTY, seedNode);
					project.setPersistentProperty(CoreActivator.PORT_PROPERTY, port);
					project.setPersistentProperty(CoreActivator.UDF_DIRECTORY, null);
					project.setPersistentProperty(CoreActivator.AQL_GENERATION_DIRECTORY, null);
					//make a java project
					IJavaProject javaProject = JavaCore.create(project);
					// create the classpath entries
					List<IClasspathEntry> entries = new ArrayList<IClasspathEntry>();
					IExecutionEnvironmentsManager executionEnvironmentsManager = JavaRuntime.getExecutionEnvironmentsManager();
					IExecutionEnvironment[] executionEnvironments = executionEnvironmentsManager.getExecutionEnvironments();
					for (IExecutionEnvironment iExecutionEnvironment : executionEnvironments) {
					    // We will look for JavaSE-1.6 as the JRE container to add to our classpath
					    if ("JavaSE-1.6".equals(iExecutionEnvironment.getId())) {
					        entries.add(JavaCore.newContainerEntry(JavaRuntime.newJREContainerPath(iExecutionEnvironment)));
					        break;
					    } else if ("JavaSE-1.5".equals(iExecutionEnvironment.getId())) {
					        entries.add(JavaCore.newContainerEntry(JavaRuntime.newJREContainerPath(iExecutionEnvironment)));
					        break;
					    }
					}
					IClasspathEntry mavenEntry = JavaCore.newContainerEntry(new Path("org.eclipse.m2e.MAVEN2_CLASSPATH_CONTAINER"), 
							new IAccessRule[0], new IClasspathAttribute[] {
								JavaCore.newClasspathAttribute("org.eclipse.jst.component.dependency", "/WEB-INF/lib") 
								}, false);
					entries.add(mavenEntry);
					javaProject.setRawClasspath(entries.toArray(new IClasspathEntry[entries.size()]), null);
					// create source folders
					IFolder srcMainJava = project.getFolder("src/main/java");
					createFolder(srcMainJava);
					IFolder srcMainResource = project.getFolder("src/main/resource");
					createFolder(srcMainResource);
					IFolder srcTestJava = project.getFolder("src/test/java");
					createFolder(srcTestJava);
					IFolder srcTestResource = project.getFolder("src/test/resource");
					createFolder(srcTestResource);
					// create aerospike folders
					IFolder srcUDF = project.getFolder(store.getString(PreferenceConstants.UDF_PATH));
					createFolder(srcUDF);
					IFolder srcGenerated = project.getFolder(store.getString(PreferenceConstants.GENERATION_PATH));
					createFolder(srcGenerated);
					IFolder srcAql = project.getFolder("aql");
					createFolder(srcAql);
					//
					IPackageFragmentRoot mainJava = javaProject.getPackageFragmentRoot(srcMainJava);
					IPackageFragmentRoot mainResource = javaProject.getPackageFragmentRoot(srcMainResource);
					IPackageFragmentRoot testJava = javaProject.getPackageFragmentRoot(srcTestJava);
					IPackageFragmentRoot testResource = javaProject.getPackageFragmentRoot(srcTestResource);
					IPackageFragmentRoot mainGenerated = javaProject.getPackageFragmentRoot(srcGenerated);
					IClasspathEntry[] oldEntries = javaProject.getRawClasspath();
					IClasspathEntry[] newEntries = new IClasspathEntry[oldEntries.length + 5];
					System.arraycopy(oldEntries, 0, newEntries, 0, oldEntries.length);
					newEntries[oldEntries.length] = JavaCore.newSourceEntry(mainJava.getPath());
					newEntries[oldEntries.length +1] = JavaCore.newSourceEntry(mainResource.getPath());
					newEntries[oldEntries.length +2] = JavaCore.newSourceEntry(testJava.getPath());
					newEntries[oldEntries.length +3] = JavaCore.newSourceEntry(testResource.getPath());
					newEntries[oldEntries.length +4] = JavaCore.newSourceEntry(mainGenerated.getPath());
					javaProject.setRawClasspath(newEntries, monitor);
					// create the pom.xml
					ST template = projectSTG.getInstanceOf("pom");
					template.add("name", projectName);
					template.add("artifactId", artifactId);
					template.add("version", version);
					template.add("author", author);
					template.add("email", email);
					template.add("mainClass", mainClass);
					template.add("package", packageString);
					createFile(project, null, "pom.xml", monitor, template);
					// create the log4J.properties
					template = projectSTG.getInstanceOf("log4J");
					template.add("package", packageString);
					template.add("mainClass", mainClass);
					createFile(project, srcMainJava, "log4j.properties", monitor, template);
					// create the .gitignore
					template = projectSTG.getInstanceOf("ignore");
					createFile(project, null, ".gitignore", monitor, template);
					// create the README
					template = projectSTG.getInstanceOf("readme");
					template.add("name", projectName);
					createFile(project, null, "README.md", monitor, template);
					// create package
					IPackageFragment pack = javaProject.getPackageFragmentRoot(srcMainJava)
							.createPackageFragment(packageString, false, null);
					// create main class
					template = projectSTG.getInstanceOf("mainClass");
					template.add("name", mainClass);
					template.add("package", packageString);
					template.add("author", author);
					template.add("seedNode", seedNode);
					template.add("port", port);
					final ICompilationUnit cu = pack.createCompilationUnit(mainClass+".java", template.render(), false, monitor);
					// open editor on main class
					monitor.setTaskName("Opening file for editing...");
					getShell().getDisplay().asyncExec(new Runnable() {
						public void run() {
							IWorkbenchPage page =
									PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
							try {
								IEditorPart editor = IDE.openEditor(page, (IFile) cu.getResource(), true);
							} catch (PartInitException e) {
							}
						}
					});

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
	
//	protected boolean generateCProject(){
//		// Create and persist Standard Makefile project
//		{
//		// Create model project and accompanied project description
//		IWorkspace workspace = ResourcesPlugin.getWorkspace();
//		IWorkspaceRoot root = workspace.getRoot();
//
//		IProject newProjectHandle = root.getProject(projectName);
//		Assert.assertNotNull(newProjectHandle);
//		Assert.assertFalse(newProjectHandle.exists());
//
//		IProjectDescription description = 
//		workspace.newProjectDescription(newProjectHandle.getName());
//		IProject project = 
//		CCorePlugin.getDefault().createCDTProject(description, newProjectHandle, 
//		new NullProgressMonitor());
//		Assert.assertTrue(newProjectHandle.isOpen());
//
//		ICProjectDescriptionManager mngr = 
//		CoreModel.getDefault().getProjectDescriptionManager();
//		ICProjectDescription des = mngr.createProjectDescription(project, 
//		false);
//		ManagedProject mProj = new ManagedProject(des);
//
//		Configuration cfg = new Configuration(mProj, null, 
//		"your.configuration.id", "YourConfigurationName");
//
//		IBuilder bld = cfg.getEditableBuilder();
//		Assert.assertNotNull(bld);
//		Assert.assertFalse(bld.isInternalBuilder());
//
//		bld.setManagedBuildOn(false);
//
//		CConfigurationData data = cfg.getConfigurationData();
//		Assert.assertNotNull(data);
//		des.createConfiguration(ManagedBuildManager.CFG_DATA_PROVIDE R_ID, data);
//
//		// Persist the project description
//		mngr.setProjectDescription(project, des);
//
//		project.close(null);
//		}
//		return true;
//	}
	
	private void createFolder(IContainer container) throws CoreException {
	    if (!container.exists()) {
	        createFolder(container.getParent());
	        ((IFolder)container).create(false, false, null);
	    }
	}
	private IProject createProject(String name, IProgressMonitor progressMonitor) throws CoreException{
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = root.getProject(name);
		project.create(progressMonitor);
		project.open(progressMonitor);
		IProjectDescription description = project.getDescription();
		description.setNatureIds(new String[] { JavaCore.NATURE_ID, "org.eclipse.m2e.core.maven2Nature", AerospikeNature.NATURE_ID });
		project.setDescription(description, null);

		return project;
	}
	private void createFile(IProject project, IFolder folder,
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

	}
	/**
	 * The worker method. It will find the container, create the
	 * file if missing or just replace its contents, and open
	 * the editor on the newly created file.
	 */

	private void doFinish(
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

	private InputStream openContentStream() {
		String contents =
				"This is the initial file contents for *.mpe file that should be word-sorted in the Preview page of the multi-page editor";
		return new ByteArrayInputStream(contents.getBytes());
	}

	private void throwCoreException(String message) throws CoreException {
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
	}
}