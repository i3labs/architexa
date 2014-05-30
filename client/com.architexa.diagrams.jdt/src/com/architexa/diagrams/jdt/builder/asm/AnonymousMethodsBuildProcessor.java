package com.architexa.diagrams.jdt.builder.asm;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IStartup;
import org.openrdf.sesame.sailimpl.nativerdf.model.NativeURI;

import com.architexa.diagrams.RSECore;
import com.architexa.diagrams.jdt.Activator;
import com.architexa.diagrams.jdt.RJCore;
import com.architexa.diagrams.jdt.builder.AtxaBuildVisitor;
import com.architexa.diagrams.jdt.builder.AtxaRDFBuildProcessor;
import com.architexa.diagrams.jdt.services.PluggableBuildProcessor;
import com.architexa.diagrams.model.Artifact;
import com.architexa.diagrams.model.DirectedRel;



//@tag design-todo: unify transformer design pattern
public class AnonymousMethodsBuildProcessor extends AtxaRDFBuildProcessor implements IStartup {
	private static final Logger logger = Activator.getLogger(AnonymousMethodsBuildProcessor.class);

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
		public void transform(Artifact codeArt) {
	    	List<Artifact> called = codeArt.queryArtList(rdfRepo, DirectedRel.getFwd(RJCore.calls));
	    	List<Artifact> calling = codeArt.queryArtList(rdfRepo, DirectedRel.getRev(RJCore.calls));
	    	for (Artifact callingMeth : calling) {
	        	for (Artifact calledMeth : called) {
	    			rdfRepo.addStatement(callingMeth.elementRes, RJCore.calls, calledMeth.elementRes);
	    		}
			}
	    	rdfRepo.removeStatements(codeArt.elementRes, rdfRepo.rdfType, null);
		}
	}

	class Unjoiner implements Transformer {
		public void transform(Artifact codeArt) {
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
		monitor.beginTask("Anonymous Methods Build Processing: ", getProjectChildrenList().size());
		if (project != null && packgList == null)
			createPackgList();
	    transform(projArt, new Joiner(), monitor);
    }
    
	@Override
	public void cleanProj(AtxaBuildVisitor builder, IProgressMonitor monitor) {
		monitor.beginTask("Anonymous Methods Build Processing: ", getProjectChildrenList().size());
		if (project != null && packgList == null)
			createPackgList();
	    transform(projArt, new Unjoiner(), monitor);
    }

//	List<String> packgList = null;
//	private void createProcessList() {
////		IJavaElement projNameJDT = RJCore.resourceToJDTElement(StoreUtil.getDefaultStoreRepository(), projArt.elementRes);
////		String projName = ((IPackageFragment)projNameJDT).getParent().getParent().getElementName();
//		String projName = project.getName();
//		packgList = new ArrayList<String>();
//		packgList = ResourceQueueManager.getUncheckedPackageList(projName);
//	}
	
    private void transform(Artifact codeArt, Transformer transformer, IProgressMonitor monitor) {
		// do a depth first traversal of the containment heirarchy while 'transforming'
		// 'transforming' = {preCondition: search for anonymous methods} do a join on method calls
    	if (monitor.isCanceled()) return;
    	monitor.subTask(RSECore.resourceWithoutWorkspace(codeArt.elementRes));
    	List<Artifact> children = codeArt.queryChildrenArtifacts(rdfRepo);
    	for (Artifact child : children) {
    		if (packgList.contains(((NativeURI)child.elementRes).getLocalName()))
    			continue;
			transform(child, transformer, monitor);
		}
    	if (!rdfRepo.hasStatement(codeArt.elementRes, AsmMethodSupport.anonymousMethodType, true)) return;

    	monitor.subTask(RSECore.resourceWithoutWorkspace(codeArt.elementRes));
    	if (getProjectChildrenList().contains(codeArt))
    		monitor.worked(1);
    	transformer.transform(codeArt);
	}

}
