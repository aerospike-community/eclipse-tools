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
package com.aerospike.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.statushandlers.StatusManager;
import org.osgi.framework.BundleContext;

import com.aerospike.client.AerospikeClient;
import com.aerospike.client.AerospikeException;
import com.aerospike.client.Host;
import com.aerospike.client.policy.ClientPolicy;
import com.aerospike.client.policy.InfoPolicy;
import com.aerospike.core.model.AsCluster;
import com.aerospike.core.model.ClusterRefreshJob;
import com.aerospike.core.nature.AerospikeNature;
import com.aerospike.core.preferences.PreferenceConstants;




/**
 * The activator class controls the plug-in life cycle
 */
public class CoreActivator extends AbstractUIPlugin implements IResourceChangeListener{

	// The plug-in ID
	public static final String PLUGIN_ID = "aerospike-core-plugin"; //$NON-NLS-1$

	public static final String AS_CONSOLE = "AerospikeConsole"; //$NON-NLS-1$

	public static final QualifiedName SEED_NODE_PROPERTY = new QualifiedName("Aerospike", "SeedNode");
	public static final QualifiedName PORT_PROPERTY = new QualifiedName("Aerospike", "Port");
	public static final QualifiedName CLUSTER_PROPERTY = new QualifiedName("Aerospike", "AerospikeCluster");
	public static final QualifiedName CLIENT_PROPERTY = new QualifiedName("Aerospike", "AerospikeClient");
	public static final QualifiedName UDF_DIRECTORY = new QualifiedName("Aerospike", "UDFDirectory");
	public static final QualifiedName AQL_GENERATION_DIRECTORY = new QualifiedName("Aerospike", "AQLGenerationDirectory");
	public static final QualifiedName UDF_REGISTERED = new QualifiedName("Aerospike", "UDFregistered");
	public static final QualifiedName CLUSTER_CONNECTION_TIMEOUT_PROPERTY = new QualifiedName("Aerospike", "ClusterConnectionTimeout");
	private static final QualifiedName CLIENT_POLICY = new QualifiedName("Aerospike", "ClientPolicy");
	private static final QualifiedName INFO_POLICY = new QualifiedName("Aerospike", "InfoPolicy");
	public static final QualifiedName EDITED_FROM_NODE = new QualifiedName("Aerospike", "EditedFromNode");
	public static final QualifiedName CLUSTER = new QualifiedName("Aerospike", "AsCluster");
	public static final QualifiedName AUTO_REFRESH = new QualifiedName("Aerospike", "AutoRefreshCluster");
	public static final QualifiedName REFRESH_PERIOD = new QualifiedName("Aerospike", "RefreshPeriod");

	public static final QualifiedName USER_PROPERTY = new QualifiedName("Aerospike", "User");
	public static final QualifiedName PASSWORD_PROPERTY = new QualifiedName("Aerospike", "Password");
	
	// The shared instance
	private static CoreActivator plugin;
	//	
	/**
	 * The constructor
	 */
	public CoreActivator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static CoreActivator getDefault() {
		return plugin;
	}
	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return getImageDescriptor(PLUGIN_ID, path);
	}
	public static ImageDescriptor getImageDescriptor(String pluginID, String path) {
		return imageDescriptorFromPlugin(pluginID, path);
	}
	/**
	 * Returns an Image for the image file at the given
	 * plug-in relative path 
	 * @param iconString
	 * @return
	 */
	public static Image getImage(String iconString) {
		ImageDescriptor img = getImageDescriptor(iconString);
		if (img == null)
			return null;
		return img.createImage();
	}

	/**
	 * 
	 * shows an error
	 * @param e
	 * @param message
	 */
	public static void showError(Throwable e, String message){
		IStatus status = new Status(IStatus.ERROR, PLUGIN_ID, e.getMessage(), e);
		StatusManager.getManager().handle(status, StatusManager.LOG | StatusManager.SHOW);
	}
	public static void showError(String message){
		IStatus status = new Status(IStatus.ERROR, PLUGIN_ID, message, null);
		StatusManager.getManager().handle(status, StatusManager.LOG | StatusManager.SHOW);
	}
	public static void log(int level, String message){
		plugin.getLog().log(new Status(level, PLUGIN_ID, message, null));
	}
	public static void log(int level, String message, Throwable e){
		plugin.getLog().log(new Status(level, PLUGIN_ID, message, e));
	}

	public static MessageConsole findConsole(String name) {
		MessageConsole targetConsole = null;
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		if (plugin != null){
			IConsoleManager conMan = plugin.getConsoleManager();
			IConsole[] existing = conMan.getConsoles();
			for (int i = 0; i < existing.length; i++)
				if (name.equals(existing[i].getName()))
					return (MessageConsole) existing[i];
			//no console found, so create a new one
			targetConsole = new MessageConsole(name, null);
			conMan.addConsoles(new IConsole[]{targetConsole});
		}
		return targetConsole;
	}

	public static MessageConsole findAerospikeConsole() {
		return findConsole(AS_CONSOLE);
	}

	public static void clearPolicies(IProject project){
		try {	
			project.setSessionProperty(CoreActivator.CLIENT_POLICY, null);
			project.setSessionProperty(CoreActivator.INFO_POLICY, null);
		} catch (CoreException e) {
			showError(e, "Cannot clear policies");
		}
	}
	public static void clearClient(IProject project){
		AerospikeClient client;
		try {
			client = (AerospikeClient) project.getSessionProperty(CoreActivator.CLIENT_PROPERTY);
			if (client != null){
				client.close();
				client = null;
			}
			project.setSessionProperty(CoreActivator.CLIENT_PROPERTY, null);
			clearPolicies(project);
		} catch (CoreException e) {
			showError(e, "Cannot clear Aerospike client");
		}
	}

	public static AerospikeClient getClient(IProject project){
		AerospikeClient client = null;
		try {
			client = (AerospikeClient) project.getSessionProperty(CoreActivator.CLIENT_PROPERTY);
			/*
			 * remove client if not connected
			 */
			if (client != null && !client.isConnected()){
				client = null;
			}
			/*
			 * if there is no client, make one
			 */
			if (client == null){
				String seedNode = getSeedHost(project);
				int port = getPort(project);
				ClientPolicy cp = getClientPolicy(project);
				if (seedNode.contains(",")){
					String names[] = seedNode.split(",");
					List<Host> hosts = new ArrayList<Host>();
					for (String name : names){
						Host host = new Host(name, port);
						hosts.add(host);
					}
					client = new AerospikeClient(cp, hosts.toArray(new Host[hosts.size()]));
				} else {
					client = new AerospikeClient(cp, seedNode, port);
				}
				project.setSessionProperty(CoreActivator.CLIENT_PROPERTY, client);
			}
		} catch (CoreException e) {
			CoreActivator.clearClient(project);
			showError(e, "Cannot get Aerospike client");
		} catch (NumberFormatException e) {
			CoreActivator.clearClient(project);
			showError(e, "Cannot get Aerospike client");
		} catch (AerospikeException e) {
			CoreActivator.clearClient(project);
			//showError(e, "Cannot get Aerospike client");
		}
		return client;

	}

	public static AsCluster getCluster(IProject project) {
			try {
				return (AsCluster) project.getSessionProperty(CoreActivator.CLUSTER);
			} catch (CoreException e) {
				showError(e, "Cannot get Cluster from session property");
			}
			return null;
	}
	
	public static ClientPolicy getClientPolicy(IProject project){
		ClientPolicy policy = null;
		try {
			policy = (ClientPolicy) project.getSessionProperty(CoreActivator.CLIENT_POLICY);
			if (policy == null){
				policy = new ClientPolicy();
				policy.timeout = getConnectionTimeout(project);
				policy.failIfNotConnected = true;
				policy.user = getUser(project);
				policy.password = getPassword(project);
				project.setSessionProperty(CoreActivator.CLIENT_POLICY, policy);
				
			}
		} catch (CoreException e) {
			showError(e, "Cannot get Aerospike client Policy");
		}
		return policy;
	}

	public static InfoPolicy getInfoPolicy(IProject project){
		InfoPolicy policy = null;
		try {
			policy = (InfoPolicy) project.getSessionProperty(CoreActivator.INFO_POLICY);
			if (policy == null){
				policy = new InfoPolicy();
				policy.timeout = getConnectionTimeout(project);
				project.setSessionProperty(CoreActivator.INFO_POLICY, policy);
			}
		} catch (CoreException e) {
			showError(e, "Cannot get Info Policy");
		}
		return policy;
	}

	public static int getPort(IProject project){
		String portString;
		int port = 3000;
		try {
			portString = project.getPersistentProperty(CoreActivator.PORT_PROPERTY);
			if (portString == null || portString.isEmpty()){
				IPreferenceStore store = getDefault().getPreferenceStore();
				port = store.getDefaultInt(PreferenceConstants.PORT);
				project.setPersistentProperty(CoreActivator.PORT_PROPERTY, Integer.toString(port));
			} else {
				port = Integer.parseInt(portString);
			}
		} catch (CoreException e) {
			CoreActivator.log(Status.ERROR, "Error getting PORT_PROPERTY", e);
		} catch (NumberFormatException e){
			//use default
		}
		return port;
	}
	public static int getConnectionTimeout(IProject project) {
		String timeoutString;
		int timeout = 20;
		try {
			timeoutString = project.getPersistentProperty(CoreActivator.CLUSTER_CONNECTION_TIMEOUT_PROPERTY);
			if (timeoutString == null || timeoutString.isEmpty()){
				IPreferenceStore store = getDefault().getPreferenceStore();
				timeout = store.getDefaultInt(PreferenceConstants.CLUSTER_CONNECTION_TIMEOUT);
				project.setPersistentProperty(CoreActivator.PORT_PROPERTY, Integer.toString(timeout));
			} else {
				timeout = Integer.parseInt(timeoutString);
			}
		} catch (CoreException e) {
			CoreActivator.log(Status.ERROR, "Error getting CLUSTER_CONNECTION_TIMEOUT_PROPERTY", e);
		} catch (NumberFormatException e){
			//use default
		}
		return timeout;
	}
	public static String getSeedHost(IProject project){
		String seedHost = "127.0.0.1";
		try {
			seedHost = project.getPersistentProperty(CoreActivator.SEED_NODE_PROPERTY);
			if (seedHost == null || seedHost.isEmpty()){
				IPreferenceStore store = getDefault().getPreferenceStore();
				seedHost = store.getDefaultString(PreferenceConstants.SEED_NODE);
				project.setPersistentProperty(CoreActivator.SEED_NODE_PROPERTY, seedHost);
			}
		} catch (CoreException e) {
			CoreActivator.log(Status.ERROR, "Error getting SEED_NODE_PROPERTY", e);
		}
		return seedHost;
	}

	public static  Map<String, String> parseMap(final String input, String seperator) {
		final Map<String, String> map = new HashMap<String, String>();
		for (String pair : input.split(seperator)) {
			String[] kv = pair.split("=");
			map.put(kv[0], kv[1]);
		}
		return map;
	}

	public static IFolder getUdfFolder(IProject project){
		String udfFolderString;
		try {
			udfFolderString = project.getPersistentProperty(CoreActivator.UDF_DIRECTORY);
			IResource udfFolder = project.findMember(udfFolderString);
			return (IFolder) udfFolder;
		} catch (CoreException e) {
			showError(e, "Cannot locate UDF folder");
		}
		return null;
	}

	public static String getUser(IProject project) {
		String user = null;
		try {
			user = project.getPersistentProperty(CoreActivator.USER_PROPERTY);
		} catch (CoreException e) {
			CoreActivator.log(Status.ERROR, "Error getting USER_PROPERTY", e);
		}
		return user;
	}
	public static String getPassword(IProject project) {
		String password = null;
		try {
			password = project.getPersistentProperty(CoreActivator.PASSWORD_PROPERTY);
		} catch (CoreException e) {
			CoreActivator.log(Status.ERROR, "Error getting PASSWORD_PROPERTY", e);
		}
		return password;
	}

	@Override
	public void resourceChanged(final IResourceChangeEvent event) {
	    if (event == null || event.getDelta() == null) {
	        return;
	    }

		if (event.getResource() instanceof IProject){
			IProject project = (IProject) event.getResource();
			try {
		        event.getDelta().accept(new IResourceDeltaVisitor() {
		            public boolean visit(final IResourceDelta delta) throws CoreException {
		                IResource resource = delta.getResource();
		                if (((resource.getType() & IResource.PROJECT) != 0)
		                        && resource.getProject().isOpen()
		                        && delta.getKind() == IResourceDelta.CHANGED
		                        && ((delta.getFlags() & IResourceDelta.OPEN) != 0)) {

		                    IProject project = (IProject)resource;
			 				if (project.hasNature(AerospikeNature.NATURE_ID)){
								ClusterRefreshJob refreshJob = new ClusterRefreshJob(project);
								refreshJob.schedule(5);
			 				}
		                }
		                return true;
		            }
		        });
			} catch (CoreException e) {
				showError(e, "Error registering project listener");
			}
		}
		
	}

}
