package com.aerospike.aql.plugin.editors;


import org.eclipse.jface.text.rules.IWordDetector;

public class AQLWordDetector implements IWordDetector {

	public boolean isWordPart(char character) {
		return Character.isJavaIdentifierPart(character);
	}
	
	public boolean isWordStart(char character) {
		return Character.isJavaIdentifierStart(character);
	}
}
