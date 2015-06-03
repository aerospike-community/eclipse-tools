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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.UIJob;

import swing2swt.layout.BorderLayout;

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
	protected final FormToolkit toolkit = new FormToolkit(Display.getCurrent());
	protected Table table;
	protected List<KeyRecord> content = new ArrayList<KeyRecord>();
	protected RecordSet recordSet;
	protected ResultSet resultSet;
	protected TableViewer tableViewer;
	protected TableColumnLayout tcl_container;
	protected Map<String, TableColumn> columnMap;
	private boolean cancelled;
	protected RecordLabelProvider labelProvider = new RecordLabelProvider(this);

	public RecordView() {
	}

	/**
	 * Create contents of the view part.
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new BorderLayout(0, 0));
		Composite container = toolkit.createComposite(parent, SWT.NONE);
		toolkit.paintBordersFor(container);
		tcl_container = new TableColumnLayout();
		columnMap = new HashMap<String, TableColumn>();
		container.setLayout(tcl_container);
		{
			tableViewer = new TableViewer(container, SWT.BORDER | SWT.FULL_SELECTION);
			table = tableViewer.getTable();
			table.setLinesVisible(true);
			table.setHeaderVisible(true);
			toolkit.paintBordersFor(table);

			addColumn("Name space", 20);
			addColumn("Set", 20);
			addColumn("Digest", 40);
			addColumn("Generation", 10);
			addColumn("Expirary", 50);

			tableViewer.setLabelProvider(new RecordLabelProvider(this));
			tableViewer.setContentProvider(new RecordContentProvider());

			tableViewer.setInput(this.content);
		}

		createActions();
		initializeToolBar();
		initializeMenu();
	}


	public void dispose() {
		columnMap.clear();
		toolkit.dispose();
		super.dispose();
	}

	protected void addColumn(String name, int minimumWidth){
		if (columnMap.get(name) == null){
			TableViewerColumn tableColumn = new TableViewerColumn(tableViewer, SWT.NONE);
			TableColumn tblclmn = tableColumn.getColumn();
			
			tcl_container.setColumnData(tblclmn, new ColumnWeightData(0, minimumWidth, true));
			tblclmn.setText(name);
			columnMap.put(name, tblclmn);
			tblclmn.pack();  
			tableViewer.setLabelProvider(this.labelProvider);
		}
	}

	/**
	 * Create the actions.
	 */
	private void createActions() {
		// Create the actions
	}



	protected TableColumn findColumnByName(String name){
		TableColumn column = this.columnMap.get(name);
		return column;
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
		table.setFocus();
	}

	@Override
	public void scanCallback(final Key key, final Record record) throws AerospikeException {
		report(key, record);
	}

	@Override
	public void cancel() {
		close();
		this.cancelled = true;
	}

	@Override
	public void close() {
		if (this.recordSet != null) 
			this.recordSet.close();
		if (this.resultSet != null) 
			this.resultSet.close();
	}

	@Override
	public ViewFormat getViewFormat() {
		return ViewFormat.TABLE;
	}

	@Override
	public boolean isCancelled() {
		return this.cancelled;
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
		this.report(resultSet, true);

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
	public void report(final Key key, final Record record) {
		UIJob job = new UIJob("Refresh record views") { 

			public IStatus runInUIThread(IProgressMonitor arg0) {
				if (tableViewer != null && tableViewer.getControl() != null && !tableViewer.getControl().isDisposed()) {
					addRecord(new KeyRecord(key, record));
					tableViewer.refresh();
				}
				return Status.OK_STATUS;
			}

		};
		job.setUser(true);
		job.schedule();
	}

	@Override
	public void report(String message, boolean clear) {
		// TODO Auto-generated method stub

	}

	@Override
	public void report(Record record, boolean clear) {
		if (clear) clearDisplay();

	}

	@Override
	public void report(final RecordSet recordSet, final boolean clear) {
		this.recordSet = recordSet;
		if (clear) clearDisplay();
		// refresh the view
		UIJob job = new UIJob("Refresh record views") { 

			public IStatus runInUIThread(IProgressMonitor arg0) {
				if (tableViewer != null && tableViewer.getControl() != null && !tableViewer.getControl().isDisposed()) {

					Iterator<KeyRecord> it = recordSet.iterator();
					while (it.hasNext()){
						addRecord(it.next());
					}
					tableViewer.refresh();
					recordSet.close();
				}
				return Status.OK_STATUS;
			}

		};
		job.setUser(true);
		job.schedule();

	}

	protected void addRecord(KeyRecord keyRecord){
		for (Map.Entry<String, Object> entry : keyRecord.record.bins.entrySet()) {
			String binName = entry.getKey();
			addColumn(binName, 10);
		}
		content.add(keyRecord);
	}

	protected void clearDisplay(){
		UIJob job = new UIJob("clear record views") { 

			public IStatus runInUIThread(IProgressMonitor arg0) {
				content.clear();
				int count = table.getColumnCount();
				if (count > 5){
					for (int x = 5; x < count; x++){
						table.remove(x);
					}
					columnMap.clear();
				}
				return Status.OK_STATUS;
			}
		};
		job.setUser(true);
		job.schedule();
	}

	@Override
	public void report(ResultSet resultSet, boolean clear) {
		if (clear) clearDisplay();
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


	public class RecordContentProvider implements IStructuredContentProvider {

		@Override
		public void dispose() {

		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

		}

		@Override
		public Object[] getElements(Object inputElement) {
			if (inputElement instanceof List){
				List<?> list = ((List)inputElement);
				if (list.size()==0){
					return new Object[0];
				} else {
					KeyRecord[] recordArray = new KeyRecord[list.size()];
					int i = 0;
					for (Object element : list){
						KeyRecord keyRecord = (KeyRecord) element;
						recordArray[i] = keyRecord;
						i++;
					}
					return recordArray;
				}
			}
			return null;
		}

	}


	public class RecordLabelProvider extends LabelProvider implements
	ITableLabelProvider {
		final SimpleDateFormat dateFormat =  new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss");
		final long AS_EPOCH = 1262304000000L; //TODO this is still wrong

		RecordView view;

		public RecordLabelProvider(RecordView view){
			this.view = view;
		}

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			KeyRecord kr = (KeyRecord) element;
			String result = "";
			switch(columnIndex){
			case 0:
				result = kr.key.namespace;
				break;
			case 1:
				result = kr.key.setName;
				break;
			case 2:
				result = byteToHex(kr.key.digest);
				break;
			case 3:
				result = Integer.toString(kr.record.generation);
				break;
			case 4:
				switch (kr.record.expiration){
				case -1: //Forever
					result = "forever";
					break;
				case 0:
					result = "default";
					break;
				default:
					long ts = kr.record.expiration + AS_EPOCH;
					Date date = new Date(ts);
					result = dateFormat.format(date);
					break;
				}
				break;
			default:
				TableColumn col = this.view.table.getColumn(columnIndex);
				if (col != null){
					String name = col.getText();
					Object value = kr.record.getValue(name);
					if (value != null)
						result =  value.toString();
				} 
			}
			return result;
		}
		String byteToHex(final byte[] hash)
		{
			Formatter formatter = new Formatter();
			for (byte b : hash)
			{
				formatter.format("%02x", b);
			}
			String result = formatter.toString().toUpperCase();
			formatter.close();
			return result;
		}

	}
}
