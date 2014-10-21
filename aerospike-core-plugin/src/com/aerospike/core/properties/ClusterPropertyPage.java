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
package com.aerospike.core.properties;

import org.apache.log4j.Level;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.wb.swt.ResourceManager;
import org.eclipse.wb.swt.SWTResourceManager;

import com.aerospike.client.AerospikeClient;
import com.aerospike.core.CoreActivator;
import com.aerospike.core.preferences.PreferenceConstants;
/**
 * This page edits the Cluster properties attached to this lua resource
 * @author peter
 *
 */
public class ClusterPropertyPage extends PropertyPage{
	public static final String ID_SQL = "com.aerospike.properties.clusterPropertyPage.aql";
	public static final String ID_UDF = "com.aerospike.properties.clusterPropertyPage.udf";
	public static final String ID = "com.aerospike.properties.clusterPropertyPage.project";
	private DirectoryFieldEditor udfDirectoryEditor;
	private DirectoryFieldEditor genDirectoryEditor;
	private StringFieldEditor seedNodeEditor;
	private IntegerFieldEditor portEditor;
	private IPreferenceStore store;
	private IntegerFieldEditor timeoutEditor;
	public ClusterPropertyPage() {
		setImageDescriptor(ResourceManager.getPluginImageDescriptor("aerospike-core-plugin", "icons/aerospike.logo.png"));
		setTitle("Aerospike Properties");
		this.store = CoreActivator.getDefault().getPreferenceStore();
	}

	/**
	 * @see PreferencePage#createContents(Composite)
	 */
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL);
		data.grabExcessHorizontalSpace = true;
		composite.setLayoutData(data);

		Label lblNewLabel = new Label(composite, SWT.NONE);
		lblNewLabel.setImage(ResourceManager.getPluginImage("aerospike-core-plugin", "icons/Cluster.png"));

		Label lblCluster = new Label(composite, SWT.NONE);
		lblCluster.setFont(SWTResourceManager.getFont("Lucida Grande", 13, SWT.BOLD));
		lblCluster.setLayoutData(new GridData(SWT.LEFT, SWT.BOTTOM, false, false, 1, 1));
		lblCluster.setText("Cluster");

		new Label(composite, SWT.NONE);

		seedNodeEditor = new StringFieldEditor(PreferenceConstants.SEED_NODE, "&Seed Node:", composite);
		new Label(composite, SWT.NONE);

		portEditor = new IntegerFieldEditor(PreferenceConstants.PORT, "&Port:", composite);
		new Label(composite, SWT.NONE);
		
		timeoutEditor = new IntegerFieldEditor(PreferenceConstants.CLUSTER_CONNECTION_TIMEOUT, "Connection &Timeput:", composite);
		new Label(composite, SWT.NONE);
		
		Label lblNewLabel_1 = new Label(composite, SWT.NONE);
		lblNewLabel_1.setLayoutData(new GridData(SWT.LEFT, SWT.BOTTOM, false, false, 1, 1));
		lblNewLabel_1.setImage(ResourceManager.getPluginImage("aerospike-core-plugin", "icons/UDF.png"));

		Label lblUserDefinedFunctions = new Label(composite, SWT.NONE);
		lblUserDefinedFunctions.setFont(SWTResourceManager.getFont("Lucida Grande", 13, SWT.BOLD));
		lblUserDefinedFunctions.setLayoutData(new GridData(SWT.LEFT, SWT.BOTTOM, false, false, 1, 1));
		lblUserDefinedFunctions.setText("User Defined Functions");

		new Label(composite, SWT.NONE);
		udfDirectoryEditor = new DirectoryFieldEditor(PreferenceConstants.UDF_PATH, 
				"&UDF Directory:", composite);

		Label label = new Label(composite, SWT.NONE);
		label.setImage(ResourceManager.getPluginImage("aerospike-core-plugin", "icons/Query.png"));

		Label lblAerospikeQueryLanguage = new Label(composite, SWT.NONE);
		lblAerospikeQueryLanguage.setFont(SWTResourceManager.getFont("Lucida Grande", 13, SWT.BOLD));
		lblAerospikeQueryLanguage.setLayoutData(new GridData(SWT.LEFT, SWT.BOTTOM, false, false, 1, 1));
		lblAerospikeQueryLanguage.setText("Aerospike Query Language");

		new Label(composite, SWT.NONE);
		genDirectoryEditor = new DirectoryFieldEditor(PreferenceConstants.GENERATION_PATH, 
				"&Generation Directory:", composite);
		new Label(composite, SWT.NONE);


		try {
			IProject project = ((IProject) getElement());
			if (project != null){
				String seedNode = CoreActivator.getSeedHost(project);
				seedNodeEditor.setStringValue(seedNode);

				int port = CoreActivator.getPort(project);
				portEditor.setStringValue(Integer.toString(port));

				int timeout = CoreActivator.getConnectionTimeout(project);
				timeoutEditor.setStringValue(Integer.toString(timeout));

				String udfDirectoryString = project.getPersistentProperty(CoreActivator.UDF_DIRECTORY);
				if (udfDirectoryString != null)
					udfDirectoryEditor.setStringValue(udfDirectoryString);
				else
					udfDirectoryEditor.setStringValue(store.getString(PreferenceConstants.UDF_PATH));

				String aqlOutputString = project.getPersistentProperty(CoreActivator.AQL_GENERATION_DIRECTORY);
				if (aqlOutputString != null)
					genDirectoryEditor.setStringValue(aqlOutputString);
				else
					genDirectoryEditor.setStringValue(store.getString(PreferenceConstants.GENERATION_PATH));
			}
		} catch (CoreException e) {
			CoreActivator.log(Level.ERROR_INT, "failure creating Properties page", e);
		}
		return composite;
	}

	@Override
	public boolean performOk() {
		performApply();
		return true;
	}

	@Override
	protected void performDefaults() {
		super.performDefaults();
		
		seedNodeEditor.setStringValue(store.getString(PreferenceConstants.SEED_NODE));

		portEditor.setStringValue(String.valueOf(store.getInt(PreferenceConstants.PORT)));

		timeoutEditor.setStringValue(String.valueOf(store.getInt(PreferenceConstants.CLUSTER_CONNECTION_TIMEOUT)));

		udfDirectoryEditor.setStringValue(store.getString(PreferenceConstants.UDF_PATH));

		genDirectoryEditor.setStringValue(store.getString(PreferenceConstants.GENERATION_PATH));

	}

	@Override
	protected void performApply() {
		try {
			IResource resource = ((IResource) getElement());
			if (resource != null){
				String seedNode = seedNodeEditor.getStringValue();
				if (seedNode != null && !seedNode.isEmpty())
					resource.setPersistentProperty(CoreActivator.SEED_NODE_PROPERTY, seedNode);
				else 
					resource.setPersistentProperty(CoreActivator.SEED_NODE_PROPERTY, null);
				
				String port = portEditor.getStringValue();
				if (port != null && !port.isEmpty())
					resource.setPersistentProperty(CoreActivator.PORT_PROPERTY, port);
				else
					resource.setPersistentProperty(CoreActivator.PORT_PROPERTY, null);
				
				String timeout = timeoutEditor.getStringValue();
				if (timeout != null && !timeout.isEmpty())
					resource.setPersistentProperty(CoreActivator.CLUSTER_CONNECTION_TIMEOUT_PROPERTY, timeout);
				else
					resource.setPersistentProperty(CoreActivator.CLUSTER_CONNECTION_TIMEOUT_PROPERTY, null);
				
				String udfDirectoryString = udfDirectoryEditor.getStringValue();
				if (udfDirectoryString != null && !udfDirectoryString.isEmpty())
					resource.setPersistentProperty(CoreActivator.UDF_DIRECTORY, udfDirectoryString);
				else 
					resource.setPersistentProperty(CoreActivator.UDF_DIRECTORY, null);
				String aqlOutputString = genDirectoryEditor.getStringValue();
				if (aqlOutputString != null && !aqlOutputString.isEmpty())
					resource.setPersistentProperty(CoreActivator.AQL_GENERATION_DIRECTORY, aqlOutputString);
				else 
					resource.setPersistentProperty(CoreActivator.AQL_GENERATION_DIRECTORY, null);
				
				// assume the cluster values have changed and disconnect the client
				AerospikeClient client = CoreActivator.getClient((IProject) resource);
				if (client != null)
					client.close();
				resource.setSessionProperty(CoreActivator.CLIENT_PROPERTY, null);
				
			}
		} catch (CoreException e) {
			CoreActivator.showError(e, "Error saving persistent properties");
		}
	}

}