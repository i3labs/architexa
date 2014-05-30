package com.architexa.store;

import org.apache.log4j.Logger;
import org.eclipse.ui.IStartup;


/**
 * We have the store initilization at startup (instead of making it lazy
 * initialization), thus having the benefit that we can do longer
 * initializations as needed when we upgrade to a new version of the plugin
 * 
 * @author vineet
 */
public class VersionBasedStoreInitializerService implements IStartup {
    static final Logger logger = ReloStorePlugin.getLogger(VersionBasedStoreInitializerService.class);
    //class currently does nothing
    
    
	public void earlyStartup() {
//		Job initializerJob = new Job("Architexa RSE Connection") {
//			@Override
//			protected IStatus run(IProgressMonitor monitor) {
//				try {
//					checkAndInitilizeStore(monitor);
//				} catch (Throwable t) {
//					logger.error("Unexpected exception", t);
//				}
//				return Status.OK_STATUS;
//			}
//		};
//		initializerJob.setPriority(Job.LONG);
//		initializerJob.schedule();
	}
	
//	// we might want to someday expose this to the 'debugging public'
//	private boolean forcedReinit = false;

//	private void checkAndInitilizeStore(IProgressMonitor monitor) {
//		ReloRdfRepository repo = StoreUtil.getDefaultStoreRepository();
//        if (!storeInitialized(repo) || forcedReinit) initializeStore(repo, monitor);
//        
//        // no need to keep the repository open if we are not accessing them
//        //
//        // not enabled since we currently can't really ensure that all the
//		// previous build jobs have finished building, though we currently have
//		// a better records with cleaning than with building
//        //StoreUtil.shutdownDefaultRepository();
//	}

//    public final static String storeNS = "store#";
//    private static final String builders = "builders";
//    private static final String initialized = "initialized";

//    private static boolean storeInitialized(ReloRdfRepository repo) {
//    	Resource bulderRes = repo.getDefaultURI(storeNS, builders);
//    	URI initializedURI = repo.getDefaultURI(storeNS, initialized);
//        return repo.hasStatement(bulderRes, initializedURI, repo.getLiteral(getPluginVer()));
//    }
    
//    private static final String getPluginVer() {
//    	// place to change to cause a rebuild for new releases
//        //return ReloStorePlugin.getDefault().getBundle().getHeaders().get("Bundle-Version").toString();
//		// updating on every release might be too much - lets slow things down
//		// by versioning the db
//    	return "1";
//    }

//    private static void initializeStore(ReloRdfRepository repo, IProgressMonitor monitor) {
//    	// AtxaJDTBuilder.initRepository used to try this, but we should really
//		// delete the entire file to keep things clean ... also we are progress
//		// monitor compliant
//    	
//    	logger.info("Initializing Store...");
//    	
//		final List<IProject> projectsToReconnect = new ArrayList<IProject>();
//		ReloJavaConnectionUtils.getConnectedProjects(projectsToReconnect);
//
//		ReloJavaConnectionUtils.disconnectFromJavaProjects(projectsToReconnect, monitor);
//		
//		// we really do need to delete the store to keep things clean 
//        // UGLY: likely reorganizable
//        // we are making sure the other jobs are done by using a scheduling rule
//		// on the entire workspace, at worst we want it on the store, but we
//		// likely want to organize this better
//        Job forceSequentialJob = new Job("Deleting Index and Reconnecting") {
//			@Override
//			protected IStatus run(IProgressMonitor monitor) {
//		    	logger.info("Deleting Store...");
//				StoreUtil.deleteDefaultStoreRepository();
//
//				reinitAndReconnect(projectsToReconnect, monitor);
//
//				return Status.OK_STATUS;
//			}
//
//        };
//		forceSequentialJob.setRule(ResourcesPlugin.getWorkspace().getRoot());
//		forceSequentialJob.schedule();
//    }

//	private static void reinitAndReconnect(final List<IProject> projectsToReconnect, IProgressMonitor monitor) {
//		ReloRdfRepository repo = StoreUtil.getDefaultStoreRepository();
//
//		logger.info("Starting transaction...");
//        repo.startTransaction();
//        runInitializers(repo);
//        repo.addStatement(
//        		repo.getDefaultURI(storeNS, builders),
//        		repo.getDefaultURI(storeNS, initialized),
//        		repo.getLiteral(getPluginVer()));
//        repo.commitTransaction();
//		logger.info("Committed transaction...");
//        
//        ReloJavaConnectionUtils.connectToJavaProjects(projectsToReconnect, monitor);
//	}
	
//	private static void runInitializers(ReloRdfRepository repo) {
//        List<Object> initializersLst = new ArrayList<Object> (10);
//        StoreUtil.loadEclipseClasses(initializersLst, "com.architexa.store.initializer", "storeInitializer");
//        for (Object initObj : initializersLst) {
//            ReloStoreInitializer rsi = (ReloStoreInitializer) initObj;
//            logger.info("Running initializer: " + rsi.getClass());
//            rsi.initRepository(repo);
//		}
//	}
}
