package com.architexa.collab.ui;

import java.net.URL;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import com.architexa.utils.log4j.EclipseLog4JUtils;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.architexa.collab.ui";

	// The shared instance
	private static Activator plugin;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	public static ImageDescriptor getImageDescriptor(String path) {
		return AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	public static Logger getLogger(Class<?> clazz) {
		return EclipseLog4JUtils.getLogger(PLUGIN_ID, clazz);
	}

	public static Logger getLogger(String name) {
		return EclipseLog4JUtils.getLogger(PLUGIN_ID, name);
	}

	public static boolean isRuntimeWorkbench() {
    	Bundle bundle = Platform.getBundle(PLUGIN_ID);
    	if (bundle == null) return false;
        URL url = bundle.getEntry("log4j.properties");
        if (url == null) return false;
		return true;
	}
}
