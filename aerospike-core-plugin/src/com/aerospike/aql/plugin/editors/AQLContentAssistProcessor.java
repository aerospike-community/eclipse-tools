package com.aerospike.aql.plugin.editors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;

import com.aerospike.core.CoreActivator;

public class AQLContentAssistProcessor implements IContentAssistProcessor {


	@Override
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer,
			int offset) {
		IDocument document = viewer.getDocument();
		String prefix = lastWord(document, offset);
		String indent = lastIndent(document, offset);


		// Computes the set of completions based on the offset provided
		List<ICompletionProposal> proposals = 
				new ArrayList<ICompletionProposal>();
		
		for (String keyword : AQLKeyWords.getKeywords()){
			if (keyword.toLowerCase().startsWith(prefix.toLowerCase())){
				//TODO
			}
		}
		// find the Aerospike console and display it
		
//			ResultsConsoleView results = new ResultsConsoleView();
//			IWorkbench wb = PlatformUI.getWorkbench();
//			IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
//			IWorkbenchPage page = win.getActivePage();
//			IConsoleView view = (IConsoleView) page.showView(IConsoleConstants.ID_CONSOLE_VIEW);
//			view.display(results.getConsole());


		return proposals.toArray(new ICompletionProposal[proposals.size()]);
	}

	@Override
	public IContextInformation[] computeContextInformation(ITextViewer viewer,
			int offset) {
		// TODO Computes the context information based on the offset provided
		return null;
	}

	@Override
	public char[] getCompletionProposalAutoActivationCharacters() {
		// Returns the characters that, when a user types them in the editor, automatically trigger the completion/context information to appear
		return new char[] {'.'};
	}

	@Override
	public char[] getContextInformationAutoActivationCharacters() {
		return null;
	}

	@Override
	public String getErrorMessage() {
		return null;
	}

	@Override
	public IContextInformationValidator getContextInformationValidator() {
		return null;
	}
	private String lastWord(IDocument doc, int offset) {
		try {
			for (int n = offset-1; n >= 0; n--) {
				char c = doc.getChar(n);
				if (!Character.isJavaIdentifierPart(c))
					return doc.get(n + 1, offset-n-1);
			}
		} catch (BadLocationException e) {
			CoreActivator.log(Status.ERROR, "Content assist failure", e);
		}
		return "";
	}
	private String lastIndent(IDocument doc, int offset) {
		try {
			int start = offset-1; 
			while (start >= 0 && doc.getChar(start)!= '\n') start--;
			int end = start;
			while (end < offset && Character.isSpaceChar(doc.getChar(end))) end++;
			return doc.get(start+1, end-start-1);
		} catch (BadLocationException e) {
			CoreActivator.log(Status.ERROR, "Content assist failure", e);
		}
		return "";
	}

}
