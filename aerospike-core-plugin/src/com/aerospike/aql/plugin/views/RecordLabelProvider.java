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

import java.util.Date;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import com.aerospike.client.query.KeyRecord;

public class RecordLabelProvider extends LabelProvider implements
		ITableLabelProvider {

	static final int AS_EPOCH = 1262304000;
	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		KeyRecord kr= (KeyRecord) element;
		String result = "";
		switch(columnIndex){
		case 0:
			result = kr.key.digest.toString();
			break;
		case 1:
			result = Integer.toString(kr.record.generation);
			break;
		case 2:
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
				result = date.toString();
				break;
			}
			break;
		default:
			//should not reach here
			result = "";
		}
		return result;
	}

}
