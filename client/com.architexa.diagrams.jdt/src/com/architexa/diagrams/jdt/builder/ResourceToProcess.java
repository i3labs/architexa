package com.architexa.diagrams.jdt.builder;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import com.architexa.diagrams.jdt.Activator;

/**
 * Store information about a resource for use when the resource is processed
 */
public class ResourceToProcess implements Comparable<ResourceToProcess>{
	static final Logger logger = Activator.getLogger(ResourceQueue.class);

	public IResource resource;
	public boolean add;
	public boolean remove;

	public ResourceToProcess(IResource resource, boolean rem, boolean add) {
		this.resource = resource;
		this.add = add;
		this.remove = rem;
	}

    public String getName() {
    	if (resource instanceof IProject)
    		return resource.getName();
    	
    	// for rest return location
    	try {
        	IPath fileLoc = resource.getLocation();
    		fileLoc = fileLoc.removeFirstSegments(getProjectOutputLoc(resource.getProject()).matchingFirstSegments(fileLoc)).setDevice(null);
    		return fileLoc.toString();
    	} catch (Throwable e) {
    		if (resource != null)
        		logger.error("Unexpected Error While Building: " + resource + " class : " + resource.getClass(), e);
			else
				logger.error("Unexpected Error While Building: (null)", e);
    	}
		return null;
    }
    
    private static IProject cachedProject = null;
	private static IPath cachedProjOutputLoc = null;
	
	// TODO: review the need for this method
    private static IPath getProjectOutputLoc(IProject proj) throws JavaModelException {
		if (proj != cachedProject) {
			cachedProject = proj;
			cachedProjOutputLoc = cachedProject.getLocation();

			IJavaProject currJP = JavaCore.create(cachedProject);
			if (currJP.exists()) {
				// we do a removeFirstSegment since the project name is repeated
				// we add the project location since the given information is only relative
				cachedProjOutputLoc = cachedProjOutputLoc.append(currJP.getOutputLocation().removeFirstSegments(1));
			}
		}
		return cachedProjOutputLoc;
	}

	public int compareTo(ResourceToProcess o) {
		return resource.toString().compareTo(o.resource.toString());
	}
}