package com.aerospike.core.views;

import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

import com.aerospike.aql.grammar.IErrorReporter;
import com.aerospike.aql.grammar.IResultReporter;
import com.aerospike.client.AerospikeException;
import com.aerospike.client.Log;
import com.aerospike.client.Log.Level;
import com.aerospike.client.Record;
import com.aerospike.client.query.RecordSet;
import com.aerospike.core.CoreActivator;

public class ResultsConsoleView implements Log.Callback, IResultReporter, IErrorReporter {
	MessageConsole console;
	MessageConsoleStream out;

	public ResultsConsoleView() {
		console = CoreActivator.findAerospikeConsole();
		out = console.newMessageStream();
		
	}

	public void report(String message) {
		out.println(message);
		
	}
	public void report(Level level, String message) {
		switch (level){
		case DEBUG:
			report("DEBUG: " + message);
			break;
		case ERROR:
			report("ERROR: " + message);
			break;
		case WARN:
			report("WARN: " + message);
			break;
		case INFO:
			report("INFO: " + message);
			break;
		}
		
	}

	public void report(Record record) {
		out.println(record.toString());
	}

	public void report(RecordSet recordSet) {
		try {
			int count = 0;
			while (recordSet.next()) {
				//Key key = recordSet.getKey();
				Record record = recordSet.getRecord();
				report(record);
				count++;
			}
		} catch (AerospikeException e) {
			e.printStackTrace();
		} finally {
			if (recordSet != null) {
				recordSet.close();
			}
		}

	}

	public void clear(){
		console.clearConsole();
	}


	@Override
	public void log(Level level, String message) {
		report(level, message);
		
	}

	public MessageConsole getConsole() {
		return console;
	}

	@Override
	public void report(String message, boolean arg1) {
		this.report(message);
		
	}

	@Override
	public void report(Record record, boolean arg1) {
		this.report(record);
		
	}

	@Override
	public void report(RecordSet arg0, boolean arg1) {
		this.report(arg0, arg1);
		
	}

	@Override
	public void report(Level arg0, String arg1, boolean arg2) {
		this.report(arg0, arg1, arg2);
		
	}

	@Override
	public void reportError(int line, int offset, int length, String message) {
		this.report(message);
	}

	@Override
	public void reportInfo(String inforMessage, String... seperators) {
		reportInfo(inforMessage, false, seperators);		
	}

	@Override
	public void reportInfo(String inforMessage, boolean clear,
			String... seperators) {
		if (inforMessage == null || inforMessage.isEmpty() || seperators == null )
			return;
		if (clear)
			this.clear();
		if (seperators.length >= 1){
			String[] outerParts = inforMessage.split(seperators[0]);
			String rowFormat = null;
			for (int i = 0; i < outerParts.length; i++){
				if (seperators.length >= 2){
					String[] innerParts = outerParts[i].split(seperators[1]);
					if (i == 0){
						StringBuffer sb = new StringBuffer("| ");
						for (int j = 0; j < innerParts.length; j++){
							sb.append("%").append(innerParts[j].length()).append("s | ");
						}
						rowFormat = sb.toString();
						this.report(String.format(rowFormat, nameValueParts(innerParts, true, seperators[2])));
					}
					this.report(String.format(rowFormat, nameValueParts(innerParts, false, seperators[2])));
				}
			}
		}
	}

	private String[] nameValueParts(String[] parts, boolean headerRow, String seperator){
		String[] nvs = new String[parts.length];
		for (int i = 0; i < parts.length; i++) {
			String[] nv = parts[i].split(seperator);
			if (headerRow){
				nvs[i] = nv[0];
			} else if (nv.length > 1){
				nvs[i] = nv[1];
			}
		}
		return nvs;
	}
	
}
