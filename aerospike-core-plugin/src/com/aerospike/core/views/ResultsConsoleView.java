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
package com.aerospike.core.views;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

import com.aerospike.aql.grammar.IErrorReporter;
import com.aerospike.aql.IResultReporter;
import com.aerospike.client.AerospikeException;
import com.aerospike.client.Key;
import com.aerospike.client.Log;
import com.aerospike.client.Log.Level;
import com.aerospike.client.Record;
import com.aerospike.client.query.RecordSet;
import com.aerospike.client.query.ResultSet;
import com.aerospike.core.CoreActivator;

public class ResultsConsoleView implements Log.Callback, IResultReporter, IErrorReporter {
	boolean cancelled = false;
	MessageConsole console;
	MessageConsoleStream out;
	int errors = 0;
	List<String> errorList = null;

	public ResultsConsoleView() {
		console = CoreActivator.findAerospikeConsole();
		out = console.newMessageStream();
		errorList = new ArrayList<String>();
		
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
				if (this.cancelled) break;
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
	public void report(String message, boolean clear) {
		if (clear) clear();
		this.report(message);
		
	}

	@Override
	public void report(Record record, boolean clear) {
		if (clear) clear();
		this.report(record);
		
	}

	@Override
	public void report(RecordSet recordSet, boolean clear) {
		if (clear) clear();
		this.report(recordSet);
		
	}

	@Override
	public void report(Level arg0, String arg1, boolean arg2) {
		this.report(arg0, arg1, arg2);
		
	}

	@Override
	public void reportError(int line, int offset, int length, String message) {
		this.report(String.format("Error on Line: %d at %d, %s", line, offset, message));
		this.errors++;
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

	@Override
	public void cancel() {
		this.cancelled = true;
	}

	@Override
	public boolean isCancelled() {
		return this.cancelled;
	}


	@Override
	public void report(ResultSet resultSet) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void report(Key key, Record record) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void report(ResultSet resultSet, boolean clear) {
		if (clear) clear();
		report(resultSet);
		
	}

	@Override
	public void report(Key key, Record record, boolean clear) {
		if (clear) clear();
		report(key, record);
		
	}


	@Override
	public void reportInfo(String[] infoStrings, String... seperators) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void reportInfo(String[] infoStrings, boolean clear, String... seperators) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setViewFormat(ViewFormat viewFormat) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void scanCallback(Key arg0, Record arg1) throws AerospikeException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getErrorCount() {
		return this.errors;
	}

	@Override
	public List<String> getErrorList() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void reportError(int line, String message) {
		this.report(String.format("Error on Line: %d, %s", line, message));
		this.errors++;
		
	}

	@Override
	public void reportError(int line, AerospikeException e) {
		this.report(String.format("Error on Line: %d, %s", line, e.getMessage()));
		this.errors++;
		
	}

	@Override
	public ViewFormat getViewFormat() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void report(boolean clear, String message) {
		if (clear) clear();
		report(message);
		
	}

	@Override
	public void reportInfo(Map<String, Object>[] infoMaps) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void reportInfo(Map<String, Object> infoMap) {
		// TODO Auto-generated method stub
		
	}
	
}
