package com.aerospike.aql.plugin.actions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
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

import com.aerospike.aql.AQL;
import com.aerospike.client.AerospikeClient;
import com.aerospike.core.CoreActivator;
import com.aerospike.core.model.AsCluster;
import com.aerospike.core.properties.ClusterPropertyPage;
import com.aerospike.core.views.ResultsConsoleView;

public class RunOnCluster implements IWorkbenchWindowActionDelegate {
	private static final String COULD_NOT_EXECUTE_SQL_FILE = "Could not execute SQL file: ";
	private IWorkbenchWindow window;
	private ISelection selection;
	private Shell shell;
	private IWorkbenchPart targetPart;
	/**
	 * The constructor.
	 */
	public RunOnCluster() {
	}

	@SuppressWarnings("unused")
	public void run(IAction action) {
		if (selection != null && selection instanceof TreeSelection){
			TreeSelection ts = (TreeSelection)selection;
			Object element = ts.getFirstElement();
			if (element instanceof IFile && ((IFile)element).getFileExtension().equalsIgnoreCase("aql")){
				final IFile sqlFile = (IFile)element;
				if (sqlFile == null)
					return;
				final List<String> errorList = new ArrayList<String>();
				try {
					final AerospikeClient client = CoreActivator.getClient(sqlFile.getProject());
					if (client == null){
						CoreActivator.showError("Aerospike client is null");
						return;
					}
					if (!client.isConnected()){
						CoreActivator.showError("Aerospike client is not connected");
						return;
					}
					final ResultsConsoleView results = new ResultsConsoleView();
					// find the Aerospike console and display it
					IWorkbench wb = PlatformUI.getWorkbench();
					IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
					IWorkbenchPage page = win.getActivePage();
					IConsoleView view = (IConsoleView) page.showView(IConsoleConstants.ID_CONSOLE_VIEW);
					view.display(results.getConsole());
					
					
					final File aqlFile = sqlFile.getRawLocation().makeAbsolute().toFile();
					
					// create and run a Job to execute the AQL
					Job job = new Job("Run AQL on cluster") {

						@Override
						protected IStatus run(IProgressMonitor monitor) {
							results.report("Ecexuting AQL file: " + sqlFile.getName());
							AQL aql = new AQL(client);
							aql.execute(aqlFile, results, results);
							results.report(sqlFile.getName() + " completed");
							return Status.OK_STATUS;
						}
					};
					job.schedule();
				} catch (CoreException e) {
					CoreActivator.showError(e, COULD_NOT_EXECUTE_SQL_FILE + sqlFile.getName());
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