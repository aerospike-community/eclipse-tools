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
import com.aerospike.core.model.PackageFolder;
import com.aerospike.core.model.Package;

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
		} else if (element instanceof PackageFolder){
			return CoreActivator.getImage("icons/small/UDF.png");
		} else if (element instanceof Package){
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
