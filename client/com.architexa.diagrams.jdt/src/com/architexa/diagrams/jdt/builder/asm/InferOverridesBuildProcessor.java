package com.architexa.diagrams.jdt.builder.asm;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IStartup;
import org.openrdf.sesame.sailimpl.nativerdf.model.NativeURI;

import com.architexa.diagrams.RSECore;
import com.architexa.diagrams.jdt.Activator;
import com.architexa.diagrams.jdt.Filters;
import com.architexa.diagrams.jdt.RJCore;
import com.architexa.diagrams.jdt.builder.AtxaBuildVisitor;
import com.architexa.diagrams.jdt.builder.AtxaRDFBuildProcessor;
import com.architexa.diagrams.jdt.services.PluggableBuildProcessor;
import com.architexa.diagrams.jdt.utils.MethodParametersSupport;
import com.architexa.diagrams.model.Artifact;
import com.architexa.diagrams.model.DirectedRel;


// this build processor adds about 12s to building the lapis project!
//@tag design-todo: unify transformer design pattern
public class InferOverridesBuildProcessor extends AtxaRDFBuildProcessor implements IStartup {
	private static final Logger logger = Activator.getLogger(InferOverridesBuildProcessor.class);

	public void earlyStartup() {
		try {
			earlyStartupInternal();
		} catch (Throwable t) {
			logger.error("Unexpected Error", t);
		}
	}
	private void earlyStartupInternal() {
		PluggableBuildProcessor.register(this);
	}
	
	interface Transformer {
		void transform(Artifact codeArt);
	}

	class Joiner implements Transformer {
		public void transform(Artifact currClass) {
    		List<Artifact> currMethsLookingForOverrides = currClass.queryChildrenArtifacts(rdfRepo, Filters.getTypeFilter(rdfRepo, RJCore.methodType));
    		
			// we need to check interfaces seperate from classes since if there
			// is a method that is overridden in both we need to check the class
			// parents first
    		List <Artifact> interfacesForLaterCheck = new ArrayList<Artifact> (10);
    		
    		getSuperClassesToCheckForOverrides(currClass, currMethsLookingForOverrides, interfacesForLaterCheck);
    		for(Artifact interfaceArt : new ArrayList<Artifact>(interfacesForLaterCheck)) {
    			interfacesForLaterCheck.remove(interfaceArt);
    			checkForOverrides(interfaceArt, currMethsLookingForOverrides);
	    		getSuperClassesToCheckForOverrides(interfaceArt, currMethsLookingForOverrides, interfacesForLaterCheck);
    		}
		}

		private void getSuperClassesToCheckForOverrides(Artifact currClass, List<Artifact> currMethsLookingForOverrides , List<Artifact> interfacesForLaterCheck) {
			List<Artifact> superClasses = currClass.queryArtList(rdfRepo, DirectedRel.getFwd(RJCore.inherits));
			for (Artifact superClass : superClasses) {
				if (superClass.queryType(rdfRepo).equals(RJCore.interfaceType)) {
					interfacesForLaterCheck.add(superClass);
					continue;
				}
				checkForOverrides(superClass, currMethsLookingForOverrides);
	    		getSuperClassesToCheckForOverrides(superClass, currMethsLookingForOverrides, interfacesForLaterCheck);
			}
		}

		private void checkForOverrides(Artifact superClass, List<Artifact> currMethsLookingForOverrides) {
			List<Artifact> superMeths = superClass.queryChildrenArtifacts(rdfRepo, Filters.getTypeFilter(rdfRepo, RJCore.methodType));
    		for (Artifact currMeth : new ArrayList<Artifact>(currMethsLookingForOverrides)) {
    			String currMethName = currMeth.queryName(rdfRepo);
    			String currMethSig = MethodParametersSupport.getInOutSig(currMeth, rdfRepo, 2);
    			for (Artifact superMeth : superMeths) {
        			String superMethName = superMeth.queryName(rdfRepo);
        			String superMethSig = MethodParametersSupport.getInOutSig(superMeth, rdfRepo, 2);
        			if (!currMethName.equals(superMethName)) continue;
        			if (!currMethSig.equals(superMethSig)) continue;
        			
        			currMethsLookingForOverrides.remove(currMeth);
        			
        			rdfRepo.addStatement(currMeth.elementRes, RJCore.overrides, superMeth.elementRes);
				}
			}
		}
	}

	class Unjoiner implements Transformer {
		public void transform(Artifact currClass) {
    		List<Artifact> currMeths = currClass.queryChildrenArtifacts(rdfRepo, Filters.getTypeFilter(rdfRepo, RJCore.methodType));
    		for (Artifact currMeth : currMeths) {
    			rdfRepo.removeStatements(currMeth.elementRes, RJCore.overrides, null);
    		}
		}
		
	}

	@Override
	public void processProj(AtxaBuildVisitor builder, IProgressMonitor monitor) {
		monitor.beginTask("Overrides Build Processing: ", getProjectChildrenList().size());
		if (project != null && packgList == null)
			createPackgList();
	    transform(projArt, new Joiner(), monitor);
    }
    
	@Override
	public void cleanProj(AtxaBuildVisitor builder, IProgressMonitor monitor) {
		monitor.beginTask("Overrides Build Processing: ", getProjectChildrenList().size());
		if (project != null && packgList == null)
			createPackgList();
	    transform(projArt, new Unjoiner(), monitor);
    }

    private void transform(Artifact codeArt, Transformer transformer, IProgressMonitor monitor) {
		// do a depth first traversal of the containment heirarchy while 'transforming'
		// 'transforming' = {preCondition: search for anonymous methods} do a join on method calls

    	List<Artifact> children = codeArt.queryChildrenArtifacts(rdfRepo);
    	for (Artifact child : children) {
    		if (packgList.contains(((NativeURI)child.elementRes).getLocalName()))
    			continue;
			transform(child, transformer, monitor);
		}
    	if (!codeArt.queryType(rdfRepo).equals(RJCore.classType)) return;
    	if (monitor.isCanceled()) return;
    	monitor.subTask(RSECore.resourceWithoutWorkspace(codeArt.elementRes));
    	if (getProjectChildrenList().contains(codeArt))
    		monitor.worked(1);
    	transformer.transform(codeArt);
	}

}
