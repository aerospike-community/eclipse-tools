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


public class NewAerospikeProjectWizardJava extends NewAerospikeProjectWizard {

	/**
	 * Constructor for NewAerospikeProjectWizard.
	 */
	public NewAerospikeProjectWizardJava() {
		super();
		this.language = language.JAVA;
	}
	
	protected boolean generateProject(){
		URL url = this.getClass().getResource("java_project.stg");
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
		final boolean generateMain = page.getGenerateMain();
		final boolean generateJUnit = page.getGenerateJUnit();
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					//Create the project
					IProject project = createProject(projectName, monitor, "org.eclipse.jdt.core.javanature", "org.eclipse.m2e.core.maven2Nature");
					project.setPersistentProperty(CoreActivator.SEED_NODE_PROPERTY, seedNode);
					project.setPersistentProperty(CoreActivator.PORT_PROPERTY, port);
					project.setPersistentProperty(CoreActivator.UDF_DIRECTORY, null);
					project.setPersistentProperty(CoreActivator.AQL_GENERATION_DIRECTORY, null);
					addBuilder(project, "org.eclipse.jdt.core.javabuilder");
					addBuilder(project, "org.eclipse.m2e.core.maven2Builder" );
//					//make a java project
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
					// create the README
					template = projectSTG.getInstanceOf("classpath");
					createFile(project, null, ".classpath", monitor, template);
					// create JUnit
					if (generateJUnit){
//						IPackageFragment pack = javaProject.getPackageFragmentRoot(srcTestJava)
//								.createPackageFragment(packageString, false, null);
						IFolder testFolder = srcTestJava.getFolder(packageToFolder(packageString));
						createFolder(testFolder);
						template = projectSTG.getInstanceOf("junit");
						template.add("name", mainClass + "Test");
						template.add("package", packageString);
						template.add("classUnderTest", mainClass);
//						pack.createCompilationUnit(mainClass + "Test" + ".java", template.render(), false, monitor);
						final IFile junitMain = createFile(project, testFolder, mainClass + "Test.java", monitor, template);
					}
				// create main class
//					IPackageFragment pack = javaProject.getPackageFragmentRoot(srcMainJava)
//							.createPackageFragment(packageString, false, null);
					IFolder mainFolder = srcMainJava.getFolder(packageToFolder(packageString));
					createFolder(mainFolder);
					template = projectSTG.getInstanceOf("mainClass");
					template.add("name", mainClass);
					template.add("package", packageString);
					template.add("author", author);
					template.add("seedNode", seedNode);
					template.add("port", port);
//					final ICompilationUnit cu = pack.createCompilationUnit(mainClass+".java", template.render(), false, monitor);
					final IFile javaMain = createFile(project, mainFolder, mainClass+".java", monitor, template);

					// open editor on main class
					monitor.setTaskName("Opening file for editing...");
					getShell().getDisplay().asyncExec(new Runnable() {
						public void run() {
							IWorkbenchPage page =
									PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
							try {
								IEditorPart editor = IDE.openEditor(page, javaMain, true);
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