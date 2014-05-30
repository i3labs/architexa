package com.architexa.diagrams.jdt.builder;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;

import com.architexa.diagrams.jdt.Activator;
import com.architexa.rse.BuildStatus;
import com.architexa.store.ReloRdfRepository;
import com.architexa.store.ReloStoreInitializer;
import com.architexa.store.StoreUtil;

public class VersionedRepositoryCheckAndInitializer{
	static final Logger logger = Activator.getLogger(VersionedRepositoryCheckAndInitializer.class);

	/**
	 * Make sure that there is a current plugin version ReloRDFRepository (makes
	 * it if needed)
	 */
	public static void launchJobToCheckAndInitilizeStore() {
		Job initializerJob = new Job("Architexa RSE Connection") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					checkAndInitilizeStore(monitor);
				} catch (Throwable t) {
					logger.error("Unexpected exception", t);
				}
				return Status.OK_STATUS;
			}
		};
		initializerJob.setUser(false);
		initializerJob.setPriority(Job.LONG);
		initializerJob.schedule();
	}

	private static boolean forcedReinit = false;

	private static void checkAndInitilizeStore(IProgressMonitor monitor) {
		ReloRdfRepository repo = StoreUtil.getDefaultStoreRepository();
		if (!storeInitialized(repo) || forcedReinit)
			initializeStore(repo, monitor, false);

		// no need to keep the repository open if we are not accessing them
		//
		// not enabled since we currently can't really ensure that all the
		// previous build jobs have finished building, though we currently have
		// a better records with cleaning than with building
		// StoreUtil.shutdownDefaultRepository();
	}

	public final static String storeNS = "store#";
	private static final String builders = "builders";
	private static final String initialized = "initialized";

	private static boolean storeInitialized(ReloRdfRepository repo) {
		Resource bulderRes = repo.getDefaultURI(storeNS, builders);
		URI initializedURI = repo.getDefaultURI(storeNS, initialized);

		// check for correct version
		return repo.hasStatement(bulderRes, initializedURI, repo.getLiteral(getPluginVer()));
	}

	private static final String getPluginVer() {
		// place to change to cause a rebuild for new releases
		// ReloStorePlugin.getDefault().getBundle().getHeaders().get("Bundle-Version").toString();
		// updating on every release might be too much - lets slow things down
		// by versioning the db
		return "1";
	}

	public static void initializeStore(ReloRdfRepository repo, IProgressMonitor monitor, final boolean isUserAction) {
		// AtxaJDTBuilder.initRepository used to try this, but we should really
		// delete the entire file to keep things clean ... also we are progress
		// monitor compliant

		logger.info("Initializing Store...");
		
		// we really do need to delete the store to keep things clean
		// UGLY: likely reorganizable
		// we are making sure the other jobs are done by using a scheduling rule
		// on the entire workspace, at worst we want it on the store, but we
		// likely want to organize this better
		Job forceSequentialJob = new Job("Deleting Index and Reconnecting") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				if (BuildStatus.getClientDisabled() ){//|| !AccountStatusUtils.testAccountValid()) {
					// disabled - ask to enable 
					// if no - do nothing
					// if yes - test if valid
					// if valid - do build
					// if invalid - warn
					
					// EXCEPTION: if is running on startup it was not user initiated so do not show dialog
					if (!isUserAction) return Status.OK_STATUS;
					
					boolean enabled = new SyncExecQuestionDialog().runDialog("Architexa RSE - Account Disabled", 
							"You disabled your Architexa client. \nEnable to rebuild?");
					if (enabled) {
						reInitStore(monitor);
					} else return Status.OK_STATUS;
					
				} else
					reInitStore(monitor);
				return Status.OK_STATUS;
			}

		};
		forceSequentialJob.setUser(false);
		
		forceSequentialJob.schedule();

	}
	
	public static class SyncExecQuestionDialog {
		private boolean questionAnswer;
		public boolean runDialog(final String title, final String text) {
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					questionAnswer = MessageDialog.openQuestion(null, title, text);
				}
			});
			return questionAnswer;
		}
	};
	
	private static void reInitStore(IProgressMonitor monitor) {
		logger.info("Deleting Store...");
		StoreUtil.deleteDefaultStoreRepository();
		ResourceQueue.clear();
		try {
			reinit(monitor);
		} catch (Throwable t) {
			logger.error("Unexpected Exception", t);
		}
	}

	private static void reinit(IProgressMonitor monitor) throws CoreException {
		ReloRdfRepository repo = StoreUtil.getDefaultStoreRepository();

		logger.info("Starting transaction...");
		repo.startTransaction();
		runInitializers(repo);
		repo.addStatement(repo.getDefaultURI(storeNS, builders), repo
				.getDefaultURI(storeNS, initialized), repo
				.getLiteral(getPluginVer()));
		repo.commitTransaction();
		logger.info("Committed transaction...");

		ResourceQueueManager.startSelectiveBuild();
	}

	private static void runInitializers(ReloRdfRepository repo) {
		List<Object> initializersLst = new ArrayList<Object>(10);
		StoreUtil.loadEclipseClasses(initializersLst, "com.architexa.store.initializer", "storeInitializer");
		for (Object initObj : initializersLst) {
			ReloStoreInitializer rsi = (ReloStoreInitializer) initObj;
			logger.info("Running initializer: " + rsi.getClass());
			rsi.initRepository(repo);
		}
	}

}
