package com.aerospike.core.properties;

import org.apache.log4j.Level;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PropertyPage;

import com.aerospike.core.CoreActivator;
import com.aerospike.core.preferences.PreferenceConstants;

import org.eclipse.wb.swt.ResourceManager;
import org.eclipse.wb.swt.SWTResourceManager;
/**
 * This page edits the Cluster properties attached to this lua resource
 * @author peter
 *
 */
public class ClusterPropertyPage extends PropertyPage{
	public static final String ID_SQL = "com.aerospike.properties.clusterPropertyPage.aql";
	public static final String ID_UDF = "com.aerospike.properties.clusterPropertyPage.udf";
	private DirectoryFieldEditor udfDirectoryEditor;
	private DirectoryFieldEditor genDirectoryEditor;
	private StringFieldEditor seedNodeEditor;
	private IntegerFieldEditor portEditor;
	private IPreferenceStore store;
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
			IResource resource = ((IResource) getElement());
			if (resource != null){
				String seedNode = resource.getPersistentProperty(CoreActivator.SEED_NODE_PROPERTY);
				if (seedNode != null)
					seedNodeEditor.setStringValue(seedNode);
				else
					seedNodeEditor.setStringValue(store.getString(PreferenceConstants.SEED_NODE));

				String port = resource.getPersistentProperty(CoreActivator.PORT_PROPERTY);
				if (port != null)
					portEditor.setStringValue(port);
				else
					portEditor.setStringValue(String.valueOf(store.getInt(PreferenceConstants.PORT)));

				String udfDirectoryString = resource.getPersistentProperty(CoreActivator.UDF_DIRECTORY);
				if (udfDirectoryString != null)
					udfDirectoryEditor.setStringValue(udfDirectoryString);
				else
					udfDirectoryEditor.setStringValue(store.getString(PreferenceConstants.UDF_PATH));

				String aqlOutputString = resource.getPersistentProperty(CoreActivator.AQL_GENERATION_DIRECTORY);
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
			}
		} catch (CoreException e) {
			CoreActivator.showError(e, "Error saving persistent properties");
		}
	}

}