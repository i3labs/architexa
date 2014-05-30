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
package com.architexa.intro;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.osgi.framework.BundleContext;

import com.architexa.rse.AccountSettings;
import com.architexa.rse.BuildSettings;
import com.architexa.utils.log4j.EclipseLog4JUtils;
import com.eaio.util.lang.Hex;


/**
 * The main plugin class to be used in the desktop.
 */
public class AtxaIntroPlugin extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "com.architexa.intro";

    //The shared instance.
	private static AtxaIntroPlugin plugin;
	
	//Resource bundle.
	//private ResourceBundle resourceBundle;

	private IPersistentPreferenceStore configStore;

	/**
	 * The constructor.
	 */
	public AtxaIntroPlugin() {
		super();
		plugin = this;
		//try {
		//	resourceBundle= ResourceBundle.getBundle("com.architexa.intro.ReloPluginResources");
		//} catch (MissingResourceException x) {
		//	resourceBundle = null;
		//}
	}

    public static Logger getLogger(Class clazz) {
        return EclipseLog4JUtils.getLogger(PLUGIN_ID, clazz);
    }

    public static ImageDescriptor getImageDescriptor(String path) {
		return AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, path);
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
	}

	/**
	 * Returns the shared instance.
	 */
	public static AtxaIntroPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns the workspace instance.
	 */
	public static IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}

	///**
	// * Returns the string from the plugin's resource bundle,
	// * or 'key' if not found.
	// */
	//public static String getResourceString(String key) {
	//	ResourceBundle bundle= AtxaIntroPlugin.getDefault().getResourceBundle();
	//	try {
	//		return (bundle != null) ? bundle.getString(key) : key;
	//	} catch (MissingResourceException e) {
	//		return key;
	//	}
	//}
	//
	///**
	// * Returns the plugin's resource bundle,
	// */
	//public ResourceBundle getResourceBundle() {
	//	return resourceBundle;
	//}

	@Override
	protected void initializeDefaultPreferences(IPreferenceStore store) {
		AccountSettings.initializeDefaultPreferences();
		BuildSettings.initializeDefaultPreferences();
	}
	
    public IPersistentPreferenceStore getConfigStore() {
        // Create the preference store lazily.
        if (configStore == null)
        	configStore = new ScopedPreferenceStore(new ConfigurationScope(),getBundle().getSymbolicName());
        return configStore;
    }

	private static char[] charList = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0213456789".toCharArray();
	private static String createUniqueKey() {
		
		if (!getUniqueKey().trim().equals("")) return getUniqueKey();
		
		Random rand = new Random();
		String id = "";
		for (int i = 0; i < 12; i++) {
			// no numbers in the first spot
			if (i == 0)
				id += charList[rand.nextInt(charList.length - 10)];
			else
				id += charList[rand.nextInt(charList.length)];
		}
		return id;
	}

	public static String getUniqueKey() {
		String key = AccountSettings.getConfigSystemKey();
		if (key == null || key.length() == 0)
			key = getModifiedKey(getMacAddressStr());
		// No network card
		if (key == null)
			key = createUniqueKey();
		//Save key in config
		AccountSettings.setConfigSystemKey(key);
		return key;
	}
	
	private static String getModifiedKey(String key) {
		//Strip the colons and rot13 the String chars
		String tempKey = key.replaceAll(":", "");
		for (int i = 0; i < tempKey.length(); i++) {
            char c = tempKey.charAt(i);
            if       (c >= 'a' && c <= 'm') c += 13;
            else if  (c >= 'n' && c <= 'z') c -= 13;
            else if  (c >= 'A' && c <= 'M') c += 13;
            else if  (c >= 'A' && c <= 'Z') c -= 13;
        }
		return tempKey;
	}  
	
	private static String macAddress = "";
	private static List<String> macList = new ArrayList<String>();
	
	public static List<String> getMacList() {
		List<String> keyList = new ArrayList<String>();
		if (macList.isEmpty())
			getMacAddressStr();
		for (String str : macList) {
			keyList.add(getModifiedKey(str));
		}
		return keyList;
	}
	
	public static String getMacAddressStr() {
		if (macAddress != null && macAddress.length() != 0)
			return macAddress;
//		List<String> macList = new ArrayList<String>();
		String macStr = null;
		try {
            Class.forName("java.net.InterfaceAddress");
            macStr = Class.forName(
                    "com.eaio.uuid.UUIDGen$HardwareAddressLookup").newInstance().toString();
        }
        catch (ExceptionInInitializerError err) {
            // Ignored.
        }
        catch (ClassNotFoundException ex) {
            // Ignored.
        }
        catch (LinkageError err) {
            // Ignored.
        }
        catch (IllegalAccessException ex) {
            // Ignored.
        }
        catch (InstantiationException ex) {
            // Ignored.
        }
        catch (SecurityException ex) {
            // Ignored.
        }

        if (macStr == null) {

            Process p = null;
            BufferedReader in = null;

            try {
                String osname = System.getProperty("os.name", "");

                if (osname.startsWith("Windows")) {
                    p = Runtime.getRuntime().exec(
                            new String[] { "ipconfig", "/all" }, null);
                }
                // Solaris code must appear before the generic code
                else if (osname.startsWith("Solaris")
                        || osname.startsWith("SunOS")) {
                    String hostName = getFirstLineOfCommand(
                            "uname", "-n" );
                    if (hostName != null) {
                        p = Runtime.getRuntime().exec(
                                new String[] { "/usr/sbin/arp", hostName },
                                null);
                    }
                }
                else if (new File("/usr/sbin/lanscan").exists()) {
                    p = Runtime.getRuntime().exec(
                            new String[] { "/usr/sbin/lanscan" }, null);
                }
                else if (new File("/sbin/ifconfig").exists()) {
                    p = Runtime.getRuntime().exec(
                            new String[] { "/sbin/ifconfig", "-a" }, null);
                }

                if (p != null) {
                    in = new BufferedReader(new InputStreamReader(
                            p.getInputStream()), 128);
                    String l = null;
                    while ((l = in.readLine()) != null) {
                        macStr = parse(l);
                        if (macStr != null
                                && Hex.parseShort(macStr) != 0xff) {
                        	macList.add(macStr);
                        }
                    }
                }

            }
            catch (SecurityException ex) {
                // Ignore it.
            }
            catch (IOException ex) {
                // Ignore it.
            }
            finally {
                if (p != null) {
                    if (in != null) {
                        try {
                            in.close();
                        }
                        catch (IOException ex) {
                            // Ignore it.
                        }
                    }
                    try {
                        p.getErrorStream().close();
                    }
                    catch (IOException ex) {
                        // Ignore it.
                    }
                    try {
                        p.getOutputStream().close();
                    }
                    catch (IOException ex) {
                        // Ignore it.
                    }
                    p.destroy();
                }
            }

        }
        
        String temp = "";
        for (String mc : macList) {
        	if (temp.length() == 0) {
        		temp = mc;
        		continue;
        	}
        	if (temp.charAt(0) > mc.charAt(0))
        		temp = mc;
        }
        
        macAddress = temp;
        return macAddress;
	}
	
	private static String parse(String in) {

        String out = in;

        // lanscan

        int hexStart = out.indexOf("0x");
        if (hexStart != -1 && out.indexOf("ETHER") != -1) {
            int hexEnd = out.indexOf(' ', hexStart);
            if (hexEnd > hexStart + 2) {
                out = out.substring(hexStart, hexEnd);
            }
        }

        else {

            int octets = 0;
            int lastIndex, old, end;

            if (out.indexOf('-') > -1) {
                out = out.replace('-', ':');
            }

            lastIndex = out.lastIndexOf(':');

            if (lastIndex > out.length() - 2) {
                out = null;
            }
            else {

                end = Math.min(out.length(), lastIndex + 3);

                ++octets;
                old = lastIndex;
                while (octets != 5 && lastIndex != -1 && lastIndex > 1) {
                    lastIndex = out.lastIndexOf(':', --lastIndex);
                    if (old - lastIndex == 3 || old - lastIndex == 2) {
                        ++octets;
                        old = lastIndex;
                    }
                }

                if (octets == 5 && lastIndex > 1) {
                    out = out.substring(lastIndex - 2, end).trim();
                }
                else {
                    out = null;
                }

            }

        }

        if (out != null && out.startsWith("0x")) {
            out = out.substring(2);
        }

        return out;
    }
	
	private static String getFirstLineOfCommand(String... commands) throws IOException {

        Process p = null;
        BufferedReader reader = null;

        try {
            p = Runtime.getRuntime().exec(commands);
            reader = new BufferedReader(new InputStreamReader(
                    p.getInputStream()), 128);

            return reader.readLine();
        }
        finally {
            if (p != null) {
                if (reader != null) {
                    try {
                        reader.close();
                    }
                    catch (IOException ex) {
                        // Ignore it.
                    }
                }
                try {
                    p.getErrorStream().close();
                }
                catch (IOException ex) {
                    // Ignore it.
                }
                try {
                    p.getOutputStream().close();
                }
                catch (IOException ex) {
                    // Ignore it.
                }
                p.destroy();
            }
        }

    }

}
