package com.architexa.collab.proxy;

import org.eclipse.core.runtime.Platform;

public class PluginUtils {

	private static double getStartsWithDouble(String inp) {
		int pos = inp.indexOf('.');
		if (pos == -1) return Double.parseDouble(inp);
		pos = inp.indexOf('.', pos+1);
		if (pos == -1) 
			return Double.parseDouble(inp);
		else
			return Double.parseDouble(inp.substring(0, pos));
	}

	/**
	 * When comparing the plugin version returned by this method to another 
	 * version number, use PluginUtils.versionsEqual()to test for equality
	 * rather than ==. That is, instead of doing if(getPluginVer >= 3.3), do
	 * if(getPluginVer > 3.3 || versionsEqual(getPluginVer, 3.3) 
	 */
	public static final double getPluginVer(String symbolicName) {
		String ver = Platform.getBundle(symbolicName).getHeaders().get("Bundle-Version").toString();
		return getStartsWithDouble(ver);
    }
	
}
