package com.aerospike.aql.plugin.views;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.UIJob;

import com.aerospike.aql.grammar.IResultReporter;
import com.aerospike.client.Log.Level;
import com.aerospike.client.Record;
import com.aerospike.client.query.RecordSet;


public class ResultView extends ViewPart implements IResultReporter{

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "com.aerospike.aql.plugin.views.ResultView";

	private Action action1;
	private Action action2;
	private Action doubleClickAction;

	private IStructuredContentProvider content = new ViewContentProvider();

	private Text messageText;
	private Table recordTable;

	protected List<Record> recordsForDisplay = new ArrayList<Record>();

	private TableViewer recordTableViewer;

	class ViewContentProvider implements IStructuredContentProvider {
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}
		public void dispose() {
		}
		public Object[] getElements(Object parent) {
			//			if (ResultView.this.results != null && ResultView.this.results.getRecords() != null) {
			//				return ResultView.this.results.getRecords().toArray();
			//			}

			return new String[] { "No results" };
		}
	}

	class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {
		public String getColumnText(Object obj, int index) {
			if (obj instanceof Record){
				Record rec = (Record)obj;
				ArrayList<String> binNames = new ArrayList(rec.bins.keySet());
				Collections.sort(binNames);
				if (index == 0){
					return Integer.toString(rec.expiration);
				} if (index == 1){
					return Integer.toString(rec.generation);
				} else {
					return rec.getValue(binNames.get(index-2)).toString();
				}
			}
			return getText(obj);
		}
		public Image getColumnImage(Object obj, int index) {
			return getImage(obj);
		}
		public Image getImage(Object obj) {
			return null;
			//			return PlatformUI.getWorkbench().
			//					getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
		}
	}
	/**
	 * The constructor.
	 */
	public ResultView() {
	}

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {

		SashForm sashForm = new SashForm(parent, SWT.NONE);
		sashForm.setOrientation(SWT.VERTICAL);

		messageText = new Text(sashForm, SWT.BORDER | SWT.READ_ONLY | SWT.V_SCROLL | SWT.MULTI);
		messageText.setLayoutData(new GridData(GridData.FILL_BOTH));

		recordTableViewer = new TableViewer(sashForm, SWT.BORDER | SWT.FULL_SELECTION);
		recordTable = recordTableViewer.getTable();
		recordTable.setLinesVisible(true);
		recordTable.setHeaderVisible(true);
		recordTable.setBounds(0, 0, 18, 81);

		TableViewerColumn recordTTLColumn = new TableViewerColumn(recordTableViewer, SWT.NONE);
		TableColumn tblclmnTtl = recordTTLColumn.getColumn();
		tblclmnTtl.setWidth(100);
		tblclmnTtl.setText("TTL");

		TableViewerColumn tableViewerColumn_1 = new TableViewerColumn(recordTableViewer, SWT.NONE);
		TableColumn tblclmnGeneration = tableViewerColumn_1.getColumn();
		tblclmnGeneration.setWidth(100);
		tblclmnGeneration.setText("Generation");
		sashForm.setWeights(new int[] {3, 20});

		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				ResultView.this.fillContextMenu(manager);
			}
		});
		//		Menu menu = menuMgr.createContextMenu(tableViewBuilder.getTableViewer().getControl());
		//		tableViewBuilder.getTableViewer().getControl().setMenu(menu);
		//		getSite().registerContextMenu(menuMgr, tableViewBuilder.getTableViewer());
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(action1);
		manager.add(new Separator());
		manager.add(action2);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(action1);
		manager.add(action2);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(action1);
		manager.add(action2);
	}

	private void makeActions() {
		action1 = new Action() {
			public void run() {
				showMessage("Action 1 executed");
			}
		};
		action1.setText("Action 1");
		action1.setToolTipText("Action 1 tooltip");
		action1.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
				getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));

		action2 = new Action() {
			public void run() {
				showMessage("Action 2 executed");
			}
		};
		action2.setText("Action 2");
		action2.setToolTipText("Action 2 tooltip");
		action2.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
				getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
		doubleClickAction = new Action() {
			public void run() {
				ISelection selection = recordTableViewer.getSelection();
				Object obj = ((IStructuredSelection)selection).getFirstElement();
				showMessage("Double-click detected on "+obj.toString());
			}
		};
	}

	private void hookDoubleClickAction() {
		recordTableViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	}
	private void showMessage(String message) {
		//		MessageDialog.openInformation(
		//				tableViewBuilder.getTableViewer().getControl().getShell(),
		//				"Sample View",
		//				message);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		//		tableViewBuilder.getTableViewer().getControl().setFocus();
	}





	@Override
	public void report(final String message) {
		this.report(message, true);
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
	public void report(Level level, String message) {
		this.report(level, message, false);
	}

	@Override
	public void report(final String message, final boolean clear) {
		UIJob job = new UIJob("Report Message") { 
			public IStatus runInUIThread(IProgressMonitor arg0) {
				if (clear){
					ResultView.this.messageText.setText(message);
				} else {
					String newMessage = ResultView.this.messageText.getText() + "\n" + message;
					ResultView.this.messageText.setText(newMessage);
				}
				return Status.OK_STATUS;
			}
		};
		job.setUser(true);
		job.schedule();
	}

	@Override
	public void report(final Record record, final boolean clear) {
		UIJob job = new UIJob("Report Record") { 
			public IStatus runInUIThread(IProgressMonitor arg0) {
				Table table = recordTable;
				table.setRedraw( false ); // stop repaint the control till we are finished creating columns
				if (clear){
					recordsForDisplay.clear();
				}
				recordsForDisplay.add(record);
				// delete all the columns
				while ( table.getColumnCount() > 2 ) {
					table.getColumns()[ 2 ].dispose();
				}

				//Create columns for the bins in the 1st record
				ArrayList<String> binNames = new ArrayList(record.bins.keySet());
				Collections.sort(binNames);
				for (String binName : binNames){
					TableViewerColumn tableViewerColumn = new TableViewerColumn(recordTableViewer, SWT.NONE);
					TableColumn tblclmnGeneration = tableViewerColumn.getColumn();
					tblclmnGeneration.setWidth(50);
					tblclmnGeneration.setText("binName");

				}
				table.setRedraw( true );
				recordTableViewer.refresh();

				// Set content
				recordTableViewer.setContentProvider(content);
				recordTableViewer.setLabelProvider(new ViewLabelProvider());
				recordTableViewer.setInput(recordsForDisplay);
				return Status.OK_STATUS;
			}
		};
		job.setUser(true);
		job.schedule();
	}

	@Override
	public void report(final RecordSet message, final boolean clear) {
		UIJob job = new UIJob("Report Record Set") { 
			public IStatus runInUIThread(IProgressMonitor arg0) {
				if (clear){

				}
				//TODO
				return Status.OK_STATUS;
			}
		};
		job.setUser(true);
		job.schedule();
	}

	@Override
	public void report(final Level level, final String message, final boolean clear) {
		UIJob job = new UIJob("Report Message") { 
			public IStatus runInUIThread(IProgressMonitor arg0) {
				if (clear){
					ResultView.this.messageText.setText(message);
				} else {
					String newMessage = ResultView.this.messageText.getText() + "\n" + message;
					ResultView.this.messageText.setText(newMessage);
				}
				return Status.OK_STATUS;
			}
		};
		job.setUser(true);
		job.schedule();
	}

	@Override
	public void reportInfo(String inforMessage, String... seperators) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void reportInfo(String inforMessage, boolean clear,
			String... seperators) {
		// TODO Auto-generated method stub
		
	}
}