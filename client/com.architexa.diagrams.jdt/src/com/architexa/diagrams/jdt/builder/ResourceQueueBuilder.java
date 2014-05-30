package com.architexa.diagrams.jdt.builder;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.openrdf.model.Resource;

import com.architexa.diagrams.jdt.Activator;
import com.architexa.diagrams.jdt.builder.PluggableExtensionBuilderSupport.IAtxaExtensionBuilder;
import com.architexa.diagrams.jdt.builder.asm.PackageBasedRDFBuildProcessor;
import com.architexa.diagrams.jdt.compat.ASTUtil;
import com.architexa.diagrams.jdt.services.PluggableBuildProcessor;
import com.architexa.intro.AtxaIntroPlugin;
import com.architexa.intro.BuildOffReminderDialog;
import com.architexa.intro.preferences.PreferenceConstants;
import com.architexa.rse.BuildStatus;
import com.architexa.rse.RSE;
import com.architexa.store.ReloRdfRepository;
import com.architexa.store.StoreUtil;

public class ResourceQueueBuilder extends Job {
	static final Logger logger = Activator.getLogger(ResourceQueueBuilder.class);
	
	public static final Object AtxaBuildFamily = ResourcesPlugin.FAMILY_AUTO_BUILD;

	public static final String ReloBuildJobName = "Indexing For " + RSE.appName + " ";
	// public static final String ReloCleanJobName = "Cleaning For " + RSE.appName + " ";
	
	public static Job resourceBuilder = null;

	private final AtxaBuildVisitor builderResVisitor;

	public boolean projNeedsProcessing = false;
	public static boolean showBuild = false;
	public static boolean isSilentBuild = false;
	
	
	private static void setSilentBuild(boolean val) {
		isSilentBuild = val;
	}
	
	public static synchronized void startBuildJob(boolean _showBuild, boolean isSilentCall, boolean resetDACSS) {
		if (jobInProgress()) return;
		
		IWorkspaceDescription description = ResourcesPlugin.getWorkspace().getDescription();
		if (!description.isAutoBuilding()
				&& AtxaIntroPlugin.getDefault().getPreferenceStore()
						.getBoolean(PreferenceConstants.BUILD_OFF_REMINDER_KEY)) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					new BuildOffReminderDialog(PlatformUI.getWorkbench()
							.getActiveWorkbenchWindow().getShell()).open();
				}
			});
		}
		ReloRdfRepository reloRepo = StoreUtil.getDefaultStoreRepository();
		AtxaBuildVisitor builderResVisitor = new AtxaBuildVisitor(reloRepo, resetDACSS);
		showBuild = _showBuild;
    	resourceBuilder = new ResourceQueueBuilder(ReloBuildJobName, builderResVisitor);
    	
    	// if setting is silent then set in resourceBuilder
    	setSilentBuild(isSilentCall);

   		if (_showBuild) {
    		// fullBuild: pop up progress dialog
    		// (otherwise just show in progress view, i.e. the default)
    		for (AtxaRDFBuildProcessor proc : PluggableBuildProcessor.getRegisteredProcessors()){
    			if (proc instanceof PackageBasedRDFBuildProcessor)
    				((PackageBasedRDFBuildProcessor) proc).resetCachedPckg();
    		}
    		resourceBuilder.setUser(true);
    		resourceBuilder.setSystem(false);
    	} else {
    		resourceBuilder.setUser(false);
    		resourceBuilder.setSystem(true);
    		
    		// for testing
    		// resourceBuilder.setUser(true);
    		// resourceBuilder.setSystem(false);
    		// resourceBuilder.setName("Part Build");
    	}
    	resourceBuilder.setPriority(Job.DECORATE);
    	resourceBuilder.schedule();
	}
	
	public static synchronized void startBuildJob(boolean _showBuild, boolean isSilentCall) {
		startBuildJob(_showBuild, isSilentCall, true);
	}
	
	public static boolean jobInProgress() {
		// If the last build was interrupted, theProjs could potentially be non-empty
		// even though no build is running or is scheduled to run. This means we also 
		// need to check that a build job is actually still in progress or else 
		// we'll wait indefinitely for curProj to finish building (which will never
		// happen since no build is or will be running).
		return (resourceBuilder != null && 
				Job.getJobManager().find(AtxaBuildFamily).length>0);
	}

	/**
	 * If there are resources to be processed but no job running returns true
	 */
	public static boolean jobNeedsStart() {
		// TODO: shouldn't this be identical to the above method?
		if (resourceBuilder != null) return false;
		
		if (BuildStatus.getClientDisabled()) return false;
		
		synchronized (ResourceQueue.getQueueSynchObject()) {
			return !ResourceQueue.isEmpty();
		}
	}

	public ResourceQueueBuilder(String name, AtxaBuildVisitor builderResVisitor) {
		super(name);
		this.builderResVisitor = builderResVisitor;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		// logger.info("Starting Build Job");
		try {
			long buildStart = System.currentTimeMillis();

			logger.info("Build Job - Performing Build");

			monitor.beginTask("Indexing Java Files.", builderResVisitor.getTaskSize());
			builderResVisitor.setMonitor(monitor);
			
			// if we are doing a smart/silent build process resources 10 at a time, otherwise process all of them.
			if (isSilentBuild)
				processResuorcesIncrementally();
			else
				processResources();

			long buildEnd = System.currentTimeMillis();

			// Only useful for build time and last project now
			logger.info("Build Job - Time: " + (buildEnd - buildStart) / 1000
					+ "s: Processed: " + builderResVisitor.taskDone + "/"
					+ builderResVisitor.taskSize);
			
			// only save stats for build if we are doing a full build
			if (showBuild){
				BuildStatus.addClientInfo("Build Size", builderResVisitor.getTaskSize());
				BuildStatus.addClientInfo("Build Time", (int) ((buildEnd - buildStart) / 1000));
			}
			
			//logger.info("Build processed: " + builderResVisitor.taskDone + " of " + builderResVisitor.taskSize);
			
			monitor.done();
		} catch (Throwable t) {
			logger.error("Unexpected exception", t);
		} finally {
			// make sure since this thread is going to end that jobRunning is
			// false so a new job can be started, only an issue if exception is thrown
			resourceBuilder = null;
		}
		// logger.info("Done Build Job");
		return Status.OK_STATUS;
	}

	@Override
	public boolean belongsTo(Object family) {
		if (family == AtxaBuildFamily)
			return true;
		else
			return false;
	}

	/////////////////////////////////////////////
	//// Do the actual processing of resources //
	/////////////////////////////////////////////

	
	/**
	 * Go through each project in theProjs and process resources associated with
	 * the projects as needed
	 */
	private void processResources() {
		// need a call here for repository build issues
		IProject currProj = getProjectForProcessing(null, true);
		while (currProj != null) {
			// Here we are checking if the project we are processing from the queue is selected to be built in the preferences
			// Alternatively we could check this when the resources are being added to the queue, this may save memory
			boolean projIsSelectedInPref = ResourceQueueManager.isProjectSelectedForBuild(currProj.getName());
			if (projIsSelectedInPref )
				processProjectResources(currProj, ResourceQueue.getResources(currProj));

			if (resourceBuilder == null) return;

			// make sure more resources need to be processed
			currProj = getProjectForProcessing(currProj, projIsSelectedInPref);
			if (currProj == null) resourceBuilder = null;
		}
	}

	
	private void processResuorcesIncrementally() {
		synchronized (ResourceQueue.getQueueSynchObject()) {
			if (ResourceQueue.isEmpty()) return;
			Map<IProject, Set<ResourceToProcess>> resProjMap = ResourceQueue.getResourcesToProcess();
			for (IProject proj : resProjMap.keySet()) {
				if (builderResVisitor.isTaskCancelled()) return;
				builderResVisitor.initProject(proj);
				
				// Here we are checking if the project we are processing from the queue is selected to be built in the preferences
				// Alternatively we could check this when the resources are being added to the queue, this may save memory
				boolean projIsSelectedInPref = ResourceQueueManager.isProjectSelectedForBuild(proj.getName());
				if (projIsSelectedInPref )
					processProjectResources(proj, new HashSet<ResourceToProcess>(resProjMap.get(proj)));
				
				ResourceQueue.removeBuiltResourcesFromProject(proj, resProjMap.get(proj));			
				Collection<ResourceToProcess> currProjResources = ResourceQueue.getResources(proj);
				if (currProjResources.isEmpty() || !projIsSelectedInPref) {
					projNeedsProcessing = builderResVisitor.finishProcessing();
					if (!projNeedsProcessing) ResourceQueue.removeProj(proj);
				} 
			}
		}
	}
	
	/**
	 * See if anything left to process with prevProj, if not return something
	 * else for the builder
	 * @param projIsSelectedInPref 
	 */
	private IProject getProjectForProcessing(IProject prevProj, boolean projIsSelectedInPref) {
		synchronized (ResourceQueue.getQueueSynchObject()) {
			if (builderResVisitor.isTaskCancelled()) return null;
			
			// do we need to do more processing of prevProj?
			if (projIsSelectedInPref) {
				Collection<ResourceToProcess> prevProjResources = ResourceQueue.getResources(prevProj);
				if (prevProjResources != null && !prevProjResources.isEmpty()) return prevProj;
			}
			
			// wrap up prevProj to find another one
			if (prevProj != null) 
				projNeedsProcessing = builderResVisitor.finishProcessing();
			
			if (!projNeedsProcessing) ResourceQueue.removeProj(prevProj);
			
			if (ResourceQueue.isEmpty()) return null;

			// which doesn't matter, just need a project
			IProject nextProj = ResourceQueue.nextProj();
			builderResVisitor.initProject(nextProj);
			return nextProj;
		}
	}

	/**
	 * Has builderResVisitor process the resource associated with each member of
	 * the resources ArrayList
	 * @param currProjResources 
	 */
	private void processProjectResources(IProject currProj, Collection<ResourceToProcess> currProjResources) {
		if (currProjResources == null || currProjResources.isEmpty()) return;
		
		if (builderResVisitor.isTaskCancelled()) return;
		
		builderResVisitor.setTaskSize(builderResVisitor.taskDone + currProjResources.size(), currProj.getName());
		logger.info("Processing: " + currProj.getName() + " with " + currProjResources.size() + " task(s)");

		while (!currProjResources.isEmpty()) {
			ResourceToProcess rtp = null;
			if (builderResVisitor.isTaskCancelled()) return;
			try {
				// Only needs to lock resourcesMap 
				synchronized (currProjResources) {
					rtp = currProjResources.iterator().next();
					currProjResources.remove(rtp);
				}
				if (rtp != null && rtp.resource.exists())
					processResource(rtp);
			} catch (NoSuchElementException e) {
				// will be thrown when no resources are left
				break;
			} catch (Throwable t) {
	    		if (rtp != null && rtp.resource != null)
	        		logger.error("Unexpected Error While Building: " + rtp.resource + " class : " + rtp.resource.getClass(), t);
				else
					logger.error("Unexpected Error While Building: (null)", t);
			}
		}
	}

	private void processResource(ResourceToProcess rtp) {
		if (builderResVisitor.isTaskCancelled()) return;

		if (rtp.resource.getType() == IResource.PROJECT) {
			Collection<PluggableExtensionBuilderSupport.IAtxaExtensionBuilder> allExtBuilders = PluggableExtensionBuilderSupport.getAllBuilders();
			for (PluggableExtensionBuilderSupport.IAtxaExtensionBuilder builder : allExtBuilders) {
				builder.processProject(builderResVisitor, (IProject) rtp.resource);
			}
		}

		// process files
		
    	//// for scheduling archives we need to do something like:
		//name = new ZipFile(rtp.resource.getRawLocation().toOSString()).getName();

		// Remove if the AST for this resource has been cached
		ASTUtil.removeFileFromCache(rtp.resource);
		Collection<IAtxaExtensionBuilder> resBuilders = PluggableExtensionBuilderSupport.getBuilderForExt(rtp.resource.getFileExtension());
		if (resBuilders != null) {
			for (IAtxaExtensionBuilder extBuilder : resBuilders) {
				if (builderResVisitor.isTaskCancelled()) return;
				List<Resource> classResList = extBuilder.processExtensionResource(builderResVisitor, rtp);
				if (classResList != null)
					for (Resource classRes : classResList){
						if (builderResVisitor.isTaskCancelled()) return;
						builderResVisitor.getRepo().startTransaction();
						builderResVisitor.runProcessors(classRes, false);
						builderResVisitor.getRepo().commitTransaction();
					}
			}
		}

		builderResVisitor.incTaskDone();
    }

	public void restartBuildInProgress() {
		this.cancel();
		if (resourceBuilder!=null) resourceBuilder.cancel();
		builderResVisitor.monitor.done();
		ResourceQueueManager.terminate();
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();

		ResourceQueueManager.loadProjects(root);
		ResourceQueueManager.loadResources(root);
		if (ResourceQueueBuilder.jobNeedsStart()) {
			ResourceQueueBuilder.startBuildJob(true, false);
		}
	}

}
