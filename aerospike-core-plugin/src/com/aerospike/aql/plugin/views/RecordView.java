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
package com.aerospike.aql.plugin.views;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.ViewPart;

import com.aerospike.aql.IResultReporter;
import com.aerospike.client.AerospikeException;
import com.aerospike.client.Key;
import com.aerospike.client.Log.Level;
import com.aerospike.client.Record;
import com.aerospike.client.query.KeyRecord;
import com.aerospike.client.query.RecordSet;
import com.aerospike.client.query.ResultSet;

public class RecordView extends ViewPart implements IResultReporter{

	public static final String ID = "com.aerospike.aql.plugin.views.RevordView"; //$NON-NLS-1$
	private final FormToolkit toolkit = new FormToolkit(Display.getCurrent());
	private Table recordTable;
	private Table table;
	private List<KeyRecord> content = new ArrayList<KeyRecord>();
	private RecordSet recordSet;
	private ResultSet resultSet;
	private TableViewer tableViewer;

	public RecordView() {
	}

	/**
	 * Create contents of the view part.
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {
		Composite container = toolkit.createComposite(parent, SWT.NONE);
		toolkit.paintBordersFor(container);
		TableColumnLayout tcl_container = new TableColumnLayout();
		container.setLayout(tcl_container);
		{
			tableViewer = new TableViewer(container, SWT.BORDER | SWT.FULL_SELECTION);
			table = tableViewer.getTable();
			table.setLinesVisible(true);
			table.setHeaderVisible(true);
			toolkit.paintBordersFor(table);
			{
				TableViewerColumn tableDigestColumn = new TableViewerColumn(tableViewer, SWT.NONE);
				TableColumn tblclmnDigest = tableDigestColumn.getColumn();
				tcl_container.setColumnData(tblclmnDigest, new ColumnWeightData(1, ColumnWeightData.MINIMUM_WIDTH, true));
				tblclmnDigest.setText("Digest");
			}
			{
				TableViewerColumn tableGenerationColumn = new TableViewerColumn(tableViewer, SWT.NONE);
				TableColumn tblclmnGeneration = tableGenerationColumn.getColumn();
				//tcl_container.setColumnData(tblclmnGeneration, new ColumnPixelData(150, true, true));
				tcl_container.setColumnData(tblclmnGeneration, new ColumnWeightData(1, ColumnWeightData.MINIMUM_WIDTH, true));
				tblclmnGeneration.setText("Generation");
			}
			{
				TableViewerColumn tableExpiraryColumn = new TableViewerColumn(tableViewer, SWT.NONE);
				TableColumn tblclmnExpirary = tableExpiraryColumn.getColumn();
				//tcl_container.setColumnData(tblclmnExpirary, new ColumnPixelData(150, true, true));
				tcl_container.setColumnData(tblclmnExpirary, new ColumnWeightData(1, ColumnWeightData.MINIMUM_WIDTH, true));
				tblclmnExpirary.setText("Expirary");
			}
			tableViewer.setLabelProvider(new RecordLabelProvider());
			tableViewer.setContentProvider(new RecordContentProvider());
			
			tableViewer.setInput(this.content);
		}

		createActions();
		initializeToolBar();
		initializeMenu();
	}
	

	public void dispose() {
		toolkit.dispose();
		super.dispose();
	}

	/**
	 * Create the actions.
	 */
	private void createActions() {
		// Create the actions
	}

	/**
	 * Initialize the toolbar.
	 */
	private void initializeToolBar() {
		IToolBarManager tbm = getViewSite().getActionBars().getToolBarManager();
	}

	/**
	 * Initialize the menu.
	 */
	private void initializeMenu() {
		IMenuManager manager = getViewSite().getActionBars().getMenuManager();
	}

	@Override
	public void setFocus() {
		// Set the focus
	}

	@Override
	public void scanCallback(Key arg0, Record arg1) throws AerospikeException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void cancel() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ViewFormat getViewFormat() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isCancelled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void report(String message) {
		this.report(message, false);
		
	}

	@Override
	public void report(Record record) {
		this.report(record, false);
		
	}

	@Override
	public void report(RecordSet recordSet) {
		this.report(recordSet, false);
		
	}

	@Override
	public void report(ResultSet resultSet) {
		this.report(resultSet, false);
		
	}

	@Override
	public void report(boolean clear, String message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void report(Level arg0, String message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void report(Key key, Record record) {
		//content.add(new KeyRecord(key, record));
		this.tableViewer.add(new KeyRecord(key, record));
		this.tableViewer.refresh();
	}

	@Override
	public void report(String message, boolean clear) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void report(Record record, boolean clear) {
		if (clear) this.content.clear();
		
	}

	@Override
	public void report(RecordSet recordSet, boolean clear) {
		if (clear) this.content.clear();
		this.recordSet = recordSet;
		
		//TODO find the best way to iterate through the record set
		
		this.tableViewer.refresh();
	}

	@Override
	public void report(ResultSet resultSet, boolean clear) {
		if (clear) this.content.clear();
		this.resultSet = resultSet;
		
	}

	@Override
	public void report(Level arg0, String arg1, boolean arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void report(Key arg0, Record arg1, boolean arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void reportInfo(Map<String, Object>[] arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void reportInfo(Map<String, Object> arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void reportInfo(String arg0, String... arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void reportInfo(String[] arg0, String... arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void reportInfo(String arg0, boolean arg1, String... arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void reportInfo(String[] arg0, boolean arg1, String... arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setViewFormat(ViewFormat arg0) {
		// TODO Auto-generated method stub
		
	}
}
