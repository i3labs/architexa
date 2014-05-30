package com.architexa.collab.proxy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ListIterator;

import org.apache.commons.collections.iterators.ArrayListIterator;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.architexa.collab.core.IProxyDataSource;
import com.architexa.collab.core.IProxyDataSourceGenerator;
import com.architexa.shared.SharedLogger;

public class ProxyDataSourceGenerator implements IProxyDataSourceGenerator {

	private static final String P_HTTP_HOST = "http.proxyHost";
	private static final String P_HTTP_PORT = "http.proxyPort";
	private static final String P_HTTP_USER = "http.proxyUser";
	private static final String P_HTTP_PASSWORD = "http.proxyPassword";
	private static final String P_HTTP_DOMAIN = "http.proxyDomain";
	private static final String HTTP_PROXY_TYPE = "HTTP";
	private static final String DIRECT_PROVIDER = "Direct"; 
	private static String debugString;
	private static final SharedLogger logger = new SharedLogger();
	public String getDebugStr () {
		try {
			getProxy();
			return debugString;
		} catch (Exception e) {
			logger.error("Error initiallizing Proxy", e);
			return "Error initiallizing Proxy\n" + e.getMessage();
		}
	}
	
	private ServiceTracker proxyTracker = null;
	private ServiceTracker getProxyTracker () throws ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		if (proxyTracker != null)
			return proxyTracker;
		
		String proxyServiceClassName = "org.eclipse.core.net.proxy.IProxyService";
		String bundleClassName = "org.osgi.framework.Bundle";
    	Class bundleClass = Class.forName(bundleClassName);
		Method getBundleContextMth = bundleClass.getMethod("getBundleContext", null);
		getBundleContextMth.setAccessible(true);
		
		BundleContext bundleCntx = (BundleContext) getBundleContextMth.invoke(bundle, null);
		proxyTracker = new ServiceTracker(bundleCntx, proxyServiceClassName, null);
		proxyTracker.open();
		
		return proxyTracker;
	}
	
	private Method getMethodtoInvoke (String methodName, Class className) throws SecurityException, NoSuchMethodException {
		Method method = className.getMethod(methodName, null);
		method.setAccessible(true);
		return method;
	}
	
	private ProxyDataSource getProxy () throws IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException, URISyntaxException {
		ProxyDataSource proxyInfo = new ProxyDataSource();
		
		double jdtUIVer = PluginUtils.getPluginVer("org.eclipse.jdt.ui");
		debugString = "";
		debugString += "Eclipse Version: " + jdtUIVer;
		if (jdtUIVer >= 3.3) {

			Object service = getProxyTracker().getService();
			if (service == null) {
				logger.error("Could not get service from proxyTracker.");
				debugString = "\nCould not get service from proxyTracker."; 
				logger.error(debugString);
				return null;
			}
				
			Class iProxyServiceClass = service.getClass();
			String iProxyService = iProxyServiceClass.getName();

			Boolean isProxiesEnabled = (Boolean) getMethodtoInvoke("isProxiesEnabled", iProxyServiceClass).invoke(service, null);
			if (!isProxiesEnabled.booleanValue()) {
				debugString += "\nConnecting through proxy provider: " + DIRECT_PROVIDER;
				return null;
			}
			
			Object proxyDataList = null;
			Method getProxyDataMth;
			Class iProxyDataClass = null;
			if (jdtUIVer >= 3.5) {
				// call the select method
				URI myArchitexa = new URI(HTTP_PROXY_TYPE, "//my.architexa.com", null);
				getProxyDataMth = iProxyServiceClass.getMethod("select", new Class[] {URI.class});
				getProxyDataMth.setAccessible(true);
				proxyDataList = getProxyDataMth.invoke(service, new Object[] {myArchitexa});
			} else {
				// call the getProxyForData method
				getProxyDataMth = iProxyServiceClass.getMethod("getProxyDataForHost", new Class[] {String.class, String.class});
				getProxyDataMth.setAccessible(true);
				//TODO add support for other types https, socks etc
				Object obj = getProxyDataMth.invoke(service, new Object[] {"//my.architexa.com", HTTP_PROXY_TYPE});
				proxyDataList = new Object[] {obj};
			}
			
			if (proxyDataList == null) {
				debugString += "\nRecieved null proxy list.";
				logger.error(debugString);
				return null;
			}
			
			ListIterator objList = new ArrayListIterator(proxyDataList);
			while (objList.hasNext()) {
				Object proxyData = objList.next();
				
				iProxyDataClass = proxyData.getClass();
				String iProxyData = iProxyDataClass.getName();
				
				String type = (String) getMethodtoInvoke("getType",	iProxyDataClass).invoke(proxyData, null);

				// Handling http only. Add https and other support later.
				if (!type.equalsIgnoreCase(HTTP_PROXY_TYPE))
					continue;
				// Setting all the relevant information in proxy 
				proxyInfo.setHttpProxyHost((String) getMethodtoInvoke("getHost", iProxyDataClass).invoke(proxyData, null));
				
				Integer port = (Integer) getMethodtoInvoke("getPort", iProxyDataClass).invoke(proxyData, null);
				
				proxyInfo.setHttpProxyUser((String) getMethodtoInvoke("getUserId", iProxyDataClass).invoke(proxyData, null));
				
				if (proxyInfo.getHttpProxyUser() != null)
						checkforNTMLProxyAndUpdate(proxyInfo);
				
				proxyInfo.setHttpProxyPassword((String) getMethodtoInvoke("getPassword", iProxyDataClass).invoke(proxyData, null));
				
				if (proxyInfo.getHttpProxyHost() != null) {
					if (port == null || port.intValue() == -1)
						proxyInfo.setHttpProxyPort(80);
					else
						proxyInfo.setHttpProxyPort(port.intValue());

					String eclipseHost = proxyInfo.getHttpProxyHost();
					int eclipsePort = proxyInfo.getHttpProxyPort();
					String eclipseUser = proxyInfo.getHttpProxyUser();
					String eclipsePass = proxyInfo.getHttpProxyPassword();
					String eclipseDom = proxyInfo.getHttpProxyDomain();
					
					debugString += "\nProxy Service class: " + iProxyService;
					debugString += "\nProxy Data Class: " + iProxyData;
					debugString += "\nProxy Host(Eclipse): " + eclipseHost;
					debugString += "\nProxy Port(Eclipse): " + eclipsePort;
					debugString += "\nProxy User(Eclipse): " + eclipseUser;
					debugString += "\nProxy Password(Eclipse): " + eclipsePass;
					debugString += "\nProxy Domain(Eclipse): " + eclipseDom;
				}
			}
		} else if (jdtUIVer < 3.3) {
			// 3.2 stuff
//			setUpProxyForOlderVersion(proxyInfo);
		}
		
		debugString += "\nProxy Host(System): " + System.getProperty(P_HTTP_HOST);
		debugString += "\nProxy Port(System): " + System.getProperty(P_HTTP_PORT);
		debugString += "\nProxy User(System): " + System.getProperty(P_HTTP_USER);
		debugString += "\nProxy Pass(System): " + System.getProperty(P_HTTP_PASSWORD);
		debugString += "\nProxy Domain(System): " + System.getProperty(P_HTTP_DOMAIN);
		debugString += "\nProxy Used to connect - HttpProxyHost: " + proxyInfo.getHttpProxyHost() + "\tHttpProxyPort: " + proxyInfo.getHttpProxyPort();
		return proxyInfo;
	}
	
	
    // TODO: This needs to be done with reflection since eclipse 4 no longer supports this type of updates

//	private static void setUpProxyForOlderVersion(ProxyDataSource proxyInfo) {
		// Get system properties for proxy setup.
        // If system properties are not set then pick up values from preference store
//		UpdateCore.getPlugin().getPluginPreferences();// Important: DO NOT REMOVE. System properties get set when this method is called for the first time
//		String httpProxyHost = System.getProperty(P_HTTP_HOST) != null ? 
//                        System.getProperty(P_HTTP_HOST)
//                        : UpdateCore.getPlugin().getPluginPreferences().getString(P_HTTP_HOST);
//        if (httpProxyHost.equals("")) //$NON-NLS-1$
//                httpProxyHost = null;
//        
//        String httpProxyPort = System.getProperty(P_HTTP_PORT) != null ?
//                System.getProperty(P_HTTP_PORT)
//                : UpdateCore.getPlugin().getPluginPreferences().getString(P_HTTP_PORT);
//        if (httpProxyPort.equals("")) //$NON-NLS-1$
//        	httpProxyPort = null;
//        
//		String httpProxyUser = System.getProperty(P_HTTP_USER) != null ? 
//				System.getProperty(P_HTTP_USER) 
//				: UpdateCore.getPlugin().getPluginPreferences().getString(P_HTTP_USER);
//		if (httpProxyUser.equals("")) //$NON-NLS-1$
//	    	httpProxyUser = null;		
//				
//		String httpProxyPassword = System.getProperty(P_HTTP_PASSWORD) != null ? 
//				System.getProperty(P_HTTP_PASSWORD)
//				: UpdateCore.getPlugin().getPluginPreferences().getString(P_HTTP_PASSWORD);
//		if (httpProxyPassword.equals("")) //$NON-NLS-1$
//		       httpProxyPassword = null;
//				
//		String httpProxyDomain = System.getProperty(P_HTTP_DOMAIN) != null ? 
//				System.getProperty(P_HTTP_DOMAIN)
//				: UpdateCore.getPlugin().getPluginPreferences().getString(P_HTTP_DOMAIN);
//		if (httpProxyDomain.equals("")) //$NON-NLS-1$
//		       httpProxyDomain = null;
//		
//		proxyInfo.setHttpProxyHost(httpProxyHost);
//		proxyInfo.setHttpProxyUser(httpProxyUser);
//		proxyInfo.setHttpProxyPassword(httpProxyPassword);
//		proxyInfo.setHttpProxyDomain(httpProxyDomain);
//		if (httpProxyPort != null)
//			proxyInfo.setHttpProxyPort(Integer.parseInt(httpProxyPort));
//		
//	}

	private static void checkforNTMLProxyAndUpdate(ProxyDataSource proxyInfo) {

		if (System.getProperty(P_HTTP_DOMAIN) != null) {
			proxyInfo.setHttpProxyDomain(System.getProperty(P_HTTP_DOMAIN));
			return;
		}
		
		if (proxyInfo.getHttpProxyUser() == null) return;
		String user = proxyInfo.getHttpProxyUser();
		if (user.indexOf("\\") != -1) {
			int slashLoc = user.indexOf("\\"); 
			proxyInfo.setHttpProxyDomain(user.substring(0, slashLoc));
			user = user.substring(slashLoc + 1);
			proxyInfo.setHttpProxyUser(user);
		}
	}
	
	private Bundle bundle = null;
	
	public ProxyDataSourceGenerator(Bundle _bundle) {
		this.bundle = _bundle;
	}

	public IProxyDataSource getProxyDataSource() {
		try {
			//return new ProxyDataSourceGenerator(Activator.getDefault().getBundle());
			return getProxy();
		} catch (Throwable t) {
			logger.error("Error getting proxy \n" + t.getMessage());
		}
		return null;
	}
	

	private class ProxyDataSource implements IProxyDataSource {
		private String httpProxyHost = null;
		private int httpProxyPort = -1;
		private String httpProxyUser = null;
		private String httpProxyPassword = null;
		private String httpProxyDomain = null;
		
		public ProxyDataSource() {
		}
		
		public void setHttpProxyHost(String httpProxyHost) {
			this.httpProxyHost = httpProxyHost;
		}
		public String getHttpProxyHost() {
			return httpProxyHost;
		}
		public void setHttpProxyPort(int httpProxyPort) {
			this.httpProxyPort = httpProxyPort;
		}
		public int getHttpProxyPort() {
			return httpProxyPort;
		}
		public void setHttpProxyUser(String httpProxyUser) {
			this.httpProxyUser = httpProxyUser;
		}
		public String getHttpProxyUser() {
			return httpProxyUser;
		}
		public void setHttpProxyPassword(String httpProxyPassword) {
			this.httpProxyPassword = httpProxyPassword;
		}
		public String getHttpProxyPassword() {
			return httpProxyPassword;
		}
		public void setHttpProxyDomain(String httpProxyDomain) {
			this.httpProxyDomain = httpProxyDomain;
		}
		public String getHttpProxyDomain() {
			return httpProxyDomain;
		}
	}

}
