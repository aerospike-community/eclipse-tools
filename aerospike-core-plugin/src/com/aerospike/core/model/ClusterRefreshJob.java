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
package com.aerospike.core.model;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.progress.UIJob;

import com.aerospike.client.AerospikeClient;
import com.aerospike.client.AerospikeException;
import com.aerospike.client.Info;
import com.aerospike.client.cluster.Node;
import com.aerospike.client.policy.InfoPolicy;
import com.aerospike.core.CoreActivator;

public class ClusterRefreshJob extends Job{
	AsCluster cluster;
	IViewPart viewPart;
	private Viewer viewer;
	
	public ClusterRefreshJob(IProject project){
		this(CoreActivator.getCluster(project));
	}
	public ClusterRefreshJob(AsCluster cluster) {
		super(cluster.getProject().getName() + " cluster refresh");
		this.cluster = cluster;
		this.viewer = cluster.getViewer();
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		monitor.beginTask("Refreshing Aerospike cluster information", 30);

		String seedNode = cluster.getSeedHost();
		int port = cluster.getPort();
		InfoPolicy infoPolicy = CoreActivator.getInfoPolicy(cluster.getProject());
		try {
			TimeUnit.SECONDS.sleep(1);
			// refresh nodes
			if (monitor.isCanceled())
				return Status.CANCEL_STATUS;
			monitor.subTask("Fetching nodes");
			AerospikeClient client = CoreActivator.getClient(cluster.getProject());
			if (client == null)
				return Status.CANCEL_STATUS;
			Node[] nodes = client.getNodes();
			for (Node node : nodes){
				AsNode newNode = this.cluster.addNode(node);
				HashMap<String, String> info = (HashMap<String, String>) Info.request(infoPolicy, node);
				newNode.setDetails(info);
				if (monitor.isCanceled())
					return Status.CANCEL_STATUS;
			}
			monitor.worked(10);
			
			monitor.subTask("Fetching name spaces");
			this.cluster.namespaces.clearSetData();
			// refresh Namespace list for each node
			for (Object kid : this.cluster.nodes.getChildren()){
				if (monitor.isCanceled())
					return Status.CANCEL_STATUS;
				if (kid instanceof AsNode){
					AsNode node = (AsNode)kid;
					String nodeAddress = node.getAddress();
					int nodePort = node.getPort();
					String namespacesString = Info.request(nodeAddress, nodePort, "namespaces");
					if (!namespacesString.isEmpty()){
						String[] nameSpaces = namespacesString.split(";");
						for (String nameSpace : nameSpaces){
							if (monitor.isCanceled())
								return Status.CANCEL_STATUS;
							AsNameSpace nodeNamespace = node.fetchNameSpace(nameSpace);
							AsNameSpace clusterNameSpace = this.cluster.namespaces.fetchNameSpace(nameSpace);
							String setsString = Info.request(nodeAddress, nodePort, "sets/"+nameSpace);
							if (!setsString.isEmpty()){
								String[] sets = setsString.split(";");
								for (String setData : sets) {
									if (monitor.isCanceled())
										return Status.CANCEL_STATUS;
									nodeNamespace.addSet(setData);
									clusterNameSpace.mergeSet(setData);
								}
							}
						}
					}
				}
			}
			monitor.worked(10);

			// refresh packages;
			if (monitor.isCanceled())
				return Status.CANCEL_STATUS;
			monitor.subTask("Fetching packages");
			String packagesString = Info.request(seedNode, port, "udf-list");
			if (!packagesString.isEmpty()){
				this.cluster.packages.clear();
				String[] packagesList = packagesString.split(";");
				for (String pkgString : packagesList){
					if (monitor.isCanceled())
						return Status.CANCEL_STATUS;
					Module pkg = this.cluster.packages.fetchPackage(pkgString);
					String udfString = Info.request(seedNode, port, "udf-get:filename=" + pkg.getName());
					pkg.setDetailInfo(udfString);//gen=qgmyp0d8hQNvJdnR42X3BXgUGPE=;type=LUA;content=bG9jYWwgZnVuY3Rpb24gcHV0QmluKHIsbmFtZSx2YWx1ZSkKICAgIGlmIG5vdCBhZXJvc3Bpa2U6ZXhpc3RzKHIpIHRoZW4gYWVyb3NwaWtlOmNyZWF0ZShyKSBlbmQKICAgIHJbbmFtZV0gPSB2YWx1ZQogICAgYWVyb3NwaWtlOnVwZGF0ZShyKQplbmQKCi0tIFNldCBhIHBhcnRpY3VsYXIgYmluCmZ1bmN0aW9uIHdyaXRlQmluKHIsbmFtZSx2YWx1ZSkKICAgIHB1dEJpbihyLG5hbWUsdmFsdWUpCmVuZAoKLS0gR2V0IGEgcGFydGljdWxhciBiaW4KZnVuY3Rpb24gcmVhZEJpbihyLG5hbWUpCiAgICByZXR1cm4gcltuYW1lXQplbmQKCi0tIFJldHVybiBnZW5lcmF0aW9uIGNvdW50IG9mIHJlY29yZApmdW5jdGlvbiBnZXRHZW5lcmF0aW9uKHIpCiAgICByZXR1cm4gcmVjb3JkLmdlbihyKQplbmQKCi0tIFVwZGF0ZSByZWNvcmQgb25seSBpZiBnZW4gaGFzbid0IGNoYW5nZWQKZnVuY3Rpb24gd3JpdGVJZkdlbmVyYXRpb25Ob3RDaGFuZ2VkKHIsbmFtZSx2YWx1ZSxnZW4pCiAgICBpZiByZWNvcmQuZ2VuKHIpID09IGdlbiB0aGVuCiAgICAgICAgcltuYW1lXSA9IHZhbHVlCiAgICAgICAgYWVyb3NwaWtlOnVwZGF0ZShyKQogICAgZW5kCmVuZAoKLS0gU2V0IGEgcGFydGljdWxhciBiaW4gb25seSBpZiByZWNvcmQgZG9lcyBub3QgYWxyZWFkeSBleGlzdC4KZnVuY3Rpb24gd3JpdGVVbmlxdWUocixuYW1lLHZhbHVlKQogICAgaWYgbm90IGFlcm9zcGlrZTpleGlzdHMocikgdGhlbiAKICAgICAgICBhZXJvc3Bpa2U6Y3JlYXRlKHIpIAogICAgICAgIHJbbmFtZV0gPSB2YWx1ZQogICAgICAgIGFlcm9zcGlrZTp1cGRhdGUocikKICAgIGVuZAplbmQKCi0tIFZhbGlkYXRlIHZhbHVlIGJlZm9yZSB3cml0aW5nLgpmdW5jdGlvbiB3cml0ZVdpdGhWYWxpZGF0aW9uKHIsbmFtZSx2YWx1ZSkKICAgIGlmICh2YWx1ZSA+PSAxIGFuZCB2YWx1ZSA8PSAxMCkgdGhlbgogICAgICAgIHB1dEJpbihyLG5hbWUsdmFsdWUpCiAgICBlbHNlCiAgICAgICAgZXJyb3IoIjEwMDA6SW52YWxpZCB2YWx1ZSIpIAogICAgZW5kCmVuZAoKLS0gUmVjb3JkIGNvbnRhaW5zIHR3byBpbnRlZ2VyIGJpbnMsIG5hbWUxIGFuZCBuYW1lMi4KLS0gRm9yIG5hbWUxIGV2ZW4gaW50ZWdlcnMsIGFkZCB2YWx1ZSB0byBleGlzdGluZyBuYW1lMSBiaW4uCi0tIEZvciBuYW1lMSBpbnRlZ2VycyB3aXRoIGEgbXVsdGlwbGUgb2YgNSwgZGVsZXRlIG5hbWUyIGJpbi4KLS0gRm9yIG5hbWUxIGludGVnZXJzIHdpdGggYSBtdWx0aXBsZSBvZiA5LCBkZWxldGUgcmVjb3JkLiAKZnVuY3Rpb24gcHJvY2Vzc1JlY29yZChyLG5hbWUxLG5hbWUyLGFkZFZhbHVlKQogICAgbG9jYWwgdiA9IHJbbmFtZTFdCgogICAgaWYgKHYgJSA5ID09IDApIHRoZW4KICAgICAgICBhZXJvc3Bpa2U6cmVtb3ZlKHIpCiAgICAgICAgcmV0dXJuCiAgICBlbmQKCiAgICBpZiAodiAlIDUgPT0gMCkgdGhlbgogICAgICAgIHJbbmFtZTJdID0gbmlsCiAgICAgICAgYWVyb3NwaWtlOnVwZGF0ZShyKQogICAgICAgIHJldHVybgogICAgZW5kCgogICAgaWYgKHYgJSAyID09IDApIHRoZW4KICAgICAgICByW25hbWUxXSA9IHYgKyBhZGRWYWx1ZQogICAgICAgIGFlcm9zcGlrZTp1cGRhdGUocikKICAgIGVuZAplbmQKCi0tIFNldCBleHBpcmF0aW9uIG9mIHJlY29yZAotLSBmdW5jdGlvbiBleHBpcmUocix0dGwpCi0tICAgIGlmIHJlY29yZC50dGwocikgPT0gZ2VuIHRoZW4KLS0gICAgICAgIHJbbmFtZV0gPSB2YWx1ZQotLSAgICAgICAgYWVyb3NwaWtlOnVwZGF0ZShyKQotLSAgICBlbmQKLS0gZW5kCg==;
				}
			}
			monitor.worked(10);
			
			//refresh Indexes
			if (monitor.isCanceled())
				return Status.CANCEL_STATUS;
			monitor.subTask("Fetching Indexes");
			String indexString = Info.request(seedNode, port, "sindex");
			if (!indexString.isEmpty()){
				String[] indexList = indexString.split(";");
				for (String index : indexList){
					if (monitor.isCanceled())
						return Status.CANCEL_STATUS;
					this.cluster.indexes.add(index);	
				}
			}
			monitor.worked(10);
			


		} catch (AerospikeException e) {
			CoreActivator.showError(e, "Could not refresh cluster");
			return Status.CANCEL_STATUS;
		} catch (InterruptedException e) {
			CoreActivator.showError(e, "Could not refresh cluster");
			return Status.CANCEL_STATUS;
		}


		// refresh the view
		UIJob job = new UIJob("Refresh Aerospike nodes") { 


			public IStatus runInUIThread(IProgressMonitor arg0) {
				if (viewer != null && viewer.getControl() != null && !viewer.getControl().isDisposed()) {
					viewer.refresh();
				}
				return Status.OK_STATUS;
			}

		};
		//        ISchedulingRule rule = op.getRule();
		//        if (rule != null) {
		//            job.setRule(rule);
		//        }
		job.setUser(true);
		job.schedule();
		return Status.OK_STATUS;
	}

}
