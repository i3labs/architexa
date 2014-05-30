package com.architexa.diagrams.ui;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.architexa.diagrams.Activator;

/**
 * We don't really need to cache descriptors, but we do it anyway (for unified access)
 * 
 * The most common method called here is calcImageFromDescriptor.
 */
public class ImageCache {
	static final Logger logger = Activator.getLogger(ImageCache.class);
	
	public static Image ungroup = getImageDescriptor("icons/ungroup.PNG", "com.architexa.diagrams.chrono").createImage();
	public static Image remove = getImageDescriptor("icons/remove.gif", "com.architexa.diagrams.relo").createImage();
	public static Image impl_co = getImageDescriptor("icons/implm_co.gif", "com.architexa.diagrams.chrono").createImage();
	public static Image moduleColl = getImageDescriptor("icons/moduleColl.png", "com.architexa.diagrams.strata").createImage();
//	ImageDescriptor.createFromFile(ReloEditor.class, "error_co.gif").createImage();

	private static ImageDescriptor getImageDescriptor(String path, String plugin_id) {
		return AbstractUIPlugin.imageDescriptorFromPlugin(plugin_id, path);
	}

	// keys here are usually a string representation
	private static Map<String, ImageDescriptor> descriptorMapCache = new HashMap<String, ImageDescriptor>();
	
	// keys are sometimes a string representation, but more often a descriptor
	// -- caller needs to makes sure that the descriptor sent here is the same
	// as what has been put in the cache (either because it hashes to the same
	// value, or because the descriptor is from the above)
	private static Map<String, Image> keyImageMapCache = new HashMap<String, Image>();
	private static Map<ImageDescriptor, Image> descImageMapCache = new HashMap<ImageDescriptor, Image>();


	public static void add(String descriptorName, ImageDescriptor descriptor) {
		descriptorMapCache.put(descriptorName, descriptor);
	}
	public static ImageDescriptor getDescriptor(String descriptorName) {
		return descriptorMapCache.get(descriptorName);
	}

	public static void add(String imageName, Image icon) {
		keyImageMapCache.put(imageName, icon);
	}
	public static void add(ImageDescriptor desc, Image image) {
		descImageMapCache.put(desc, image);
	}
	public static Image getImage(ImageDescriptor descriptor) {
		return descImageMapCache.get(descriptor);
	}
	public static Image calcImageFromDescriptor(ImageDescriptor descriptor) {
		if (!descImageMapCache.containsKey(descriptor))
			descImageMapCache.put(descriptor, descriptor.createImage());
		return descImageMapCache.get(descriptor);
	}
	public static Image getImage(String imageName) {
		if (keyImageMapCache.containsKey(imageName))
			return keyImageMapCache.get(imageName);
		if (getDescriptor(imageName) != null) {
			if (descImageMapCache.containsKey(getDescriptor(imageName))) {
				keyImageMapCache.put(imageName, descImageMapCache.get(getDescriptor(imageName)));
				return descImageMapCache.get(getDescriptor(imageName));
			}
			// we don't convert descriptor to image - we only use it if already available
		}
		try {
        	URL url = Activator.getDefault().getBundle().getEntry("icons/" + imageName);
            Image newImg = new Image(Display.getDefault(), url.openStream());
            keyImageMapCache.put(imageName, newImg);
			return newImg;
        } catch (Exception e) {
            logger.error("Unexpected exception", e);
        }
        return null;
	}

}
