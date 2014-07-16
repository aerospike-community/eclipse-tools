package com.aerospike.aql.plugin.editors;

import org.eclipse.ui.editors.text.TextEditor;



public class AQLEditor extends TextEditor {

	private ColorManager colorManager;

	public AQLEditor() {
		super();
		this.colorManager = new ColorManager();
		setSourceViewerConfiguration(new AQLConfiguration(colorManager));
		setDocumentProvider(new AQLDocumentProvider());

	}
	public void dispose() {
		colorManager.dispose();
		super.dispose();
	}

	
}
