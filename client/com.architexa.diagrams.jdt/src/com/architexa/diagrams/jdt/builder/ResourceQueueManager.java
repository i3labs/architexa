package com.architexa.diagrams.jdt.builder;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.Timer;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.PlatformUI;

import com.architexa.diagrams.jdt.Activator;
import com.architexa.diagrams.jdt.RJMapFromId;
import com.architexa.diagrams.jdt.actions.ReinitRepository;
import com.architexa.diagrams.jdt.actions.UpdateRepository;
import com.architexa.diagrams.jdt.builder.asm.ClassExtensionBuilder;
import com.architexa.diagrams.jdt.compat.ASTUtil;
import com.architexa.diagrams.ui.ArchitexaMenuAction;
import com.architexa.intro.AtxaIntroPlugin;
import com.architexa.rse.BuildSettings;
import com.architexa.rse.BuildStatus;
import com.architexa.store.ReloStorePlugin;

/**
 * This class deals with saving information on shut down about resources that
 * still need to be processed.
 * 
 * This class deals with loading information on startup about resources that
 * still need to be processed and starting their processing
 */

public class ResourceQueueManager implements IStartup {
	static final Logger logger = Activator.getLogger(ResourceQueueManager.class);

	public void earlyStartup() {
		try {
			earlyStartupInternal();
		} catch (Throwable t) {
			logger.error("Unexpected Error", t);
		}
	}
	private void earlyStartupInternal() {
//		PluggableExtensionBuilderSupport.registerExtensionBuilder(AtxaBuildVisitor.ReloExt, new ReloExtensionBuilder());
		PluggableExtensionBuilderSupport.registerExtensionBuilder(AtxaBuildVisitor.ClassExt, new ClassExtensionBuilder());
		
		ReinitRepository.init(ArchitexaMenuAction.registerAction(new ReinitRepository()));
		UpdateRepository.init(ArchitexaMenuAction.registerAction(new UpdateRepository()));

		// check every hour if we are doing a daily build and it it the correct hour
		initBuildClock();
		
		VersionedRepositoryCheckAndInitializer.launchJobToCheckAndInitilizeStore();

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		// we need to add two different resourceChangeListeners as the default
		// one does not add a listener for PRE_BUILD events
		workspace.addResourceChangeListener(new IResourceChangeListener() {
			public void resourceChanged(IResourceChangeEvent event) {
				try {
					if (ResourceQueueBuilder.jobInProgress()) return;
					
					queueAndStartBuild(event.getDelta(), false);
				} catch (Throwable e) {
					logger.error("Unexpected exception", e);
				}
			}}, IResourceChangeEvent.POST_BUILD);
		
		// try to get clean/full build requests
		workspace.addResourceChangeListener(new IResourceChangeListener() {
			public void resourceChanged(IResourceChangeEvent event) {
				try {
					// Collect resources even if build is disabled so that we
					// can do an 'update' later

					// if (AccountStatus.getClientDisabled()) {
					//	// do not gather resources if doing a clean while disabled
					//	logger.info("Client Disabled: Do Not Build");
					//	return;
					// }
					if (ResourceQueueBuilder.jobInProgress()) return;
					boolean fullBuild = false;
					if (event.getBuildKind() == IncrementalProjectBuilder.CLEAN_BUILD
							|| event.getBuildKind() == IncrementalProjectBuilder.FULL_BUILD)
						fullBuild = true;
					if (fullBuild && !BuildStatus.getClientDisabled())
						startSelectiveBuild();
				} catch (Throwable e) {
					logger.error("Unexpected exception", e);
				}
			}}, IResourceChangeEvent.PRE_BUILD);

		/** Load resources that need processing if any */
		loadProjects(root);
		loadResources(root);
		if (ResourceQueueBuilder.jobNeedsStart() && BuildSettings.checkBuildSchedule()) {
			ResourceQueueBuilder.startBuildJob(false, false);
		}
	}

	private static ResourceQueueManager instance = null;
	
	public static void initBuildClock() {
		if (instance == null) {
			instance = new ResourceQueueManager();
			instance.setupClock();
			instance.setupSmartBuildClock();
		}
	}

	private static int clockFrequency = 1 /*hr*/ * 60 /*min*/ * 60 /*sec*/ * 1000;

	private void setupClock() {
		Timer timer = new Timer(clockFrequency, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				clockTick();
			}
		});
		timer.start();
	}

	int clockFreq = 15 /*min*/ * 60 /*sec*/ * 1000;
	protected static int errorDialogClosed = 0;
	private void setupSmartBuildClock() {
		Timer timer = new Timer(clockFreq, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				smartBuildClockTick();
			}
		});
		timer.start();
	}
	
	protected void smartBuildClockTick() {
		new Thread(new Runnable() {
			public void run() {
				if (BuildSettings.isSilentBuild())
					ResourceQueueBuilder.startBuildJob(false, true, false);
			}}).start();
	}
	
	public static void clockTick() {
		new Thread(new Runnable() {
			public void run() {
				if (BuildSettings.isDailyBuild() && BuildSettings.checkBuildTime())
					ResourceQueueBuilder.startBuildJob(true, false, false);
			}}).start();
	}
	
	
	private static final String persistProjectsFileName = "projs.tmp";
	private static final String persisResourcesFileName = "resources.tmp";

	private static String persistLocation;

	private static String getPersistLoc() {
		try {
			if (persistLocation == null) 
				persistLocation = ReloStorePlugin.getDefault().getStateLocation().addTrailingSeparator().toString();
		} catch (Throwable t) { 
			logger.error("Unexpected Exception", t);
			
			// this should not happen, but just in case use a temporary directory
			persistLocation = System.getProperty("java.io.tmpdir");
		}
		return persistLocation;
	}

	/**
	 * creates projs.tmp in appropriate directory, each line represent a project
	 * that needs work still form each project is
	 * " (location of IProject relevant to workspace)"
	 */
	private static void persistProjects() {
		String projLoc = getPersistLoc() + persistProjectsFileName;
		try {
			BufferedWriter outProjs = new BufferedWriter(new FileWriter(projLoc));
			synchronized (ResourceQueue.getQueueSynchObject()) {
				for (IProject proj : ResourceQueue.getProjects()) {
					String loc = proj.getFullPath().toString();
					// Space is used in loading to differentiate from an empty line
					outProjs.write(" " + loc); 
					outProjs.newLine();
				}
			}
			outProjs.close();
		} catch (IOException e) {
			logger.error("IO issue writing information on projects: ", e);
		}
	}

	/**
	 * creates a file called fileName in appropriate directory, each line
	 * represents a resource form for each resource is"(name of resource) (location of resource relevant to workspace) (toRemove) (toAdd)"
	 */
	private static void persistResources() {
		String resourceLoc = getPersistLoc() + persisResourcesFileName;
		try {
			BufferedWriter outResources = new BufferedWriter(new FileWriter(resourceLoc));
			synchronized (ResourceQueue.getQueueSynchObject()) {
				for (IProject proj : ResourceQueue.getProjects()) {
					ResourceQueue.persistResources(outResources, proj);
				}
			}
			outResources.close();
		} catch (IOException e) {
			logger.error("IO issue writing information on classes to be processed: ",e);
		}
	}

	/* Make sure if job running it will stop, then save still flagged resources */
	public static void terminate() {
		ResourceQueueBuilder.resourceBuilder = null;

		/** Dump information about resources that need to be processed to disk */
		try {
			persistProjects();
			persistResources();
		} catch (Throwable e) {
			logger.error("Unexpected Error writing information on projects: ", e);
		}
	}

	/** Load projects that have processing work needed */
	public static void loadProjects(IWorkspaceRoot root) {
		String projsLoc = getPersistLoc() + persistProjectsFileName;
		File projsSaved = new File(projsLoc);
		try {
			if (projsSaved.exists()) {
				BufferedReader inProjs = new BufferedReader(new FileReader(projsLoc));
				String theLine = inProjs.readLine();

				// blank line at the end so need to end on that
				while (theLine != null && theLine.indexOf(" ") != -1) {

					// form of line is " location"

					// Make sure resource is there
					IPath path = new Path(theLine.substring(1));
					IProject proj = (IProject) root.findMember(path);
					if (proj != null && proj.exists() && proj.isOpen())
						ResourceQueue.insertProj(proj);

					// possibly another proj to load
					theLine = inProjs.readLine();
				}

				inProjs.close();
				projsSaved.delete();
			}
		} catch (Exception e) {
			logger.error("Issue loading up flagged class files: ", e);
		}
	}

	/**
	 * Load resources of resourceType from file fileName that need to be
	 * processed
	 */
	public static void loadResources(IWorkspaceRoot root) {

		String resourcesLoc = getPersistLoc() + persisResourcesFileName;
		File resourcesSaved = new File(resourcesLoc);
		try {
			if (resourcesSaved.exists()) {
				BufferedReader inResources = new BufferedReader(new FileReader(resourcesLoc));
				String theLine = inResources.readLine();

				// blank line at the end so need to end on that
				while (theLine != null && theLine.indexOf(" ") != -1) {

					// form of line is "location remove add"
					// (location is workspace relative)
					String[] splitLine = split(theLine);
					if (splitLine.length != 3) {
						logger.error("Unexpected format locating from build queue: " + theLine);
						// go to next line or else we'll infinitely loop on the problematic line
						theLine = inResources.readLine(); 
						continue;
					}
						
					String resource = splitLine[0];
					boolean remove = Boolean.parseBoolean(splitLine[1]);
					boolean add = Boolean.parseBoolean(splitLine[2]);
					// Make sure resource is there
					IResource res = root.findMember(new Path(resource));
					if (res != null)
						ResourceQueue.addResourceToProcess(new ResourceToProcess(res, remove, add));

					// possibly another class to load
					theLine = inResources.readLine();
				}

				inResources.close();
				resourcesSaved.delete();
			}
		} catch (Exception e) {
			logger.error("Issue loading from " + getPersistLoc() + persisResourcesFileName + ": ", e);
		}
	}

	/* Form of given line is "location(workspace relative) remove add".
	 * We could have a location that contains spaces, such as 
	 * "/My Project/bin/ClassC.class true false", so we cannot simply 
	 * call theLine.split(" "). 
	 * @returns an array of 3 Strings representing location, remove, and
	 * add, where the location entry in the array may include spaces
	 */
	private static String[] split(String theLine) {
		String[] splitLine = theLine.split(" ");
		int length = splitLine.length;
		if(length==3) return splitLine; // proj name contained no spaces
		if(length<3) return new String[]{}; // something wrong with the line

		String remove = splitLine[length-2];
		String add = splitLine[length-1];

		// remove and add should be booleans
		// if they aren't, something wrong with the line
		if((!remove.equals("true") && !remove.equals("false")) ||
				(!add.equals("true") && !add.equals("false"))) return new String[]{};

		String location = "";
		for(int i=0; i<length-2; i++) {
			location = location+splitLine[i]+" ";
		}

		return new String[] {location.trim(), remove, add};
	}
	
	// This is a map from all projects to all the children packages that are unchecked
	// it is generated during the build from the list of unchecked items in the preferences
	private static Map<String, List<String>> filteredPackagesBuildMap;
	
	// This is a map from all projects to all the children packages with the same name that exist in separate src folders
	// it is generated during the build from the java model
	private static Map<String, List<String>> projToDuplicatedPackagesMap;  
	
	@SuppressWarnings("unchecked")
	public static void updateBuildMapFilters() {
		filteredPackagesBuildMap = new HashMap<String, List<String>>();
		projToDuplicatedPackagesMap = BuildSettings.findDuplicatedPackages();
		filteredPackagesBuildMap = BuildSettings.getStoredUnselectedProjPackgMap();
		
	}
	
	public static List<String> getUncheckedPackageListForProject(String projName) {
		if (filteredPackagesBuildMap == null) updateBuildMapFilters();
		return filteredPackagesBuildMap.get(projName);
	}
	
	public static Map<String, List<String>> getBuildPreferenceMap() {
		return new HashMap<String, List<String>>(filteredPackagesBuildMap);
	}
	
	public static boolean isProjectSelectedForBuild(String projName) {
		if (filteredPackagesBuildMap == null) updateBuildMapFilters();
		// If the user have not stored any preferences then the buildMap will
		// return empty as by default all the projects should be built
		List<String> packgList = filteredPackagesBuildMap.get(projName);
		// not present in list
		if (packgList == null) return true;
		// unchecked
		if (packgList.isEmpty()) return false;

		return true;
	}
	
	// adds all selected projects/packages from the preferences to the build queue
	public static void startSelectiveBuild () {
		updateBuildMapFilters();
		ASTUtil.emptyCache();
		RJMapFromId.emptyCache();

		List<IProject> projList = new ArrayList<IProject>();
		IWorkspace workspace = AtxaIntroPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		IWorkspaceDescription description = workspace.getDescription();
		String[] projectsInBuildOrder = description.getBuildOrder();
		if(projectsInBuildOrder!=null) {
			for(String projName : projectsInBuildOrder) {
				projList.add(root.getProject(projName));
			}
		} else { // workspace's default build order is being used
			// Build projects in order of their dependencies (a project 
			// should be built _after_ the projects it depends on)
			IWorkspace.ProjectOrder order = AtxaIntroPlugin.getWorkspace().computeProjectOrder(root.getProjects());
			projList.addAll(Arrays.asList(order.projects));
		}
		int cnt = 0;
		
		boolean emptyWkspc = false;
		if (projList.isEmpty())
			emptyWkspc = true;
		
		for (IProject proj : projList) {
			if (!proj.isOpen() || !isProjectSelectedForBuild(proj.getName())) 
				continue;
			try {
				cnt++;
				queueAndStartBuild(proj, true);
			} catch (CoreException e) {
				logger.error(e.getMessage());
			}
		}
		if (cnt == 0 && !emptyWkspc && errorDialogClosed == 0) { // only show this error when there are
										// actually projects in the workspace.
										// We dont want to show it when users
										// create a new workspace and install
										// architexa for the first time.
										// ALSO: do not show duplicates when we are cleaning multiple individual projects.
										// This fires multiple resource changed events so we need to be smart and not make the user click to close a bunch of dialogs 
			errorDialogClosed = -1;
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					if (PlatformUI.getWorkbench().getActiveWorkbenchWindow() == null) return;
					MessageDialog errorDialog = new MessageDialog(
							PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), 
							"Architexa RSE - Build Error",
							null, 
							"No packages have been selected for build. To select the packages to be built, please go to \"Eclipse Preferences > Architexa > Build Preferences.\" \n\n To disable the Architexa build, go to \"Architexa > Disable Build\"", 
							MessageDialog.ERROR, 
							new String[]{"OK"}, 
							0);
					errorDialogClosed = errorDialog.open();
				}});
		}
	}
	
	public static void queueAndStartBuild(Object buildTgt, boolean fullBuild) throws CoreException {
		// Update map when incremental build is called
		if (!fullBuild)	updateBuildMapFilters();
		
		if (buildTgt == null) 
			throw new IllegalArgumentException("Trying to build null object");

		if (buildTgt instanceof IWorkspace)
			buildTgt = ((IWorkspace)buildTgt).getRoot();

		if (buildTgt instanceof IWorkspaceRoot) {
			if (fullBuild) ((IWorkspaceRoot)buildTgt).refreshLocal(IResource.DEPTH_INFINITE, null);

			IProject[] projects = ((IWorkspaceRoot)buildTgt).getProjects();
			for (IProject project : projects) {
				if (!project.isOpen()) continue;	// ignore closed projects
				queueAndStartBuild(project, fullBuild);
			}
			return;
		}

		if (!(buildTgt instanceof IResource) && !(buildTgt instanceof IResourceDelta))
			throw new IllegalArgumentException("Don't know how to build type: " + buildTgt.getClass());

		ResourceDeltaVisitorForQueueing visitor = new ResourceDeltaVisitorForQueueing(fullBuild);
		if (buildTgt instanceof IResource)
			((IResource) buildTgt).accept(visitor);
		else if (buildTgt instanceof IResourceDelta)
			((IResourceDelta) buildTgt).accept(visitor);

		// We only want to build if the jobNeedsStart(resourceBuilder is not
		// null, client is not disabled, the queue is not empty) and the build
		// is either a user selected complete build or scheduled to run
		// instantly  
		if (ResourceQueueBuilder.jobNeedsStart() && (BuildSettings.checkBuildSchedule() || fullBuild )) {// Do not build if we are scheduled to build daily or on request
			ResourceQueueBuilder.startBuildJob(fullBuild, false);
		}
	}


    static Set<String> extensionsForScheduling = new HashSet<String> (5);
	//static {
	//	// not sure who even consumes these - as jar's and zip's seem to be
	//	// processed at the project level
	//	extensionsForScheduling.add("jar");
	//	extensionsForScheduling.add("zip");
	//}
    public static void addExtensionForScheduling(String ext) {
    	extensionsForScheduling.add(ext);
    }
	public static Map<String, List<String>> getDuplicatedPackageMap() {
		return projToDuplicatedPackagesMap;
	}
    
}
