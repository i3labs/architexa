/*
 * Created on Jan 7, 2005
 *
 */
package com.architexa.diagrams.jdt.builder;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;

import com.architexa.diagrams.jdt.Activator;
import com.architexa.diagrams.jdt.RJMapFromId;
import com.architexa.rse.BuildSettings;


/**
 * Used by ResourceQueueManager for iterating through Resources
 */
public class ResourceDeltaVisitorForQueueing implements IResourceDeltaVisitor, IResourceVisitor {
	static final Logger logger = Activator.getLogger(ResourceDeltaVisitorForQueueing.class);

	private final boolean fullBuild;
    
	public ResourceDeltaVisitorForQueueing(boolean fullBuild) {
		this.fullBuild = fullBuild;}
    
    /* (non-Javadoc)
     * @see org.eclipse.core.resources.IResourceDeltaVisitor#visit(org.eclipse.core.resources.IResourceDelta)
     */
    public boolean visit(IResourceDelta delta) throws CoreException {
        switch (delta.getKind()) {
        case IResourceDelta.REMOVED : 		
            visitResource(delta.getResource(), true, false);
            break;
        case IResourceDelta.ADDED :
        	visitResource(delta.getResource(), false, true);
            break;
        case IResourceDelta.CHANGED :
			visitResource(delta.getResource(), true, true);
            break;
        }
        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.resources.IResourceVisitor#visit(org.eclipse.core.resources.IResource)
     */
    //private static boolean debug = false;
    //private static String last = null;
	public boolean visit(IResource resource) throws CoreException {
		Map<String, List<String>> unselectedProjToPckgMap = ResourceQueueManager.getBuildPreferenceMap();
		Map<String, List<String>> dupPckgMap = ResourceQueueManager.getDuplicatedPackageMap();
			
		// Do we need anything from the sec folder??? Currently returning everything in src
		// YES, we need web.xml from src for spring support
		// if (resource.toString().contains("src")) return false;

		// if we are a file then we do not need to worry about filtering by
		// package. This would have been done before we reached the file level
		int type = resource.getType();
		if (type != IResource.FOLDER) {
			
			if (!findResInCorrectSrcFldr(resource, dupPckgMap))
				return false;
			
			visitResource(resource, false, true); 
	        return true;
		}
		
		for (String proj : unselectedProjToPckgMap.keySet()) {
			if (!resource.toString().contains(proj) ) continue; // different project, skip to next one

			String pckgResPath = getPckgStrFromIResource(resource, proj);
			// if we are an output package that may have come from a
			// duplicated package (one that exists in two separate src
			// folders) we need to be visited because we need to visit the
			// children to determine if any need to be filtered  
			List<String> dupPckgList = dupPckgMap.get(proj);
			if (dupPckgList!=null && pckgResPath!=null && dupPckgList.contains(pckgResPath.replace("/", "."))) {
				visitResource(resource, false, true); 
		        return true;
			}
			for (String pckg : unselectedProjToPckgMap.get(proj)) {
				String uncheckedPrefPath = pckg.replaceAll("\\.", "/");
				if (pckgResPath.equals(uncheckedPrefPath)) {
					return false; // do not add resource to queue, it has been unchecked in build prefs
				}
			}
		}
    	visitResource(resource, false, true); 
        return true;
    }

	/**
	 we might be a file that is in a duplicated package and we will
	 need to check which src folder we came from to properly filter
	
	 if this guys parent is in the dupPackg map we need to create an IJE and
	 figure out what src fld it exists in then check if it is being
	 filtered
	 
	 note: this may run slow due to the call to idToJdtElement since it traverses the whole workspace.
	 TODO: Optimize this so idToJDTElement is called as little as possible 
	 * @throws JavaModelException 
	 */
	private boolean findResInCorrectSrcFldr(IResource resource, Map<String, List<String>> dupPckgMap) throws JavaModelException {
		String projStr = resource.getProject().toString();
		
		if (projStr.startsWith("P/"))
			projStr = projStr.replace("P/", "");
		String parentPckgStr = getPckgStrFromIResource(resource.getParent(), projStr);//resource.getParent().toString();
		List<String> dupPckgList = dupPckgMap.get(projStr);
		parentPckgStr = parentPckgStr.replaceAll("/", ".");
		if (dupPckgList!=null && dupPckgList.contains(parentPckgStr)) {
			List<String> srcFolders = BuildSettings.itemToSrcMap.get(parentPckgStr);
			if (srcFolders != null) { 
				for (String unselectedSrcFldr : srcFolders) { 
					String resName = parentPckgStr+"$"+resource.getName();
					IJavaElement ijeInUnbuiltSrcFldr = RJMapFromId.idToJdtElement(resName, unselectedSrcFldr);
					if (ijeInUnbuiltSrcFldr == null) 
						continue;
					return false;
				}
			}
		} else {
			// check if we are in the itemToSrcMap anyway to make sure we shouldnt be filtered out
			List<String> srcFolders = BuildSettings.itemToSrcMap.get(parentPckgStr);
			if (srcFolders != null)
				return false;
		}
		return true;
	}

	private String getPckgStrFromIResource(IResource resource, String proj) {
		String res = resource.toString();
		if (!res.contains(proj)) return "";
		String fullPath = res.substring(res.indexOf(proj)+proj.length());
		String pckgResPath = fullPath;
//			String pathSrcFolder = null;
		if (fullPath.contains("/")) {
			fullPath = fullPath.replaceFirst("/","");
			if (fullPath.contains("/")) {
				fullPath = fullPath.replaceFirst("/", "::");
				String[] splitPath = fullPath.split("::");
	//				pathSrcFolder = splitPath[0];
				pckgResPath = splitPath[1];
			} else
				pckgResPath = fullPath;
		} 
		return pckgResPath;
	}
	
    private void visitResource(IResource resource, boolean rem, boolean add) throws JavaModelException {
    	if (fullBuild && resource instanceof IProject) {
			ResourceQueue.addResourceToProcess(new ResourceToProcess(resource, rem, add));
		}

		if (resource.getType() != IResource.FILE) return;

		if (resource.getFileExtension() != null && ResourceQueueManager.extensionsForScheduling.contains(resource.getFileExtension().toLowerCase())) {
			ResourceQueue.addResourceToProcess(new ResourceToProcess(resource, rem, add));
		}
    }

}

