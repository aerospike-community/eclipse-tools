package com.aerospike.aql.plugin.actions;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.jface.text.TextViewer;
import swing2swt.layout.BorderLayout;


public class AqlSnippetDialog extends Dialog {

	/**
	 * @wbp.parser.constructor
	 */
	public AqlSnippetDialog(Shell parentShell) {
		super(parentShell);
	}

	protected AqlSnippetDialog(IShellProvider parentShell) {
		super(parentShell);
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = new Composite(parent, SWT.BORDER);
		container.setLayout(new BorderLayout(0, 0));
		
		TextViewer textViewer = new TextViewer(container, SWT.BORDER);
		StyledText styledText = textViewer.getTextWidget();
		return container;
	}
}
