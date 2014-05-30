package com.architexa.rse.update;

import java.lang.reflect.Method;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.architexa.collab.proxy.PluginUtils;
import com.architexa.intro.AtxaIntroPlugin;

public class UpdateAction extends Action {

	static final Logger logger = AtxaIntroPlugin.getLogger(UpdateAction.class);
	static final String atxaUpdateSite = "http://update.architexa.com/client/";
	public static final String atxaExtendedUpdateSite = "http://update.architexa.com/client-extended/";
//	static final String atxaExtendedUpdateSite = "http://subclipse.tigris.org/update_1.6.x";
	boolean isExtended = false;

	public UpdateAction(boolean isExtended) {
		this.isExtended = isExtended;
	}
	
	private static double jdtUIVer = PluginUtils.getPluginVer("org.eclipse.jdt.ui");
	@Override
	public void run() {
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		try {
			if (jdtUIVer >= 3.6) { // use equinox p2 update support
				Class<?> uhClass = Class.forName("org.eclipse.equinox.internal.p2.ui.sdk.UpdateHandler");
				Method getExecMth = uhClass.getMethod("execute", ExecutionEvent.class);
				getExecMth.setAccessible(true);
				getExecMth.invoke(uhClass.newInstance(), (ExecutionEvent)null);
			} else {  // use old update method
//				UpdateJob updateJob = getUpdateJob();
//				if(updateJob==null) 
//					UpdateManagerUI.openInstaller(shell);
//				else
//					UpdateManagerUI.openInstaller(shell, updateJob);
			} 
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

//	private UpdateJob getUpdateJob() {
//
//		URL siteURL=null;
//		try {
//			if (isExtended)
//				siteURL = new URL(atxaExtendedUpdateSite);
//			else
//				siteURL = new URL(atxaUpdateSite);
//			
//		} catch (MalformedURLException e) {
//			logger.error("Unable to update Architexa plugin.", e);
//		}
//		if(siteURL==null) {
//			return null;
//		}
//		UpdateSearchScope scope = new UpdateSearchScope();
//		scope.addSearchSite("Architexa Update Site", siteURL, null);
//
//		UpdateSearchRequest searchRequest = new UpdateSearchRequest(
//				UpdateSearchRequest.createDefaultSiteSearchCategory(),
//				scope);
//		searchRequest.addFilter(new BackLevelFilter());
//		searchRequest.addFilter(new EnvironmentFilter());
//
//		
//		UpdateJob updateJob = new UpdateJob("Updating Architexa..", searchRequest);
//
//		return updateJob;
//	}

}
