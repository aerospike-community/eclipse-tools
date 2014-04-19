package com.aerospike.aql.plugin.editors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordRule;

public class AQLPartitionScanner extends RuleBasedPartitionScanner {
	public final static String AQL_COMMENT= "__asql_comment"; //$NON-NLS-1$
	public final static String[] AQL_PARTITION_TYPES= new String[] { AQL_COMMENT };

	/**
	 * Detector for empty comments.
	 */
	static class EmptyCommentDetector implements IWordDetector {

		public boolean isWordStart(char c) {
			return (c == '/');
		}

		public boolean isWordPart(char c) {
			return (c == '*' || c == '/');
		}
	}
	
	/**
	 * 
	 */
	static class WordPredicateRule extends WordRule implements IPredicateRule {
		
		private IToken fSuccessToken;
		
		public WordPredicateRule(IToken successToken) {
			super(new EmptyCommentDetector());
			fSuccessToken= successToken;
			addWord("/**/", fSuccessToken); //$NON-NLS-1$
		}
		
		/*
		 * @see org.eclipse.jface.text.rules.IPredicateRule#evaluate(ICharacterScanner, boolean)
		 */
		public IToken evaluate(ICharacterScanner scanner, boolean resume) {
			return super.evaluate(scanner);
		}

		/*
		 * @see org.eclipse.jface.text.rules.IPredicateRule#getSuccessToken()
		 */
		public IToken getSuccessToken() {
			return fSuccessToken;
		}
	}

	public AQLPartitionScanner() {
		super();

		IToken comment= new Token(AQL_COMMENT);

		List<IPredicateRule> rules= new ArrayList<IPredicateRule>();

		// Add rule for single line comments.
		rules.add(new EndOfLineRule("#", Token.UNDEFINED)); //$NON-NLS-1$

		// Add rule for strings .
		rules.add(new SingleLineRule("'", "'", Token.UNDEFINED, '\\')); //$NON-NLS-2$ //$NON-NLS-1$

		// Add special case word rule.
		rules.add(new WordPredicateRule(comment));

		IPredicateRule[] result= new IPredicateRule[rules.size()];
		rules.toArray(result);
		setPredicateRules(result);

	}
}
