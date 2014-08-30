/* 
 * Copyright 2012-2014 Aerospike, Inc.
 *
 * Portions may be licensed to Aerospike, Inc. under one or more contributor
 * license agreements.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
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
	private StringFieldEditor aqlDirectoryEditor;
	private IntegerFieldEditor timeoutEditor;

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
		timeoutEditor = new IntegerFieldEditor(PreferenceConstants.CLUSTER_CONNECTION_TIMEOUT, "Connection &Timeout:", getFieldEditorParent());
		timeoutEditor.setPreferenceStore(store);
		addField(timeoutEditor);
		udfDirectoryEditor = new StringFieldEditor(PreferenceConstants.UDF_PATH, 
				"&UDF Directory:", getFieldEditorParent());
		udfDirectoryEditor.setPreferenceStore(store);
		addField(udfDirectoryEditor);
		aqlDirectoryEditor = new StringFieldEditor(PreferenceConstants.AQL_PATH, 
				"&AQL Directory:", getFieldEditorParent());
		aqlDirectoryEditor.setPreferenceStore(store);
		addField(aqlDirectoryEditor);
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