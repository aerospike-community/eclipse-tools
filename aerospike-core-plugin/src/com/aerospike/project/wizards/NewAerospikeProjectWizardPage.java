/* 
 * Copyright 2012-2015 Aerospike, Inc.
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
package com.aerospike.project.wizards;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;

import com.aerospike.aql.AQLGenerator.Language;
import com.aerospike.core.CoreActivator;
import com.aerospike.core.preferences.PreferenceConstants;


public class NewAerospikeProjectWizardPage extends WizardPage {
	private Text projectNameText;

	private Text mainClassText;

	private ISelection selection;
	private Label lblprojectName;
	private GridData gd_projectNameText;
	private Label lblMainClass;
	private Label lblAuthor;
	private Text authorText;
	private Label lblEmail;
	private Text emailText;
	private Label lblArtifactId;
	private Text artifactIdText;
	private Label lblVersion;
	private Text versionText;
	private Label lblSeedNode;
	private Text seedNodeText;
	private Label lblPort;
	private Text portText;
	private IPreferenceStore store;
	private Label lblPackage;
	private Text packageText;
	private Button btnMainCheckButton;
	private Composite genElemetsComposit;
	private Button btnJunitCheckButton;

	private Language lang;
	

	/**
	 * Constructor for SampleNewWizardPage.
	 * 
	 * @param pageName
	 */
	public NewAerospikeProjectWizardPage(ISelection selection) {
		super("newAerospikeExamplePage");
		setTitle("New Aerospike project");
		setDescription("This wizard creates a new Aerospike project. This project can be used as a start for an Aerospike application");
		this.selection = selection;
		this.store = CoreActivator.getDefault().getPreferenceStore();
	}

	/**
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 3;
		layout.verticalSpacing = 9;
		Label label;
		lblprojectName = new Label(container, SWT.NULL);
		lblprojectName.setText("&Project Name:");

		projectNameText = new Text(container, SWT.BORDER | SWT.SINGLE);
		GridData gd;
		gd_projectNameText = new GridData(GridData.FILL_HORIZONTAL);
		projectNameText.setLayoutData(gd_projectNameText);
		projectNameText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});

		Button button = new Button(container, SWT.PUSH);
		button.setText("Browse...");
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleBrowse();
			}
		});
		
		lblArtifactId = new Label(container, SWT.NONE);
		lblArtifactId.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblArtifactId.setText("Artifact Id:");
		
		artifactIdText = new Text(container, SWT.BORDER);
		artifactIdText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(container, SWT.NONE);
		
		lblVersion = new Label(container, SWT.NONE);
		lblVersion.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblVersion.setText("Version:");
		
		versionText = new Text(container, SWT.BORDER);
		versionText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		versionText.setText("1.0.0");
		new Label(container, SWT.NONE);
		
		lblPackage = new Label(container, SWT.NONE);
		lblPackage.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblPackage.setText("Module:");
		
		packageText = new Text(container, SWT.BORDER);
		packageText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(container, SWT.NONE);
		
		lblMainClass = new Label(container, SWT.NONE);
		lblMainClass.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblMainClass.setText("Main class:");

		mainClassText = new Text(container, SWT.BORDER | SWT.SINGLE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		mainClassText.setLayoutData(gd);
		mainClassText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});
		dialogChanged();
		setControl(container);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		
		genElemetsComposit = new Composite(container, SWT.NONE);
		genElemetsComposit.setLayout(new GridLayout(2, false));
		
		btnMainCheckButton = new Button(genElemetsComposit, SWT.CHECK);
		btnMainCheckButton.setText("Main method");
		
		btnJunitCheckButton = new Button(genElemetsComposit, SWT.CHECK);
		btnJunitCheckButton.setText("Junit Test");
		new Label(container, SWT.NONE);
		
		lblAuthor = new Label(container, SWT.NONE);
		lblAuthor.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblAuthor.setText("Author");
		
		authorText = new Text(container, SWT.BORDER);
		authorText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(container, SWT.NONE);
		
		lblEmail = new Label(container, SWT.NONE);
		lblEmail.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblEmail.setText("email:");
		
		emailText = new Text(container, SWT.BORDER);
		emailText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(container, SWT.NONE);
		
		lblSeedNode = new Label(container, SWT.NONE);
		lblSeedNode.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblSeedNode.setText("Seed node:");
		
		seedNodeText = new Text(container, SWT.BORDER);
		seedNodeText.setText(store.getString(PreferenceConstants.SEED_NODE));
		seedNodeText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(container, SWT.NONE);
		
		lblPort = new Label(container, SWT.NONE);
		lblPort.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblPort.setText("Port:");
		
		portText = new Text(container, SWT.BORDER);
		portText.setText(Integer.toString(store.getInt(PreferenceConstants.PORT)));
		portText.addVerifyListener(new VerifyListener() {
			public void verifyText(VerifyEvent e) {
				String currentText = ((Text)e.widget).getText();
		        String port =  currentText.substring(0, e.start) + e.text + currentText.substring(e.end);
		        try{  
		            int portNum = Integer.valueOf(port);  
		            if(portNum <0 || portNum > 65535){  
		                e.doit = false;  
		            }  
		        }  
		        catch(NumberFormatException ex){  
		            if(!port.equals(""))
		                e.doit = false;  
		        }  
			}
		});
		GridData gd_portText = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
		gd_portText.widthHint = 95;
		portText.setLayoutData(gd_portText);
		new Label(container, SWT.NONE);
		
		
		switch (lang){
		case JAVA:
			this.genElemetsComposit.setVisible(true);
			this.lblArtifactId.setVisible(true);
			this.artifactIdText.setVisible(true);
			this.lblPackage.setText("Package:");
			this.lblMainClass.setText("Main class:");
			break;
		case GO:
			this.genElemetsComposit.setVisible(false);
			this.lblArtifactId.setVisible(false);
			this.artifactIdText.setVisible(false);
			this.lblPackage.setText("Package:");
			this.lblMainClass.setText("Module:");
			break;
		default:
			break;

		}

	}

	/**
	 * Uses the standard container selection dialog to choose the new value for
	 * the container field.
	 */

	private void handleBrowse() {
		ContainerSelectionDialog dialog = new ContainerSelectionDialog(
				getShell(), ResourcesPlugin.getWorkspace().getRoot(), false,
				"Select new file container");
		if (dialog.open() == ContainerSelectionDialog.OK) {
			Object[] result = dialog.getResult();
			if (result.length == 1) {
				projectNameText.setText(((Path) result[0]).toString());
			}
		}
	}

	/**
	 * Ensures that both text fields are set.
	 */

	private void dialogChanged() {
//		IResource container = ResourcesPlugin.getWorkspace().getRoot()
//				.findMember(new Path(getContainerName()));
//		String fileName = getFileName();
//
//		if (getContainerName().length() == 0) {
//			updateStatus("File container must be specified");
//			return;
//		}
//		if (container == null
//				|| (container.getType() & (IResource.PROJECT | IResource.FOLDER)) == 0) {
//			updateStatus("File container must exist");
//			return;
//		}
//		if (!container.isAccessible()) {
//			updateStatus("Project must be writable");
//			return;
//		}
//		if (fileName.length() == 0) {
//			updateStatus("File name must be specified");
//			return;
//		}
//		if (fileName.replace('\\', '/').indexOf('/', 1) > 0) {
//			updateStatus("File name must be valid");
//			return;
//		}
//		int dotLoc = fileName.lastIndexOf('.');
//		if (dotLoc != -1) {
//			String ext = fileName.substring(dotLoc + 1);
//			if (ext.equalsIgnoreCase("mpe") == false) {
//				updateStatus("File extension must be \"mpe\"");
//				return;
//			}
//		}
		updateStatus(null);
	}

	private void updateStatus(String message) {
		setErrorMessage(message);
		setPageComplete(message == null);
	}

	public String getProjectName() {
		return projectNameText.getText();
	}

	public String getPackage(){
		return packageText.getText();
	}
	public String getMainClassName() {
		return mainClassText.getText();
	}
	public String getVersion() {
		return versionText.getText();
	}
	public String getArtifiactId() {
		return artifactIdText.getText();
	}
	public String getAuthor() {
		return authorText.getText();
	}
	public String getEmail() {
		return emailText.getText();
	}
	public String getSeedNode(){
		return seedNodeText.getText();
	}
	public int getPort(){
		return Integer.parseInt(portText.getText());
	}
	public String getPortString(){
		return portText.getText();
	}
	public boolean getGenerateJUnit(){
		return btnJunitCheckButton.getSelection();
	}
	public boolean getGenerateMain(){
		return btnMainCheckButton.getSelection();
	}

	public void setLanguage(Language lang) {
		this.lang = lang;
	}

}