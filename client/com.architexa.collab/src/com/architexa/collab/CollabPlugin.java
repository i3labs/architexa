package com.architexa.collab;

import org.apache.log4j.Logger;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.architexa.collab.core.IProxyDataSource;
import com.architexa.collab.core.Srvr;
import com.architexa.collab.proxy.ProxyDataSourceGenerator;
import com.architexa.shared.SharedLogger;
import com.architexa.utils.log4j.EclipseLog4JUtils;

/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public class CollabPlugin extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "com.architexa.collab";
	private static final Logger logger = getLogger(CollabPlugin.class);
	private static CollabPlugin plugin;
	
	public CollabPlugin() {
		super();
		plugin = this;
		Srvr.logger = new SharedLogger() {
			Logger srvrLogger = CollabPlugin.getLogger(Srvr.class);
			@Override
			public void info(String string) {
//				srvrLogger.info(string);
			}
			@Override
			public void error(String string) {
				srvrLogger.error(string);
			}
			@Override
			public void error(String string, Exception exception) {
				srvrLogger.error(string, exception);
			}
		};
	}

	/**
	 * Returns the shared instance.
	 */
	public static CollabPlugin getDefault() {
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

	public static Logger getLogger(Class clazz) {
		return EclipseLog4JUtils.getLogger(CollabPlugin.PLUGIN_ID, clazz);
	}

	public static Logger getLogger(String name) {
		return EclipseLog4JUtils.getLogger(CollabPlugin.PLUGIN_ID, name);
	}
	
//	private static String UNIQUE_KEY = "uniqueKey";
//	private static char[] charList = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0213456789".toCharArray();
//	private static String createUniqueKey() {
//		
//		if (!getUniqueKey().trim().equals("")) return getUniqueKey();
//		
//		Random rand = new Random();
//		String id = "";
//		for (int i = 0; i < 12; i++) {
//			// no numbers in the first spot
//			if (i == 0)
//				id += charList[rand.nextInt(charList.length - 10)];
//			else
//				id += charList[rand.nextInt(charList.length)];
//		}
//		
//		getDefault().getPreferenceStore().setValue(UNIQUE_KEY, id);
//		return id;
//	}
//
//	private static String getStoredUniqueKey() {
//		String key = getDefault().getPreferenceStore().getString(UNIQUE_KEY);
//		if (key == null || key.length()==0)
//			key = createUniqueKey();
//		return key;
//	}
//	
//	public static String getUniqueKey() {
//		String key = UUIDGen.getMACAddress();
//		key = modifyKey(key);
//		// No network card
//		if (key == null)
//			key = getStoredUniqueKey();
//		//Save key in config
//		getDefault().getPreferenceStore().setValue(UNIQUE_KEY, key);
//		return key;
//	}
//	
//	private static String modifyKey(String key) {
//		//Strip the colons and rot13 the String chars
//		String tempKey = key.replaceAll(":", "");
//		for (int i = 0; i < tempKey.length(); i++) {
//            char c = tempKey.charAt(i);
//            if       (c >= 'a' && c <= 'm') c += 13;
//            else if  (c >= 'n' && c <= 'z') c -= 13;
//            else if  (c >= 'A' && c <= 'M') c += 13;
//            else if  (c >= 'A' && c <= 'Z') c -= 13;
//        }
//		return tempKey;
//	}

	public static String getDebugStr () {
		return new ProxyDataSourceGenerator(plugin.getBundle()).getDebugStr();
	}

	public static IProxyDataSource getProxy() {
		try {
			return new ProxyDataSourceGenerator(plugin.getBundle()).getProxyDataSource();
		} catch (Throwable e) {
			logger.error("Error initiallizing Proxy", e);
		}
		return null;
	}
	
}
