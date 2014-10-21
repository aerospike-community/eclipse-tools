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
package com.aerospike.core.navigator;


import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.navigator.IDescriptionProvider;

import com.aerospike.core.CoreActivator;
import com.aerospike.core.model.AsCluster;
import com.aerospike.core.model.AsNameSpace;
import com.aerospike.core.model.AsNode;
import com.aerospike.core.model.AsSet;
import com.aerospike.core.model.Function;
import com.aerospike.core.model.NameValuePair;
import com.aerospike.core.model.NodeFolder;
import com.aerospike.core.model.NsFolder;
import com.aerospike.core.model.ModuleFolder;
import com.aerospike.core.model.Module;

public class ClusterLabelProvider extends LabelProvider implements ILabelProvider, IDescriptionProvider{

	@Override
	public Image getImage(Object element) {
		if (element instanceof AsCluster){
			return CoreActivator.getImage("icons/small/cluster.png");
		} else if (element instanceof NsFolder){
			return CoreActivator.getImage("icons/small/namespace.png");
		} else if (element instanceof AsNameSpace){
			return CoreActivator.getImage("icons/small/namespace.png");
		} else if (element instanceof NodeFolder){
			return CoreActivator.getImage("icons/small/node.png");
		} else if (element instanceof AsNode){
			return CoreActivator.getImage("icons/small/node.png");
		} else if (element instanceof ModuleFolder){
			return CoreActivator.getImage("icons/small/UDF.png");
		} else if (element instanceof Module){
			return CoreActivator.getImage("icons/small/UDF.png");
		} else if (element instanceof Function){
			return CoreActivator.getImage("icons/small/function.png");
		} else if (element instanceof NameValuePair){
			return CoreActivator.getImage("icons/small/statistics.png");
		} else if (element instanceof AsSet){
			return CoreActivator.getImage("icons/small/set.png");
		} else {
		return null;
		}
	}

	@Override
	public String getText(Object element) {
		if (element instanceof NameValuePair){
			NameValuePair nvp = (NameValuePair)element;
			return nvp.getName() + " = " + nvp.getValue();
		}
		return element.toString();
	}

	@Override
	public String getDescription(Object element) {
		return element.toString();
	}

	
}
