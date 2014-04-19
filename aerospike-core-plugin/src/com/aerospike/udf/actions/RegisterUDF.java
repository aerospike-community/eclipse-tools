package com.aerospike.udf.actions;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
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
import com.aerospike.client.Language;
import com.aerospike.client.cluster.Node;
import com.aerospike.client.policy.ClientPolicy;
import com.aerospike.client.policy.Policy;
import com.aerospike.client.task.RegisterTask;
import com.aerospike.core.CoreActivator;
import com.aerospike.core.views.ResultsConsoleView;

/**
 * Our sample action implements workbench action delegate.
 * The action proxy will be created by the workbench and
 * shown in the UI. When the user tries to use the action,
 * this delegate will be created and execution will be 
 * delegated to it.
 * @see IWorkbenchWindowActionDelegate
 */
public class RegisterUDF implements IWorkbenchWindowActionDelegate {
	public static final String AEROSPIKE_UDF_PROBLEM = "AerospikeUDF.udfProblem";
	private ISelection selection;
	private Shell shell;
	private static final Pattern udfErrorPattern = Pattern.compile("Registration failed: compile_error\\nFile: .+\\nLine: (\\d+)\\nMessage: (.*)");
	/**
	 * The constructor.
	 */
	public RegisterUDF() {
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
			if (element instanceof IFile && ((IFile)element).getFileExtension().equalsIgnoreCase("lua")){
				IFile udfFile = (IFile)element;
				try {
					udfFile.deleteMarkers(AEROSPIKE_UDF_PROBLEM, false, IResource.DEPTH_ONE);
						AerospikeClient client = CoreActivator.getClient(udfFile.getProject());
					if (client == null){
						CoreActivator.log(IStatus.WARNING, "Aerospike client is null");
						return;
					}
					ClientPolicy policy = new ClientPolicy();
					policy.failIfNotConnected = true;
					final ResultsConsoleView results = new ResultsConsoleView();
					// find the Aerospike console and display it
					IWorkbench wb = PlatformUI.getWorkbench();
					IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
					IWorkbenchPage page = win.getActivePage();
					IConsoleView view = (IConsoleView) page.showView(IConsoleConstants.ID_CONSOLE_VIEW);
					view.display(results.getConsole());

					RegisterTask task = client.register(new Policy(), 
							udfFile.getRawLocation().toOSString(), 
							udfFile.getName(), 
							Language.LUA); //UDF language
					task.waitTillComplete();
					Node[] nodes = client.getNodes();
					StringBuilder message = new StringBuilder();
					message.append("UDF ");
					message.append(udfFile.getName());
					message.append("  registered successfully on nodes:\n ");
					
					for (Node node : nodes){
						message.append("\t");
						message.append(node.getHost());
						message.append(" ");
						message.append(node.getName());
						message.append("\n");
					}
					results.report(message.toString());
					MessageDialog.openInformation(
							shell,
							"Aerospike UDF",
							message.toString());
					udfFile.setPersistentProperty(CoreActivator.UDF_REGISTERED, "true");
				} catch (CoreException e) {
					CoreActivator.showError(e, "Could not register UDF in file: " + udfFile.getName());
				} catch (NumberFormatException e) {
					CoreActivator.showError(e, "Could not register UDF in file: " + udfFile.getName());
				} catch (AerospikeException e) {
					String message = e.getMessage();
					if (message.contains("compile_error")){
						/*
						 * Registration failed: compile_error
						 * File: bad.lua
						 * Line: 3
						 * Message: null
						 */
						Matcher matcher = udfErrorPattern.matcher(message);
						if (matcher.matches()){
							String error = matcher.group(2);
							int line = Integer.parseInt(matcher.group(1));
							addMarker(udfFile, error, line);
						}
						MessageDialog.openInformation(
								shell,
								"Aerospike UDF",
								message);
					} else {
						CoreActivator.showError(e, "Could not register UDF in file: " + udfFile.getName());
					}
				}

			} 
		}
	}
	private void addMarker(IFile file, String error, int line)  {
		try {
			IMarker marker = file.createMarker(AEROSPIKE_UDF_PROBLEM);
			marker.setAttribute(IMarker.LINE_NUMBER, line);
			marker.setAttribute(IMarker.MESSAGE, error);
			marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);

		} catch (CoreException e) {
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