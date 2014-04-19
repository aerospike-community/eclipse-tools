package com.aerospike.core.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.widgets.Shell;

import com.aerospike.client.AerospikeClient;
import com.aerospike.client.AerospikeException;
import com.aerospike.core.CoreActivator;

public class Utils {
	public static Map<String, String> toMap(String source){
		HashMap<String, String> responses = new HashMap<String, String>();
		String values[] = source.split(";");

		for (String value : values) {
			String nv[] = value.split("=");

			if (nv.length >= 2) {
				responses.put(nv[0], nv[1]);
			}
			else if (nv.length == 1) {
				responses.put(nv[0], null);
			}
		}

		return responses.size() != 0 ? responses : null;

	}
	
	public static List<NameValuePair> toNameValuePair(Object parent, Map<String, String> map){
		List<NameValuePair> list = new ArrayList<NameValuePair>();
		for (String key : map.keySet()){
			NameValuePair nvp = new NameValuePair(parent, key, map.get(key));
			list.add(nvp);
		}
		return list;
	}

	public static AerospikeClient getClient(IAsEntity entity) {
		AerospikeClient client = null;
		IAsEntity topEntity = entity;
		while (!(topEntity instanceof AsCluster))
			topEntity = (IAsEntity) topEntity.getParent();
		client = CoreActivator.getClient( ((AsCluster)topEntity).getProject());
		return client;
	}

	public static AsCluster getCluster(IAsEntity selectedEntity) {
		IAsEntity topEntity = selectedEntity;
		while (!(topEntity instanceof AsCluster))
			topEntity = (IAsEntity) topEntity.getParent();

		return (AsCluster) topEntity;
	}
	
	
}
