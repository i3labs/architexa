package com.architexa.diagrams.chrono.sequence;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.architexa.diagrams.chrono.ui.ColorScheme;
import com.architexa.diagrams.ui.BaseColorScheme;
import com.architexa.utils.log4j.EclipseLog4JUtils;

/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public class SeqPlugin extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "com.architexa.diagrams.chrono";

	private static SeqPlugin plugin;

	public SeqPlugin() {
		super();
		plugin = this;
	}

	public SeqPlugin(IPluginDescriptor desc) {
		super(desc);
		plugin = this;
		BaseColorScheme.registerScheme(new ColorScheme());
	}

	public static SeqPlugin getDefault() {
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
		return EclipseLog4JUtils.getLogger(SeqPlugin.PLUGIN_ID, clazz);
	}

	public static Logger getLogger(String name) {
		return EclipseLog4JUtils.getLogger(SeqPlugin.PLUGIN_ID, name);
	}

}
