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
		//TODO 
	}

	
}
