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
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
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

import com.aerospike.core.nature.AerospikeNature;


public class NewAerospikeProjectWizard extends Wizard implements INewWizard {
	public static final String PACKAGE_COM_AEROSPIKE_EXAMPLE = "com.aerospike.example";
	public static final String ID = "com.aerospike.eclipse.wizards.NewAerospikeProjectWizard";
	private NewAerospikeProjectWizardPage page;
	private ISelection selection;

	/**
	 * Constructor for NewExampleWizard.
	 */
	public NewAerospikeProjectWizard() {
		super();
		setNeedsProgressMonitor(true);
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
		URL url = this.getClass().getResource("project.stg");
		final STGroup projectSTG = new STGroupFile(url.getPath());
		final String projectName = page.getProjectName();
		final String author = page.getAuthor();
		final String email = page.getEmail();
		final String artifactId = page.getArtifiactId();
		final String version = page.getVersion();
		final String mainClass = page.getMainClassName();
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					//Create the project
					IProject project = createProject(projectName, monitor);
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
					IFolder srcUDF = project.getFolder("udf");
					createFolder(srcUDF);
					IFolder srcGenerated = project.getFolder("src/generated");
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
					template.add("package", PACKAGE_COM_AEROSPIKE_EXAMPLE);
					createFile(project, null, "pom.xml", monitor, template);
					// create the log4J.properties
					template = projectSTG.getInstanceOf("log4J");
					template.add("package", PACKAGE_COM_AEROSPIKE_EXAMPLE);
					createFile(project, srcMainResource, "log4j.properties", monitor, template);
					// create the .gitignore
					template = projectSTG.getInstanceOf("ignore");
					createFile(project, null, ".gitignore", monitor, template);
					// create the README
					template = projectSTG.getInstanceOf("readme");
					template.add("name", projectName);
					createFile(project, null, "README.md", monitor, template);
					// create package
					IPackageFragment pack = javaProject.getPackageFragmentRoot(srcMainJava)
							.createPackageFragment(PACKAGE_COM_AEROSPIKE_EXAMPLE, false, null);
					// create main class
					template = projectSTG.getInstanceOf("mainClass");
					template.add("name", mainClass);
					template.add("package", PACKAGE_COM_AEROSPIKE_EXAMPLE);
					template.add("author", author);
					final ICompilationUnit cu = pack.createCompilationUnit(mainClass+".java", template.render(), false, monitor);
					// open editor on main class
					monitor.setTaskName("Opening file for editing...");
					getShell().getDisplay().asyncExec(new Runnable() {
						public void run() {
							IWorkbenchPage page =
									PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
							try {
								IDE.openEditor(page, (IFile) cu.getResource(), true);
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