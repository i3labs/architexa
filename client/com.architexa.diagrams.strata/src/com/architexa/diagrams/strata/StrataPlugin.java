/**
 * Copyright (c) 2007-2008 Architexa, Inc
 */ 
package com.architexa.diagrams.strata;

import org.apache.log4j.Logger;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.architexa.diagrams.strata.ui.ColorScheme;
import com.architexa.diagrams.ui.BaseColorScheme;
import com.architexa.utils.log4j.EclipseLog4JUtils;


/**
 * The main plugin class to be used in the desktop.
 */
public class StrataPlugin extends AbstractUIPlugin {
	
	// The plug-in ID
	public static final String PLUGIN_ID = "com.architexa.diagrams.strata";

	//The shared instance.
	private static StrataPlugin plugin;
	
	/**
	 * The constructor.
	 */
	public StrataPlugin() {
		plugin = this;
		BaseColorScheme.registerScheme(new ColorScheme());
	}

	/**
	 * This method is called upon plug-in activation
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		plugin = null;
	}

    /**
	 * Returns the shared instance.
	 */
	public static StrataPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path.
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

    public static Logger getLogger(Class<?> clazz) {
        return EclipseLog4JUtils.getLogger(PLUGIN_ID, clazz);
    }
    public static Logger getLogger(Class<?> clazz, String name) {
        return EclipseLog4JUtils.getLogger(PLUGIN_ID, clazz.getName() + "." + name);
    }

}
