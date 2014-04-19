package com.aerospike.aql.plugin.editors;

import org.eclipse.jface.text.rules.IWhitespaceDetector;

public class AQLWhitespaceDetector implements IWhitespaceDetector {

	public boolean isWhitespace(char c) {
		return (c == ' ' || c == '\t' || c == '\n' || c == '\r');
	}
}
