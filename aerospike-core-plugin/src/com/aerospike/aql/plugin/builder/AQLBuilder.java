/* 
 * Copyright 2012-2015 Aerospike, Inc.
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
package com.aerospike.aql.plugin.builder;

import java.io.File;
import java.io.IOException;
import java.util.List;
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
import com.aerospike.client.AerospikeException;
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
		private int errors = 0;

		public AQLErrorHandler(IFile file) {
			this.file = file;
			try {
				this.provider.connect(file);
			} catch (CoreException e) {
				CoreActivator.log(Status.ERROR, "Error connecting to " + file.getName(), e);
			}
			this.document = provider.getDocument(file);
			
		}

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


		@Override
		public void reportError(int line, int charStart, int charEnd, String message) {
			try {
				addMarker(this.file, message, 
						line, charStart, charEnd, IMarker.SEVERITY_ERROR);
				errors++;
			} catch (BadLocationException e) {
				CoreActivator.log(Status.ERROR, "Error adding marker to " + file.getName(), e);
			}
		}


		@Override
		public int getErrorCount() {
			return errors;
		}

		@Override
		public List<String> getErrorList() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void reportError(int line, String message) {
			try {
				addMarker(this.file, message, 
						line, 0, 0, IMarker.SEVERITY_ERROR);
				errors++;
			} catch (BadLocationException e) {
				CoreActivator.log(Status.ERROR, "Error adding marker to " + file.getName(), e);
			}
			
		}

		@Override
		public void reportError(int arg0, AerospikeException arg1) {
			// TODO Auto-generated method stub
			
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
			//AQLErrorHandler errorHandler = new AQLErrorHandler(sqlFile);
			//final ResultsConsoleView results = new ResultsConsoleView();
			// find the Aerospike console and display it
//			IWorkbench wb = PlatformUI.getWorkbench();
//			IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
//			IWorkbenchPage page = win.getActivePage();
//			IConsoleView view = (IConsoleView) page.showView(IConsoleConstants.ID_CONSOLE_VIEW);
//			view.display(results.getConsole());
			AQL aql = new AQL();
			File sourceFile = new File(sqlFile.getRawLocation().toOSString());
			aql.setErrorReporter(reporter);
			aql.compile(sourceFile);
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
