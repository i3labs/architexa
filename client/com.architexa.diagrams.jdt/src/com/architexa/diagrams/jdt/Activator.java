package com.architexa.diagrams.jdt;

import org.apache.log4j.Logger;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.architexa.diagrams.jdt.builder.ResourceQueueManager;
import com.architexa.diagrams.jdt.ui.RSEOutlineInformationControl;
import com.architexa.utils.log4j.EclipseLog4JUtils;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "com.architexa.diagrams.jdt";

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
		ResourceQueueManager.terminate();
		RSEOutlineInformationControl.savePrefs(); 
		plugin = null;
		super.stop(context);
	}
	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

    public static Logger getLogger(Class<?> clazz) {
        return EclipseLog4JUtils.getLogger(Activator.PLUGIN_ID, clazz);
    }
    public static Logger getLogger(Class<?> clazz, String name) {
        return EclipseLog4JUtils.getLogger(Activator.PLUGIN_ID, clazz.getName() + "." + name);
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
}
