package com.aerospike.core.preferences;

import org.eclipse.jface.preference.*;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbench;

import com.aerospike.core.CoreActivator;


public class AerospikePreferencePage
	extends FieldEditorPreferencePage
	implements IWorkbenchPreferencePage {

	private StringFieldEditor seedNodeEditor;
	private IPreferenceStore store;
	private IntegerFieldEditor portEditor;
	private StringFieldEditor udfDirectoryEditor;
	private StringFieldEditor genDirectoryEditor;

	public AerospikePreferencePage() {
		super(GRID);
		store = CoreActivator.getDefault().getPreferenceStore();
		setPreferenceStore(store);
		setDescription("Aerospike Preferences");
	}
	
	/**
	 * Creates the field editors. Field editors are abstractions of
	 * the common GUI blocks needed to manipulate various types
	 * of preferences. Each field editor knows how to save and
	 * restore itself.
	 */
	public void createFieldEditors() {
		seedNodeEditor = new StringFieldEditor(PreferenceConstants.SEED_NODE, "&Seed Node:", getFieldEditorParent());
		seedNodeEditor.setPreferenceStore(store);
		addField(seedNodeEditor);
		portEditor = new IntegerFieldEditor(PreferenceConstants.PORT, "&Port:", getFieldEditorParent());
		portEditor.setPreferenceStore(store);
		addField(portEditor);
//		addField(new DirectoryFieldEditor(PreferenceConstants.UDF_PATH, 
//				"&UDF Directory:", getFieldEditorParent()));
//		addField(new DirectoryFieldEditor(PreferenceConstants.GENERATION_PATH, 
//				"&Generation Directory:", getFieldEditorParent()));
		udfDirectoryEditor = new StringFieldEditor(PreferenceConstants.UDF_PATH, 
				"&UDF Directory:", getFieldEditorParent());
		udfDirectoryEditor.setPreferenceStore(store);
		addField(udfDirectoryEditor);
		genDirectoryEditor = new StringFieldEditor(PreferenceConstants.GENERATION_PATH, 
				"&Generation Directory:", getFieldEditorParent());
		genDirectoryEditor.setPreferenceStore(store);
		addField(genDirectoryEditor);

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}
	
}