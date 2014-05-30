package com.architexa.intro;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

public class InstalledPluginUtils {
	static List<String> allInstalledFeatures;
		
	static {
		if (allInstalledFeatures == null)
			allInstalledFeatures = queryFeatures(new NullProgressMonitor());
	}
	
	public static List<String> getFeatures(IProgressMonitor monitor) {
		if (allInstalledFeatures != null)
			return allInstalledFeatures;
		else 
			return allInstalledFeatures = queryFeatures(monitor);
	}
	
	public static List<String> getFeatures() {
		return getFeatures(new NullProgressMonitor());
	}


	private static List<String> queryFeatures(IProgressMonitor monitor) {
		Object[] csites = null;
		if (allInstalledFeatures == null)
			allInstalledFeatures = new ArrayList<String>();
		try {
//			csites = openLocalSite();
			// ArrayList result = new ArrayList();
			// boolean showUnconf = true;//showUnconfFeaturesAction.isChecked();
			monitor.beginTask("Check Installed Plugins", csites.length*csites.length);
//			for (int i = 0; i < csites.length; i++) {
//				IConfiguredSiteAdapter adapter = (IConfiguredSiteAdapter) csites[i];
//				IConfiguredSite csite = adapter.getConfiguredSite();
//				IFeatureReference[] configFeat = csite.getConfiguredFeatures();
//				for (int j = 0; j < configFeat.length; j++) {
//					// System.out.println("config feat: " + configFeat[j]);
//					// System.out.println("id: " +
//					// configFeat[j].getFeature().getVersionedIdentifier().getIdentifier());
//					if (monitor.isCanceled()) return allInstalledFeatures;
//					monitor.worked(1);
//					allInstalledFeatures.add(configFeat[j].getFeature()
//							.getVersionedIdentifier().getIdentifier());
//				}
//			}
		} catch (Throwable e) {
			// potentially throws many errors, do not need to display
			// e.printStackTrace();
		}
		return allInstalledFeatures;
	}
	
//	static private Object[] openLocalSite() throws CoreException {
//		IInstallConfiguration config = SiteManager.getLocalSite()
//				.getCurrentConfiguration();
//		IConfiguredSite[] sites = config.getConfiguredSites();
//		Object[] result = new Object[sites.length];
//		for (int i = 0; i < sites.length; i++) {
//			result[i] = new ConfiguredSiteAdapter(config, sites[i]);
//		}
//		return result;
//	}

	
	private static List<String> createSpringList() {
		List<String> list = new ArrayList<String>();
		list.add("org.springframework.ide.eclipse.beans.ui");
		list.add("org.springframework.beans");
		list.add("org.springframework.core");
		list.add("org.springframework.ide.eclipse.ui");
		list.add("org.springframework.ide.eclipse");
		list.add("org.springframework.ide.eclipse.core");
		return list;
	}
	
	private static List<String> createSubclipseList() {
		List<String> list = new ArrayList<String>();
		list.add("org.tigris.subversion.subclipse.core");
		list.add("org.tigris.subversion.subclipse.ui");
		return list;
	}

	public static boolean isSpringInstalled() {
		return getFeatures().containsAll(createSpringList());
	}
	
	public static boolean isSubclipseInstalled() {
		return getFeatures().containsAll(createSubclipseList());
	}
	
	public static boolean isRSESubclipseInstalled() {
		return getFeatures().contains("com.architexa.diagrams.generate.subclipse") || getFeatures().contains("com.architexa.rse.integration.subclipse");
	}

	public static boolean isRSESpringInstalled() {
		return getFeatures().contains("com.architexa.extensions.entJava.spring") || getFeatures().contains("com.architexa.rse.entJava.spring");
	}

	
}
