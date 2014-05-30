package com.architexa.collab;

import org.eclipse.ui.IStartup;

import com.architexa.collab.core.IProxyDataSource;
import com.architexa.collab.core.IProxyDataSourceGenerator;
import com.architexa.collab.core.Srvr;

public class CollabCoreStartup implements IStartup {

	public void earlyStartup() {
		// Injecting Proxy Update class 
//		Srvr.injectedProxyDataClass = new ChildProxyUpdate().getClass();
//		Srvr.injectedProxyDataClass = ChildProxyUpdate.class;
//		Srvr.injectedProxyDataSource = new ChildProxyDataSource();
//		Srvr.injectedProxyDataClass = new IProxyDataSource {
//			public void getProxyData() {
//				return;
//			}
//		};
		Srvr.injectedProxyDataSourceGenerator = new IProxyDataSourceGenerator() {
			
			public IProxyDataSource getProxyDataSource() {
				try {
					return CollabPlugin.getProxy();
				} catch (Throwable t) {
					t.printStackTrace();
				}
				return null;
			}
		};
	}

}
