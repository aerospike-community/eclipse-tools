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

import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

import org.eclipse.core.databinding.SetBinding;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnPixelData;
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
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.wb.swt.ResourceManager;

import swing2swt.layout.BorderLayout;

import com.aerospike.aql.IResultReporter;
import com.aerospike.aql.grammar.IErrorReporter;
import com.aerospike.client.AerospikeException;
import com.aerospike.client.Key;
import com.aerospike.client.Log.Level;
import com.aerospike.client.Record;
import com.aerospike.client.query.KeyRecord;
import com.aerospike.client.query.RecordSet;
import com.aerospike.client.query.ResultSet;
import com.aerospike.core.CoreActivator;
import com.aerospike.core.views.ResultsConsoleView;

public class RecordView extends ViewPart implements IResultReporter, IErrorReporter{

	public static final String ID = "com.aerospike.aql.plugin.views.RevordView"; //$NON-NLS-1$
	protected static final int TABLE_REFRESH_PERIOD = 1000;
	protected static final int TABLE_BUFFER_SIZE = 100;
	protected final FormToolkit toolkit = new FormToolkit(Display.getCurrent());
	protected Table table;
	protected List<KeyRecord> recordContent = new ArrayList<KeyRecord>();
	protected final ArrayBlockingQueue<KeyRecord> recordBuffer;
	protected RecordSet recordSet;
	protected ResultSet resultSet;
	protected TableViewer tableViewer;
	protected TableColumnLayout tcl_container;
	protected Map<String, TableColumn> columnMap;
	protected Action textMessage;
	private boolean cancelled;
	protected RecordLabelProvider labelProvider = new RecordLabelProvider(this);
	private ResultsConsoleView consoleView;
	protected UIRefresh uiRefresh;
	protected Object lock = new Object();

	public RecordView() {
		setTitleImage(ResourceManager.getPluginImage("aerospike-core-plugin", "icons/aerospike.logo.png"));
		consoleView = new ResultsConsoleView();
		// find the Aerospike console and display it
		IWorkbench wb = PlatformUI.getWorkbench();
		IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
		IWorkbenchPage page = win.getActivePage();
		IConsoleView view;
		try {
			view = (IConsoleView) page.showView(IConsoleConstants.ID_CONSOLE_VIEW);
			view.display(consoleView.getConsole());
		} catch (PartInitException e) {
			CoreActivator.showError(e, e.getMessage());
		}
		this.recordBuffer = new ArrayBlockingQueue<KeyRecord>(TABLE_BUFFER_SIZE);
		uiRefresh = new UIRefresh(); 
		uiRefresh.schedule(TABLE_REFRESH_PERIOD);

	}



	/**
	 * Create contents of the view part.
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new BorderLayout(0, 0));
		Composite container = toolkit.createComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
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
			{
				TableViewerColumn tableViewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
				TableColumn tblclmn = tableViewerColumn.getColumn();
				tcl_container.setColumnData(tblclmn, new ColumnPixelData(150, true, true));
				tblclmn.setText("Namespace");
				tblclmn.pack(); 
			}

			{
				TableViewerColumn tableViewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
				TableColumn tblclmn = tableViewerColumn.getColumn();
				tcl_container.setColumnData(tblclmn, new ColumnPixelData(60, true, true));
				tblclmn.setText("Set");
			}

			{
				TableViewerColumn tableViewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
				TableColumn tblclmn = tableViewerColumn.getColumn();
				tcl_container.setColumnData(tblclmn, new ColumnPixelData(100, true, true));
				tblclmn.setText("Digest");
			}

			{
				TableViewerColumn tableViewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
				TableColumn tblclmn = tableViewerColumn.getColumn();
				tcl_container.setColumnData(tblclmn, new ColumnPixelData(100, true, true));
				tblclmn.setText("Generation");
				tblclmn.pack(); 
			}

			{
				TableViewerColumn tableViewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
				TableColumn tblclmn = tableViewerColumn.getColumn();
				tcl_container.setColumnData(tblclmn, new ColumnPixelData(100, true, true));
				tblclmn.setText("Expirary");
			}

			tableViewer.setLabelProvider(new RecordLabelProvider(this));
			tableViewer.setContentProvider(new RecordContentProvider());

			tableViewer.setInput(this.recordContent);
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
		textMessage = new Action("Clear") {
			public void run() {
				recordContent.clear();
				recordBuffer.clear();
				tableViewer.refresh();
			}
		};
		textMessage.setImageDescriptor(ResourceManager.getPluginImageDescriptor("aerospike-core-plugin", "icons/clear.png"));
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
		tbm.add(textMessage);
	}

	/**
	 * Initialize the menu.
	 */
	private void initializeMenu() {
		IMenuManager manager = getViewSite().getActionBars().getMenuManager();
		manager.add(textMessage);
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
		reportText(message, clear);
	}

	@Override
	public void report(Level arg0, String message) {
		reportText(message, false);
	}

	protected void reportText(final String message, final boolean clear){
		consoleView.report(message, clear);
	}

	@Override
	public void report(final Key key, final Record record) {
		addRecord(new KeyRecord(key, record));
	}

	@Override
	public void report(String message, boolean clear) {
		reportText(message, clear);
	}

	@Override
	public void report(Record record, boolean clear) {
		this.report((Key)null, record, clear);

	}

	@Override
	public void report(final RecordSet recordSet, final boolean clear) {
		this.recordSet = recordSet;
		Iterator<KeyRecord> it = recordSet.iterator();
		while (it.hasNext()){
			addRecord(it.next());
		}
		recordSet.close();
	}

	protected void addRecord(KeyRecord keyRecord){
			recordBuffer.offer(keyRecord);
	}

	protected void addColumnsForRecord(KeyRecord keyRecord){
		if (keyRecord != null && keyRecord.record != null){
			for (Map.Entry<String, Object> entry : keyRecord.record.bins.entrySet()) {
				String binName = entry.getKey();
				addColumn(binName, 10);
			}
		}
	}

	@Override
	public void report(ResultSet resultSet, boolean clear) {
		this.uiRefresh.setClear(clear);
		this.resultSet = resultSet;

	}

	@Override
	public void report(Level arg0, String format, boolean arg) {
		reportText(String.format(format, arg), false);

	}

	@Override
	public void report(Key key, Record record, boolean clear) {
		this.uiRefresh.setClear(clear);
		report(key, record);
	}

	@Override
	public void reportInfo(Map<String, Object>[] infoMap) {
		consoleView.reportInfo(infoMap);
	}

	@Override
	public void reportInfo(Map<String, Object> infoMap) {
		consoleView.reportInfo(infoMap);

	}

	@Override
	public void reportInfo(String format, String... args) {
		consoleView.reportInfo(format, args);

	}

	@Override
	public void reportInfo(String[] formats, String... args) {
		consoleView.reportInfo(formats, args);

	}

	@Override
	public void reportInfo(String format, boolean clear, String... args) {
		consoleView.reportInfo(format, clear, args);

	}

	@Override
	public void reportInfo(String[] formats, boolean clear, String... args) {
		consoleView.reportInfo(formats, clear, args);

	}

	@Override
	public void setViewFormat(ViewFormat viewFormat) {
		consoleView.setViewFormat(viewFormat);

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
			TableColumn col = this.view.table.getColumn(columnIndex);
			String result = "";
			switch(columnIndex){
			case 0:
				if (kr.key != null)
					result = kr.key.namespace;
				else
					result = "";
				if (col != null)
					col.pack();
				break;
			case 1:
				if (kr.key != null)
					result = kr.key.setName;
				else
					result = "";
				if (col != null)
					col.pack();
				break;
			case 2:
				if (kr.key != null)
					result = byteToHex(kr.key.digest);
				else
					result = "";
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
					if (col != null)
						col.pack();
					break;
				}
				break;
			default:
				if (col != null){
					String name = col.getText();
					Object value = kr.record.getValue(name);
					if (value != null)
						result =  value.toString();
					col.pack();
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

	protected class UIRefresh extends UIJob {
		
		private boolean clear;

		public UIRefresh() {
			super("Record view refresh");
			super.setPriority(DECORATE);
		}
		
		
		public boolean isClear() {
			return clear;
		}


		public void setClear(boolean clear) {
			if (!this.clear)
				this.clear = clear;
		}


		@Override
		public IStatus runInUIThread(IProgressMonitor progress) {
			if (!recordBuffer.isEmpty()){
					if (tableViewer != null && tableViewer.getControl() != null && !tableViewer.getControl().isDisposed()) {
						if (clear){
							recordContent.clear();
							int count = table.getColumnCount();
							if (count > 5){
								for (int x = 5; x < count; x++){
									table.remove(x);
								}
								columnMap.clear();
							}
						}
						
						for (;!recordBuffer.isEmpty();){
							KeyRecord keyRec;
							try {
								keyRec = recordBuffer.take();
								addColumnsForRecord(keyRec);
								recordContent.add(keyRec);
							} catch (InterruptedException e) {
								CoreActivator.showError(e, "Error refreshing record view");
							}
						}
						tableViewer.refresh();
					}
				}
			schedule(TABLE_REFRESH_PERIOD);
			return Status.OK_STATUS;
		}
	}

	/*
	 * IErrorReporter
	 */
	@Override
	public void reportError(int line, int charStart, int charEnd, String message){
		consoleView.reportError(line, charStart, charEnd, message);
	}
	@Override
	public void reportError(int line, String message){
		consoleView.reportError(line, message);
	}
	@Override
	public void reportError(int line, AerospikeException e){
		consoleView.reportError(line, e);
	}
	@Override
	public int getErrorCount(){
		return consoleView.getErrorCount();
	}
	@Override
	public List<String> getErrorList(){
		return getErrorList();
	}

}
