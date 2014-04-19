package com.aerospike.aql.plugin.builder;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.editors.text.FileDocumentProvider;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.xml.sax.helpers.DefaultHandler;

import com.aerospike.aql.AQL;
import com.aerospike.aql.grammar.IErrorReporter;
import com.aerospike.aql.plugin.editors.AQLDocumentProvider;
import com.aerospike.core.CoreActivator;
import com.aerospike.core.views.ResultsConsoleView;

public class AQLBuilder extends IncrementalProjectBuilder {
	Pattern errorPattern = Pattern.compile("line $d+:$d+");
	FileDocumentProvider documentProvider = new AQLDocumentProvider();
	
	public static final String ID = "com.aerospike.aql.plugin.builder.AQLBuilder";

	private static final String MARKER_TYPE = "AerospikeAQL.aqlProblem";

	class AQLDeltaVisitor implements IResourceDeltaVisitor {
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.resources.IResourceDeltaVisitor#visit(org.eclipse.core.resources.IResourceDelta)
		 */
		public boolean visit(IResourceDelta delta) throws CoreException {
			IResource resource = delta.getResource();
			switch (delta.getKind()) {
			case IResourceDelta.ADDED:
				// handle added resource
				checkAQL(resource, false);
				break;
			case IResourceDelta.REMOVED:
				// handle removed resource
				checkAQL(resource, true);
				break;
			case IResourceDelta.CHANGED:
				// handle changed resource
				checkAQL(resource, false);
				break;
			}
			//return true to continue visiting children.
			return true;
		}
	}

	class AQLResourceVisitor implements IResourceVisitor {
		public boolean visit(IResource resource) {
			checkAQL(resource, false);
			//return true to continue visiting children.
			return true;
		}
	}

	class AQLErrorHandler extends DefaultHandler implements IErrorReporter{
		
		private IFile file;
		private IDocument document;
		private IDocumentProvider provider = new TextFileDocumentProvider();

		public AQLErrorHandler(IFile file) {
			this.file = file;
			try {
				this.provider.connect(file);
			} catch (CoreException e) {
				CoreActivator.log(Status.ERROR, "Error connecting to " + file.getName(), e);
			}
			this.document = provider.getDocument(file);
			
		}

//		private void addMarker(AerospikeException e, int severity) {
//			addMarker(file, e.getMessage(), 
//					1, 1, 1, severity);
//		}
		private void addMarker(IFile file, String error, int line,
				int charStart, int charEnd, int severity) throws BadLocationException {
			try {
				IMarker marker = file.createMarker(MARKER_TYPE);
				int offset = document.getLineOffset(line - 1);
				marker.setAttribute(IMarker.CHAR_START, charStart + offset);
				marker.setAttribute(IMarker.CHAR_END, charEnd + offset);
				marker.setAttribute(IMarker.LINE_NUMBER, line);
				marker.setAttribute(IMarker.MESSAGE, error);
				marker.setAttribute(IMarker.SEVERITY, severity);
				
			} catch (CoreException e) {
			}
			
		}

//		public void error(AerospikeException exception) {
//			addMarker(exception, IMarker.SEVERITY_ERROR);
//		}
//
//		public void fatalError(AerospikeException exception) {
//			addMarker(exception, IMarker.SEVERITY_ERROR);
//		}
//
//		public void warning(AerospikeException exception) {
//			addMarker(exception, IMarker.SEVERITY_WARNING);
//		}

		@Override
		public void reportError(int line, int charStart, int charEnd, String message) {
			try {
				addMarker(this.file, message, 
						line, charStart, charEnd, IMarker.SEVERITY_ERROR);
			} catch (BadLocationException e) {
				CoreActivator.log(Status.ERROR, "Error adding marker to " + file.getName(), e);
			}
		}

	}



	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.internal.events.InternalBuilder#build(int,
	 *      java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
			throws CoreException {
		if (kind == FULL_BUILD) {
			fullBuild(monitor);
		} else {
			IResourceDelta delta = getDelta(getProject());
			if (delta == null) {
				fullBuild(monitor);
			} else {
				incrementalBuild(delta, monitor);
			}
		}
		return null;
	}

	protected void clean(IProgressMonitor monitor) throws CoreException {
		// delete markers set and files created
		getProject().deleteMarkers(MARKER_TYPE, true, IResource.DEPTH_INFINITE);
	}

	void checkAQL(IResource resource, boolean remove) {
		if (resource instanceof IFile && resource.getName().endsWith(".aql")) {
			IFile file = (IFile) resource;
			deleteMarkers(file);
			AQLErrorHandler reporter = new AQLErrorHandler(file);
			try {
				processAQL(file, reporter, remove);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}

	private void processAQL(IFile sqlFile, AQLErrorHandler reporter, boolean remove) {
		int errors = 0;
		try {
			AQLErrorHandler errorHandler = new AQLErrorHandler(sqlFile);
			final ResultsConsoleView results = new ResultsConsoleView();
			// find the Aerospike console and display it
//			IWorkbench wb = PlatformUI.getWorkbench();
//			IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
//			IWorkbenchPage page = win.getActivePage();
//			IConsoleView view = (IConsoleView) page.showView(IConsoleConstants.ID_CONSOLE_VIEW);
//			view.display(results.getConsole());
			AQL aql = new AQL();
			File sourceFile = new File(sqlFile.getRawLocation().toOSString());
			aql.compile(sourceFile, reporter, results);
		} catch (NumberFormatException e) {
			CoreActivator.showError(e, "Could not process AQL file: " + sqlFile.getName());
		} catch (IOException e) {
			CoreActivator.showError(e, "Could not process AQL file: " + sqlFile.getName());
		}
	}

	private void deleteMarkers(IFile file) {
		try {
			file.deleteMarkers(MARKER_TYPE, false, IResource.DEPTH_ZERO);
		} catch (CoreException ce) {
		}
	}

	protected void fullBuild(final IProgressMonitor monitor)
			throws CoreException {
		try {
			getProject().accept(new AQLResourceVisitor());
		} catch (CoreException e) {
		}
	}


	protected void incrementalBuild(IResourceDelta delta,
			IProgressMonitor monitor) throws CoreException {
		// the visitor does the work.
		delta.accept(new AQLDeltaVisitor());
	}
}
