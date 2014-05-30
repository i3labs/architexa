//package com.architexa.rse.update;
//
//import org.apache.log4j.Logger;
//import org.eclipse.core.runtime.CoreException;
//import org.eclipse.core.runtime.IProgressMonitor;
//import org.eclipse.core.runtime.IStatus;
//import org.eclipse.core.runtime.Status;
//import org.eclipse.core.runtime.jobs.IJobChangeEvent;
//import org.eclipse.core.runtime.jobs.IJobChangeListener;
//import org.eclipse.core.runtime.jobs.Job;
//import org.eclipse.core.runtime.jobs.JobChangeAdapter;
//import org.eclipse.update.internal.ui.UpdateUIMessages;
//import org.eclipse.update.ui.UpdateJob;
//
//import com.architexa.intro.AtxaIntroPlugin;
//import com.architexa.intro.InstalledPluginUtils;
//
//public class CheckForUpdateJob extends Job {
//
//	static final Logger logger = AtxaIntroPlugin.getLogger(CheckForUpdateJob.class);
//	private static String atxaFeatureId = "com.architexa.rse";
//
//	public CheckForUpdateJob() {
//		super("Checking for Architexa update");
//	}
//
//	@Override
//	protected IStatus run(IProgressMonitor monitor) {
//		try {
//			return checkForUpdateAndSetNotification();
//		} catch (CoreException e) {
//			logger.error("Unexpected exception while " +
//					"checking for Architexa update.", e);
//		}
//		return Status.CANCEL_STATUS;
//	}
//
//	private IStatus checkForUpdateAndSetNotification() throws CoreException {
//
//		// Find the Architexa plugin currently installed
//		IFeature installedAtxaFeature = getInstalledArchitexaFeature();
//		if(installedAtxaFeature==null) {
//			// do not care if we are extended feature or not since we are not showing the button here
//			UpdateToolbar.showNotification(false, false); 
//			return Status.CANCEL_STATUS;
//		}
//		final boolean isExtendedVersion = InstalledPluginUtils.isRSESubclipseInstalled() || InstalledPluginUtils.isRSESpringInstalled();
//		// Search whether an update for it is available
//		final UpdateJob updateJob = new UpdateJob(
//				UpdateUIMessages.InstallWizard_jobName, 
//				false, false, new IFeature[] {installedAtxaFeature});
//		updateJob.setUser(false);
//		updateJob.setPriority(Job.DECORATE);
//		IJobChangeListener jobListener = new JobChangeAdapter() {
//			@Override
//			public void done(IJobChangeEvent event) {
//				if(updateJob != event.getJob()) return;
//
//				// If update is available, show Update button to user
//				boolean updateAvailable = updateJob.getUpdates().length > 0;
//				UpdateToolbar.showNotification(updateAvailable, isExtendedVersion);
//				Job.getJobManager().removeJobChangeListener(this);
//				Job.getJobManager().cancel(updateJob);
//				super.done(event);
//			}
//		};
//		Job.getJobManager().addJobChangeListener(jobListener);
//		updateJob.schedule();
//		return Status.OK_STATUS;
//	}
//
//	public static IFeature getInstalledArchitexaFeature() throws CoreException {
//
//		IFeature atxaFeature = null;
//		ILocalSite localSite = SiteManager.getLocalSite();
//		IInstallConfiguration config = localSite.getCurrentConfiguration();
//		
//		// Search atxa update site for atxa feature
//		IConfiguredSite siteWithAtxaFeature = 
//			UpdateUtils.getSiteWithFeature(config, atxaFeatureId);
//		if(siteWithAtxaFeature==null) return null; // atxa plugin not installed
//
//		IFeature[] currentFeatures = UpdateUtils.searchSite(
//				atxaFeatureId, siteWithAtxaFeature, true);
//		atxaFeature = currentFeatures[0];
//		if(atxaFeature!=null) return atxaFeature;
//
//		// Couldn't find feature by searching the atxa site,
//		// so try looking through all configured sites for it
//		IConfiguredSite[] configuredSites = config.getConfiguredSites();
//		for (int i = 0; i < configuredSites.length; i++) {
//			IConfiguredSite configuredSite = configuredSites[i];
//
//			// Look for atxa feature
//			IFeatureReference[] configuredFeats = configuredSite.getConfiguredFeatures();
//			atxaFeature = searchReferencesForFeature(configuredFeats);
//			if(atxaFeature!=null) return atxaFeature;
//
//			// atxa feature not in configured features, so
//			// check all features (including unconfigured)
//			IFeatureReference[] allFeats = configuredSite.getSite().getFeatureReferences();
//			atxaFeature = searchReferencesForFeature(allFeats);
//			if(atxaFeature!=null) return atxaFeature;
//		}
//		return atxaFeature;
//	}
//
//	private static IFeature searchReferencesForFeature(
//			IFeatureReference[] featureRefs) throws CoreException {
//		for(int i=0; i<featureRefs.length; i++) {
//			IFeature feature = featureRefs[i].getFeature(null);
//			String featureId = feature.getVersionedIdentifier().getIdentifier();
//			if(atxaFeatureId.equals(featureId)) return feature;
//		}
//		return null;
//	}
//
//}
