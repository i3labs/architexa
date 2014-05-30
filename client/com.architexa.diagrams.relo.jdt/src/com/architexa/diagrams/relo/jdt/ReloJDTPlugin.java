/* 
 * Copyright (c) 2004-2005 Massachusetts Institute of Technology. This code was
 * developed as part of the Haystack (http://haystack.lcs.mit.edu/) research 
 * project at MIT. Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation files (the 
 * "Software"), to deal in the Software without restriction, including without 
 * limitation the rights to use, copy, modify, merge, publish, distribute, 
 * sublicense, and/or sell copies of the Software, and to permit persons to whom
 * the Software is furnished to do so, subject to the following conditions: 
 * 
 * The above copyright notice and this permission notice shall be included in 
 * all copies or substantial portions of the Software. 
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER  
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, 
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE. 
 */
package com.architexa.diagrams.relo.jdt;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.architexa.diagrams.relo.jdt.parts.JDTEditPartsModel;
import com.architexa.utils.log4j.EclipseLog4JUtils;


/**
 * The main plugin class to be used in the desktop.
 */
public class ReloJDTPlugin extends Plugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.architexa.diagrams.relo.jdt";
	
	//The shared instance.
	private static ReloJDTPlugin plugin;
	//Resource bundle.
	private ResourceBundle resourceBundle;
	
	/**
	 * The constructor.
	 */
	public ReloJDTPlugin() {
		super();
		plugin = this;
		try {
			resourceBundle = ResourceBundle.getBundle("com.architexa.diagrams.relo.jdt.ReloJDTPluginResources");
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}
		initPluggins();
	}
	
	private void initPluggins() {
		JDTEditPartsModel jdtEPModel = new JDTEditPartsModel();
		jdtEPModel.earlyStartup();
	}

	/**
	 * This method is called upon plug-in activation
	 */
	@Override
    public void start(BundleContext context) throws Exception {
		super.start(context);
	}

    public static Logger getLogger(Class<?> clazz) {
        return EclipseLog4JUtils.getLogger(ReloJDTPlugin.PLUGIN_ID, clazz);
    }

	/**
	 * This method is called when the plug-in is stopped
	 */
	@Override
    public void stop(BundleContext context) throws Exception {
		super.stop(context);
	}

	/**
	 * Returns the shared instance.
	 */
	public static ReloJDTPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns the string from the plugin's resource bundle,
	 * or 'key' if not found.
	 */
	public static String getResourceString(String key) {
		ResourceBundle bundle = ReloJDTPlugin.getDefault().getResourceBundle();
		try {
			return (bundle != null) ? bundle.getString(key) : key;
		} catch (MissingResourceException e) {
			return key;
		}
	}

	/**
	 * Returns the plugin's resource bundle,
	 */
	public ResourceBundle getResourceBundle() {
		return resourceBundle;
	}

	public static ImageDescriptor getImageDescriptor(String path) {
		return AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
}
