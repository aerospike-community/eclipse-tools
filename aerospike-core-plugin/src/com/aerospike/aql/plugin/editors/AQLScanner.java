package com.aerospike.aql.plugin.editors;

import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WhitespaceRule;
import org.eclipse.jface.text.rules.WordRule;


public class AQLScanner extends RuleBasedScanner {

	public AQLScanner(ColorManager manager) {

		IToken keyword = new Token(new TextAttribute(manager.getColor(ColorManager.KEYWORD), null, Font.BOLD));
		IToken type= new Token(new TextAttribute(manager.getColor(ColorManager.CONSATANT), null, Font.BOLD  | Font.ITALIC));
		IToken string= new Token(new TextAttribute(manager.getColor(ColorManager.STRING)));
		IToken comment= new Token(new TextAttribute(manager.getColor(ColorManager.SINGLE_LINE_COMMENT)));
		IToken other= new Token(new TextAttribute(manager.getColor(ColorManager.DEFAULT)));

		List<IRule> rules= new ArrayList<IRule>();

		// Add rule for single line comments.
		rules.add(new EndOfLineRule("#", comment)); //$NON-NLS-1$

		// Add rule for strings and character constants.
		rules.add(new SingleLineRule("'", "'", string, '\\')); //$NON-NLS-2$ //$NON-NLS-1$

		// Add generic whitespace rule.
		rules.add(new WhitespaceRule(new AQLWhitespaceDetector()));

		// Add word rule for keywords, types, and constants. ignore case
		WordRule wordRule= new WordRule(new AQLWordDetector(), other, true);
		String[] keywords = AQLKeyWords.getKeywords();
		for (int i= 0; i < keywords.length; i++)
			wordRule.addWord(keywords[i], keyword);
		String[] constants = AQLKeyWords.getConstants();
		for (int i= 0; i < constants.length; i++)
			wordRule.addWord(constants[i], type);
		rules.add(wordRule);

		IRule[] result= new IRule[rules.size()];
		rules.toArray(result);
		setRules(result);

	}
}
