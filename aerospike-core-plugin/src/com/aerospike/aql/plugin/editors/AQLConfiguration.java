package com.aerospike.aql.plugin.editors;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;

public class AQLConfiguration extends SourceViewerConfiguration {
	private AQLDoubleClickStrategy doubleClickStrategy;
	private AQLScanner scanner;
	private ColorManager colorManager;

	public AQLConfiguration(ColorManager colorManager) {
		this.colorManager = colorManager;
	}
	public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
		return new String[] {
			IDocument.DEFAULT_CONTENT_TYPE,
			AQLPartitionScanner.AQL_COMMENT};
	}
	public ITextDoubleClickStrategy getDoubleClickStrategy(
		ISourceViewer sourceViewer,
		String contentType) {
		if (doubleClickStrategy == null)
			doubleClickStrategy = new AQLDoubleClickStrategy();
		return doubleClickStrategy;
	}

	protected AQLScanner getScanner() {
		if (scanner == null) {
			scanner = new AQLScanner(colorManager);
			scanner.setDefaultReturnToken(
				new Token(
					new TextAttribute(
						colorManager.getColor(ColorManager.DEFAULT))));
		}
		return scanner;
	}

	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
		PresentationReconciler reconciler = new PresentationReconciler();

		DefaultDamagerRepairer dr =
			new DefaultDamagerRepairer(getScanner());
		reconciler.setDamager(dr, AQLPartitionScanner.AQL_COMMENT);
		reconciler.setRepairer(dr, AQLPartitionScanner.AQL_COMMENT);

		dr = new DefaultDamagerRepairer(getScanner());
		reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);

		NonRuleBasedDamagerRepairer ndr =
			new NonRuleBasedDamagerRepairer(
				new TextAttribute(
					colorManager.getColor(ColorManager.MULTI_LINE_COMMENT)));
		reconciler.setDamager(ndr, AQLPartitionScanner.AQL_COMMENT);
		reconciler.setRepairer(ndr, AQLPartitionScanner.AQL_COMMENT);

		return reconciler;
	}

	@Override
	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
		AQLContentAssistant aqlAssitant = new AQLContentAssistant();
		aqlAssitant.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));
		aqlAssitant.setContentAssistProcessor(new AQLContentAssistProcessor(), IDocument.DEFAULT_CONTENT_TYPE);
		aqlAssitant.setAutoActivationDelay(500);
		aqlAssitant.enableAutoActivation(true);
		aqlAssitant.setProposalSelectorBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		return aqlAssitant;
	}
}