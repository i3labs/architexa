package com.architexa.diagrams.jdt.builder.asm;

import org.eclipse.core.runtime.IProgressMonitor;
import org.openrdf.model.Resource;

import com.architexa.diagrams.jdt.builder.AtxaBuildVisitor;
import com.architexa.diagrams.jdt.builder.AtxaRDFBuildProcessor;
import com.architexa.diagrams.model.Artifact;


/**
 * TODO: move package processing out of the build processor and into the core
 * builder, and therefore change this class from being a build processor.<br>
 * 
 * @author vineet
 */
public abstract class PackageBasedRDFBuildProcessor extends AtxaRDFBuildProcessor {

    Artifact cachedPckg = null;

    @Override
	public final void processRes(Resource classRes) {
    	Artifact currPckgRes = getContainingPckg(classRes);
    	if (currPckgRes == null || currPckgRes.equals(cachedPckg)) return;
    	
    	// we have started looking at a new package, lets process the old one
    	if (cachedPckg != null)
        	processPckg(cachedPckg);
    	cachedPckg = currPckgRes;
    }
    
    public abstract void processPckg(Artifact packageArt);

    // Used when user calls a full build to clear any previous cached package 
    public void resetCachedPckg() {
    	cachedPckg = null;
    }
    
    @Override
	public void processProj(AtxaBuildVisitor builder, IProgressMonitor monitor) {
    	super.processProj(builder, monitor);
    	if (cachedPckg != null)
    		processPckg(cachedPckg);
    	cachedPckg = null;
    }
    
    protected Artifact getContainingPckg(Resource givenRes) {
    	return ContainmentOptimizer.getContainingPckg(rdfRepo, givenRes);
    }
}
