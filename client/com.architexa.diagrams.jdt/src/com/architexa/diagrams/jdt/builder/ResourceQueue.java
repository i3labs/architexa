package com.architexa.diagrams.jdt.builder;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;

import com.architexa.diagrams.jdt.Activator;

/**
 * Class describes the Job that processes resources
 */
public class ResourceQueue {
	static final Logger logger = Activator.getLogger(ResourceQueue.class);
	
	private static Map<IProject, Set<ResourceToProcess>> perProjResQueue = new LinkedHashMap<IProject, Set<ResourceToProcess>>();
	public static Map<IProject, Set<ResourceToProcess>> cachedProjResQueue = new LinkedHashMap<IProject, Set<ResourceToProcess>>();
	
	private static List<IProject> projectQueue = new ArrayList<IProject>();

    public static void addResourceToProcess(ResourceToProcess rtp) {
		synchronized (getPerProjResQueue()) {
			IProject project = rtp.resource.getProject();
			if (!project.isOpen() || !project.exists()) return;
			Set<ResourceToProcess> resNameToResources = getPerProjResQueue().get(project);
			Set<ResourceToProcess> cachedNameToResources = cachedProjResQueue.get(project);
			if (resNameToResources == null) {
				Comparator<ResourceToProcess> comparatorStr = new Comparator<ResourceToProcess>() {
					public int compare(ResourceToProcess arg0,
							ResourceToProcess arg1) {
						return arg0.toString().compareToIgnoreCase(arg1.toString());
					}
				};
				// using a TreeSet to optimize performance 
				// when processing packages as the order of resources 
				// matter and we don't want any duplicates.
				getPerProjResQueue().put(project, new TreeSet<ResourceToProcess>(comparatorStr));
				cachedProjResQueue.put(project, new TreeSet<ResourceToProcess>(comparatorStr));
				if (!projectQueue.contains(project))
					projectQueue.add(project);
				resNameToResources = getPerProjResQueue().get(project);
				cachedNameToResources = cachedProjResQueue.get(project); 
			}
			resNameToResources.add(rtp);
			cachedNameToResources.add(rtp);
		}
	}
    
    public static void removeBuiltResourcesFromProject(IProject project, Set<ResourceToProcess> resSet) {
    	Set<ResourceToProcess> origResList = getPerProjResQueue().get(project);
    	for (ResourceToProcess res : resSet)
    		origResList.remove(res);
    }
    
	//public List<IProject> getProjectQueue() {
	//	return projectQueue;
	//}
    
    public static long getPercentageOfResourcesToProcess(long percentage) {
    	long totalSize = 0;
    	for (IProject proj :getPerProjResQueue().keySet()) {
    		totalSize += getPerProjResQueue().get(proj).size();
    	}
    	if (totalSize == 0)
    		return 0;
    	return (percentage * totalSize)/ 100;
    }
    
    public static Map<IProject, Set<ResourceToProcess>> getResourcesToProcess() {
    	long totalResSize = getPercentageOfResourcesToProcess(10);
    	if (totalResSize < 50) totalResSize = 50;
    	long curResSize = 0;
    	Map<IProject, Set<ResourceToProcess>> resProjMap = new LinkedHashMap<IProject, Set<ResourceToProcess>>();
    	for (IProject proj : new ArrayList<IProject>(projectQueue)) {
    		Set<ResourceToProcess> resSet = getPerProjResQueue().get(proj);
    		// project was closed or projectQueue was
			// otherwise corrupted: we should make a
			// note and then remove it from the map so
			// we do not continue producing errors
    		if (resSet == null) { 
    			logger.error("Project " + proj.toString() + " not found in the projectResource map.");
    			ResourceQueue.removeProj(proj);
    			continue;
    		}
    		Set<ResourceToProcess> resList = new HashSet<ResourceToProcess>(resSet);
    		curResSize  += resList.size();
    		
    		if (curResSize < totalResSize) {
    			resProjMap.put(proj, resList);
    			continue;
    		}
    		
    		int leftSize = (int) (totalResSize - (curResSize - resList.size()));
    		if (leftSize > 0) {
    			List<ResourceToProcess> list = new ArrayList<ResourceToProcess>(resList);
    			resProjMap.put(proj, new HashSet<ResourceToProcess>(list.subList(0, leftSize)));
    		}
    		// we break because we only want to process x resources at a time (totalResSize) 
    		break;
    	}
    	
    	// We send the whole resource even if it is more than 10% of the queue
    	return resProjMap;
    }
    
    public static boolean isEmpty() {
    	return getPerProjResQueue().isEmpty();
    }
    
    public static void insertProj(IProject proj) {
    	getPerProjResQueue().put(proj, new TreeSet<ResourceToProcess>());
    	cachedProjResQueue.put(proj, new HashSet<ResourceToProcess>());
    	if (!projectQueue.contains(proj))
    		projectQueue.add(proj);
    }
    
    public static void removeProj(IProject proj) {
    	getPerProjResQueue().remove(proj);
   		projectQueue.remove(proj);
    }
    
    public static IProject nextProj() {
    	return getPerProjResQueue().keySet().iterator().next();
    }
    
    public static Collection<ResourceToProcess> getResources(IProject proj) {
    	Set<ResourceToProcess> resNameToResources = getPerProjResQueue().get(proj);
		return resNameToResources;
    }

	public static void clear() {
		perProjResQueue = new LinkedHashMap<IProject, Set<ResourceToProcess>>();
		cachedProjResQueue = new LinkedHashMap<IProject, Set<ResourceToProcess>>();
	}

	public static int getTotalTaskSizeFromCachedQueue() {
		int count = 0;
    	Map<IProject, Set<ResourceToProcess>> perProjResQueue = ResourceQueue.cachedProjResQueue;
    	for (IProject proj : perProjResQueue.keySet())
    		count += perProjResQueue.get(proj).size();
    	return count;
    }

	private static Map<IProject, Set<ResourceToProcess>> getPerProjResQueue() {
		return perProjResQueue;
	}
	public static Set<IProject> getProjects() {
		return perProjResQueue.keySet(); // how is this different from projectQueue?
	}
	public static Object getQueueSynchObject() {
		return perProjResQueue;
	}

	public static void persistResources(BufferedWriter out, IProject proj) throws IOException {
		Set<ResourceToProcess> toSave = new HashSet<ResourceToProcess>(ResourceQueue.getPerProjResQueue().get(proj));
		while (!toSave.isEmpty()) {
			ResourceToProcess res = toSave.iterator().next();
			String resLoc = res.resource.getFullPath().toString();
			String toWrite = 
					resLoc + " " + 
					Boolean.toString(res.remove) + " " + 
					Boolean.toString(res.add);
			out.write(toWrite);
			out.newLine();

			toSave.remove(res);
		}
		toSave.clear();
	}
}
