package com.aerospike.aql.plugin.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

public class GenerateSnipet implements IWorkbenchWindowActionDelegate{
	public static final String ID = "com.aerospike.aql.plugin.actions.GenerateSnipet";
	private IWorkbenchWindow window;
	private ISelection selection;
	private Shell shell;
	private IWorkbenchPart targetPart;

	public GenerateSnipet() {
	}

	@Override
	public void run(IAction action) {
		
//		if (selection != null && selection instanceof TreeSelection){
//			TreeSelection ts = (TreeSelection)selection;
//			Object element = ts.getFirstElement();
//			if (element instanceof IFile && ((IFile)element).getFileExtension().equalsIgnoreCase("aql")){
//				final IFile sqlFile = (IFile)element;
//				if (sqlFile == null)
//					return;
//				try {
//					final List<String> errorList = new ArrayList<String>();
//					final String actionID = action.getId();
//					final AQLResult results = new AQLResult();
//					// find the Aerospike console and display it
//					IWorkbench wb = PlatformUI.getWorkbench();
//					IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
//					IWorkbenchPage page = win.getActivePage();
//					IConsoleView view = (IConsoleView) page.showView(IConsoleConstants.ID_CONSOLE_VIEW);
//					view.display(results.getConsole());
//					// set generation language
//					String extension;
//					final com.aerospike.aql.AQL.Language language;
//						if (actionID.equals("com.aerospike.aql.plugin.actions.GenerateSource.java.popup")){
//							language = com.aerospike.aql.AQL.Language.JAVA;
//							extension = ".java";
//						} else if (actionID.equals("com.aerospike.aql.plugin.actions.GenerateSource.c.popup")){
//							language = com.aerospike.aql.AQL.Language.C;
//							extension = ".c";
//						} else if (actionID.equals("com.aerospike.aql.plugin.actions.GenerateSource.csharp.popup")){
//							language = com.aerospike.aql.AQL.Language.CSHARP;
//							extension = ".csharp";
//						} else {
//							return;
//						}
//					IProject project = sqlFile.getProject();
//					IPath outputPath;
//					String sqlFileName = sqlFile.getName();
//					String outputFileName = sqlFileName.substring(0, sqlFileName.lastIndexOf('.')) + extension ;
//
//					final AsCluster cluster = (AsCluster) project.getSessionProperty(CoreActivator.CLUSTER_PROPERTY);
//					String outputDirectoryString = project.getPersistentProperty(CoreActivator.AQL_GENERATION_DIRECTORY);
//					if (outputDirectoryString == null || outputDirectoryString.isEmpty()){
//						outputPath = project.getLocation().append(outputFileName);
//					} else {
//						IPath dirPath = project.getLocation().append(outputDirectoryString);
//						if (!dirPath.toFile().exists())
//							dirPath.toFile().mkdirs();
//						outputPath = dirPath.append(outputFileName);
//					}
//					final File outputFile = outputPath.toFile();
//					IPath location = sqlFile.getLocation();
//					final File file = location.toFile();
//					final IFile outputIFile = project.getWorkspace().getRoot().getFileForLocation(outputPath);
//					
//					
//					Job job = new Job("Generate source code from AQL: " + sqlFile.getName()) {
//
//						@Override
//						protected IStatus run(IProgressMonitor monitor) {
//							AQL aql = new AQL();
//							try {
//								String seedNode = "";
//								int port = 3000;
//								if (cluster!=null){
//									seedNode = cluster.getSeedHost();
//									port = cluster.getPort();
//								} else {
//									IPreferenceStore store = CoreActivator.getDefault().getPreferenceStore();
//									seedNode = store.getString(PreferenceConstants.SEED_NODE);
//									port = store.getInt(PreferenceConstants.PORT);
//								}
//								aql.compileAndGenerate(file, outputFile, language, seedNode, port);
//								results.report("Completed generation for " + sqlFile.getName());
//								outputIFile.getParent().refreshLocal(IResource.DEPTH_ONE, null);
//								return Status.OK_STATUS;
//							} catch (Exception e) {
//								CoreActivator.showError(e, COULD_NOT_GENERATE_CODE_FROM_SQL_FILE + sqlFile.getName());
//								return Status.CANCEL_STATUS;
//							}
//						}
//					};
//					job.setUser(true);
//					job.schedule();
//				} catch (PartInitException e) {
//					CoreActivator.showError(e, COULD_NOT_GENERATE_CODE_FROM_SQL_FILE + sqlFile.getName());
//				} catch (CoreException e) {
//					CoreActivator.showError(e, COULD_NOT_GENERATE_CODE_FROM_SQL_FILE + sqlFile.getName());
//				}
//			}
//		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
	}

	@Override
	public void dispose() {
		
	}

	@Override
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}
	
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		this.targetPart = targetPart;
		this.shell = targetPart.getSite().getShell();
	}

}
