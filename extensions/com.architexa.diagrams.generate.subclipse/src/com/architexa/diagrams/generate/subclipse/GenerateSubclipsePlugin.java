package com.architexa.diagrams.generate.subclipse;

import org.apache.log4j.Logger;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.architexa.utils.log4j.EclipseLog4JUtils;

/**
 * 
 * @author Vineet Sinha
 *
 */
public class GenerateSubclipsePlugin extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "com.architexa.diagrams.generate.subclipse";

	private static GenerateSubclipsePlugin plugin;

	public GenerateSubclipsePlugin() {
		super();
		plugin = this;
	}

	/**
	 * Returns the shared instance.
	 */
	public static GenerateSubclipsePlugin getDefault() {
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
		return EclipseLog4JUtils.getLogger(GenerateSubclipsePlugin.PLUGIN_ID, clazz);
	}

	public static Logger getLogger(String name) {
		return EclipseLog4JUtils.getLogger(GenerateSubclipsePlugin.PLUGIN_ID, name);
	}

}
