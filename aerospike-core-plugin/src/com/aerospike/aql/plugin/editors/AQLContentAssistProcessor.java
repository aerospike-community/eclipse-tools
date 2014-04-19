package com.aerospike.aql.plugin.editors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;

public class AQLContentAssistProcessor implements IContentAssistProcessor {

	@Override
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer,
			int offset) {
		// Computes the set of completions based on the offset provided
		List<ICompletionProposal> proposals = 
	            new ArrayList<ICompletionProposal>();

	        //compute proposals at offset

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

}
