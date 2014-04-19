package com.aerospike.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.statushandlers.StatusManager;
import org.osgi.framework.BundleContext;

import com.aerospike.client.AerospikeClient;
import com.aerospike.client.AerospikeException;




/**
 * The activator class controls the plug-in life cycle
 */
public class CoreActivator extends AbstractUIPlugin {

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
	
	public static AerospikeClient getClient(IProject project){
		AerospikeClient client = null;
		try {
			client = (AerospikeClient) project.getSessionProperty(CoreActivator.CLIENT_PROPERTY);
			if (client != null && !client.isConnected()){
				client = null;
			}
			if (client == null){
				String seedNode = project.getPersistentProperty(CoreActivator.SEED_NODE_PROPERTY);
				String portString = project.getPersistentProperty(CoreActivator.PORT_PROPERTY);
				client = new AerospikeClient(seedNode, Integer.parseInt(portString));
				project.setSessionProperty(CoreActivator.CLIENT_PROPERTY, client);
			}
		} catch (CoreException e) {
			showError(e, "Cannot get Aerospike client");
		} catch (NumberFormatException e) {
			showError(e, "Cannot get Aerospike client");
		} catch (AerospikeException e) {
			showError(e, "Cannot get Aerospike client");
		}
		return client;
		
	}

}
