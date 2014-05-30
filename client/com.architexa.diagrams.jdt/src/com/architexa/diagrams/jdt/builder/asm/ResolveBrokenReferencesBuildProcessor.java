package com.architexa.diagrams.jdt.builder.asm;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IStartup;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.sesame.sailimpl.nativerdf.model.NativeURI;

import com.architexa.diagrams.RSECore;
import com.architexa.diagrams.jdt.Activator;
import com.architexa.diagrams.jdt.Filters;
import com.architexa.diagrams.jdt.RJCore;
import com.architexa.diagrams.jdt.builder.AtxaBuildVisitor;
import com.architexa.diagrams.jdt.builder.AtxaRDFBuildProcessor;
import com.architexa.diagrams.jdt.services.PluggableBuildProcessor;
import com.architexa.diagrams.model.Artifact;
import com.architexa.diagrams.model.DirectedRel;


// @tag design-todo: unify transformer design pattern
public class ResolveBrokenReferencesBuildProcessor extends AtxaRDFBuildProcessor implements IStartup {
    private static final Logger logger = Activator.getLogger(ResolveBrokenReferencesBuildProcessor.class);

	static final URI resolvedBrokenReference = RSECore.createRseUri("jdt-asm#resolvedBrokenReference");

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
		public void transform(Artifact currMeth) {
	    	List<Artifact> fieldReferences = currMeth.queryArtList(rdfRepo, DirectedRel.getFwd(RJCore.refType));
	    	for (Artifact ref : fieldReferences) {
	    		if (ref.isInitialized(rdfRepo)) continue;				
				fixReferencesTo(currMeth, ref);
			}
	    	
	    	List<Artifact> methodReferences = currMeth.queryArtList(rdfRepo, DirectedRel.getFwd(RJCore.calls));	    	
	    	for (Artifact ref : methodReferences) {
	    		if (ref.isInitialized(rdfRepo)) continue;	
				fixReferencesTo(currMeth, ref);
			}
		}

		
		private void fixReferencesTo(Artifact currMeth, Artifact ref) {
			//if (true) return;

			// @tag buggy: need to thoroughly test
			//System.err.println("Checking: " + ref);
			String refURI = ref.elementRes.toString();
			
			//TODO Check if this is needed and can it save us sometime 
			// without causing problem in certain cases.
			// Also un-comment the addStatement later in the code for resolvedBrokenReferences
			
//			if (rdfRepo.hasStatement(ref.elementRes, resolvedBrokenReference, true)) {
//				logger.warn("Already resolved: " + ref);
//				return;
//			}

			int typeNdx = refURI.lastIndexOf("$");
			int fldNdx = refURI.lastIndexOf(".");
			//System.err.println(typeNdx);
			//System.err.println(fldNdx);
			if (typeNdx == -1) return;
			if (fldNdx == -1) return;
			if (typeNdx > fldNdx) return;		// types - ignore
			String currMemberName = refURI.substring(fldNdx+1);
			// @tag test-this: remove below?
			//if (currFieldName.contains(")")) return;
			//System.err.println(refURI.substring(0, fldNdx));
			Resource parentRefURI = rdfRepo.createURI(refURI.substring(0, fldNdx));
			Artifact parentRefArt = new Artifact(parentRefURI);
			//rdfRepo.dumpStatements(parentRefArt.elementRes, null, null);
			if (!parentRefArt.isInitialized(rdfRepo)) return; // parent also not set - ignore

			//System.err.println("Examining: " + ref + " fld: " + currFieldName);

    		getSuperClassesToCheckForOverrides(parentRefArt, ref, currMemberName, currMeth);
			
		}

		private void getSuperClassesToCheckForOverrides(Artifact parentRefArt, Artifact currMemberLookingForOverrides, String currMemberName, Artifact currMeth) {
			List<Artifact> superClasses = parentRefArt.queryArtList(rdfRepo, DirectedRel.getFwd(RJCore.inherits));
			for (Artifact superClass : superClasses) {
				checkForOverrides(superClass, currMemberLookingForOverrides, currMemberName, currMeth);
	    		getSuperClassesToCheckForOverrides(superClass, currMemberLookingForOverrides, currMemberName, currMeth);
			}
		}

		private void checkForOverrides(Artifact superClass,	Artifact currMemberLookingForOverrides, String currMemberName, Artifact currMeth) {

			// Creating the string to match the name and signature of the override method.
			String memberString = superClass.elementRes.toString() + "."+ currMemberName;
			List<Artifact> superFields = superClass.queryChildrenArtifacts(rdfRepo, Filters.getTypeFilter(rdfRepo, RJCore.fieldType));
			for (Artifact superField : superFields) {
				if (!superField.toString().equals(memberString))
					continue;
				// System.err.println("Real Fixing: " + currFieldLookingForOverrides.elementRes+ " ===>>>> " + superField.elementRes);
				// get all links to currFieldLookingForOverrides, and replace to superField
				List<Artifact> refSources = currMemberLookingForOverrides.queryArtList(rdfRepo, DirectedRel.getRev(RJCore.refType));
				for (Artifact refSrc : refSources) {
					rdfRepo.removeStatements(refSrc.elementRes, RJCore.refType,	currMemberLookingForOverrides.elementRes);
					rdfRepo.addStatement(refSrc.elementRes, RJCore.refType,	superField.elementRes);
				}
				
//				if (!refSources.isEmpty())
//					rdfRepo.addStatement(currMemberLookingForOverrides.elementRes, resolvedBrokenReference, true);
			}

			List<Artifact> superMethods = superClass.queryChildrenArtifacts(rdfRepo, Filters.getTypeFilter(rdfRepo, RJCore.methodType));
			for (Artifact superMethod : superMethods) {
				if (!superMethod.toString().equals(memberString))
					continue;
				List<Artifact> refSources = currMemberLookingForOverrides.queryArtList(rdfRepo, DirectedRel.getRev(RJCore.calls));
				for (Artifact refSrc : refSources) {
					rdfRepo.removeStatements(refSrc.elementRes, RJCore.calls, currMemberLookingForOverrides.elementRes);
					rdfRepo.addStatement(refSrc.elementRes, RJCore.calls, superMethod.elementRes);
				}

//				if (!refSources.isEmpty())
//					rdfRepo.addStatement(currMemberLookingForOverrides.elementRes, resolvedBrokenReference, true);
			}
		}
	}

	// @tag implement-this: otherwise db might get corrupt
	class Unjoiner implements Transformer {
		public void transform(Artifact codeArt) {
			if (true) return;
	    	List<Artifact> called = codeArt.queryArtList(rdfRepo, DirectedRel.getFwd(RJCore.calls));
	    	List<Artifact> calling = codeArt.queryArtList(rdfRepo, DirectedRel.getRev(RJCore.calls));
	    	for (Artifact callingMeth : calling) {
	        	for (Artifact calledMeth : called) {
	    			rdfRepo.removeStatements(callingMeth.elementRes, RJCore.calls, calledMeth.elementRes);
	    		}
			}
	    	rdfRepo.addTypeStatement(codeArt.elementRes, RJCore.methodType);
		}
		
	}

	@Override
	public void processProj(AtxaBuildVisitor builder, IProgressMonitor monitor) {
		monitor.beginTask("Resolve Broken Reference Build Processing: ", getProjectChildrenList().size());
		if (project != null && packgList == null)
			createPackgList();
	    transform(projArt, new Joiner(), monitor);
    }
    
	@Override
	public void cleanProj(AtxaBuildVisitor builder, IProgressMonitor monitor) {
		monitor.beginTask("Resolve Broken Reference Build Processing: ", getProjectChildrenList().size());
		if (project != null && packgList == null)
			createPackgList();
	    transform(projArt, new Unjoiner(), monitor);
    }

	private void transform(Artifact codeArt, Transformer transformer, IProgressMonitor monitor) {
		// do a depth first traversal of the containment heirarchy while 'transforming'
		// 'transforming' = {preCondition: search for methods} and then search
		//   for broken references to join with reference from base

    	List<Artifact> children = codeArt.queryChildrenArtifacts(rdfRepo);
    	for (Artifact child : children) {
    		if (packgList.contains(((NativeURI)child.elementRes).getLocalName()))
    			continue;
			transform(child, transformer, monitor);
		}
    	if (!rdfRepo.hasStatement(codeArt.elementRes, RJCore.refType, null)) return;
    	if (monitor.isCanceled()) return;
    	monitor.subTask(RSECore.resourceWithoutWorkspace(codeArt.elementRes));
    	if (getProjectChildrenList().contains(codeArt))
    		monitor.worked(1);
    	transformer.transform(codeArt);
	}

}
