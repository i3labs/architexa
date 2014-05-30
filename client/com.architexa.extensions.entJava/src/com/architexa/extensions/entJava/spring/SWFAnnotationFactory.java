package com.architexa.extensions.entJava.spring;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IStartup;
import org.objectweb.asm.AnnotationVisitor;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.sesame.sail.StatementIterator;

import com.architexa.diagrams.RSECore;
import com.architexa.diagrams.jdt.RJCore;
import com.architexa.diagrams.jdt.builder.AtxaBuildVisitor;
import com.architexa.diagrams.jdt.builder.AtxaRDFBuildProcessor;
import com.architexa.diagrams.jdt.builder.asm.AsmPackageSupport;
import com.architexa.diagrams.jdt.builder.asm.DepAndChildrenStrengthSummarizer;
import com.architexa.diagrams.jdt.builder.asm.PluggableAsmAnnotationVisitorSupport;
import com.architexa.diagrams.jdt.builder.asm.PluggableAsmAnnotationVisitorSupport.IASMAnnotationFactory;
import com.architexa.diagrams.jdt.services.PluggableBuildProcessor;
import com.architexa.diagrams.model.Artifact;
import com.architexa.extensions.entJava.EJCore;
import com.architexa.extensions.entJava.JAXRSAnnotationFactory;
import com.architexa.store.ReloRdfRepository;
import com.architexa.utils.log4j.EclipseLog4JUtils;

public class SWFAnnotationFactory extends AtxaRDFBuildProcessor implements IASMAnnotationFactory, IStartup {
	static final Logger logger = EclipseLog4JUtils.getLogger("com.architexa.diagrams.jdt", JAXRSAnnotationFactory.class);
	private static final String CONTROLLER_ANNO = "Lorg/springframework/stereotype/Controller";
	private static final String REQUEST_MAPPING_ANNO = "Lorg/springframework/web/bind/annotation/RequestMapping";
	
	// Startup / Registration
	public void earlyStartup() {
		try {
			earlyStartupInternal();
		} catch (Throwable t) {
			logger.error("Unexpected Error", t);
		}
	}
	private void earlyStartupInternal() {
		PluggableAsmAnnotationVisitorSupport.registerAnnotationFactory(this);
		PluggableBuildProcessor.register(this);
	}

	// Collection of resources to process
	boolean isMethodToClassRel = false;
	private List<String> idToProcess = new ArrayList<String>();

	public AnnotationVisitor getClassVisitor(ReloRdfRepository rdfRepo, Resource classRes, String descriptor) {
		if (descriptor.contains(CONTROLLER_ANNO))
			return new SWFAnnotationController(idToProcess);
		return null;
	}

	public AnnotationVisitor getMethdVisitor(ReloRdfRepository rdfRepo, Resource methdRes, String descriptor) {
		if (descriptor.contains(REQUEST_MAPPING_ANNO)) 
			return new SWFAnnotationController(idToProcess);
		return null;
	}
	
	// processing of resources and adding statements
	public void doneVisitingAnnotations(ReloRdfRepository rdfRepo, Resource projectRes, Resource res) {
		if (isMethodToClassRel && idToProcess.isEmpty())
			idToProcess.add(EJCore.WEBROOT);
		
		addAnnotationStatements(projectRes, res);
		idToProcess.clear();
		isMethodToClassRel = false;
	}
	
	private void addAnnotationStatements(Resource projectRes, Resource res) {
		for (String jaxrsId : idToProcess) {
			
			Artifact art = new Artifact(res);
			Resource parentRes = art.queryParentArtifact(rdfRepo).elementRes;
			Artifact artParent = new Artifact(parentRes);
			boolean isPath = false;
			if (artParent.queryType(rdfRepo).equals(RJCore.classType) && art.queryType(rdfRepo).equals(RJCore.methodType)) { 
				jaxrsId = addCntxtPath(parentRes, jaxrsId);
				isPath  = true;
			}
			
			Resource resource = res;
			Resource springRes  = RSECore.idToResource(rdfRepo, EJCore.jdtExtWkspcNS, jaxrsId);
			
			rdfRepo.commitTransaction();
			rdfRepo.startTransaction();
			
			rdfRepo.addStatement(springRes, rdfRepo.rdfType, EJCore.webPathType);
			rdfRepo.addStatement(springRes, RSECore.name, "/" + jaxrsId.substring(jaxrsId.indexOf("$")+1));
			rdfRepo.addStatement(springRes, RJCore.access, RJCore.publicAccess);
			Resource packageRes = AsmPackageSupport.getPackage(rdfRepo, AsmPackageSupport.getPckgNameFromClassID(jaxrsId), projectRes);
			rdfRepo.addStatement(packageRes, RSECore.contains, springRes);
			rdfRepo.addStatement(packageRes, EJCore.webFolder, RSECore.trueStatement);
			rdfRepo.addStatement(springRes, EJCore.implementedBy, resource);
			
			//**********
			if (isPath) {
				URI type = EJCore.webPathType;
				String forwardId = "";
				String firstUrl = jaxrsId.substring(0, jaxrsId.lastIndexOf("$"));
				String lastUrl = jaxrsId.substring(jaxrsId.lastIndexOf("$"));
				if (lastUrl.contains("."))
					lastUrl = lastUrl.substring(0, lastUrl.lastIndexOf("."));
				
				forwardId = firstUrl + lastUrl + ".jsp";
				
				Resource fwdPackageRes = AsmPackageSupport.getPackage(rdfRepo, AsmPackageSupport.getPckgNameFromClassID(forwardId),	projectRes);
				Resource forwardRes = RSECore.idToResource(rdfRepo,	EJCore.jdtExtWkspcNS, forwardId);
				rdfRepo.addStatement(forwardRes, rdfRepo.rdfType, type);
				rdfRepo.addStatement(forwardRes, RSECore.name, forwardId.substring(forwardId.indexOf("$") + 1));
				rdfRepo.addStatement(forwardRes, RJCore.access,	RJCore.publicAccess);
				rdfRepo.addStatement(springRes, EJCore.tilesCall, forwardRes);
				rdfRepo.addStatement(fwdPackageRes, RSECore.contains, forwardRes);
			}
			//**********
			
			rdfRepo.commitTransaction();
			rdfRepo.startTransaction();
			
			resourcesToProcess.add(new ItemsAndFldrRes(springRes, resource, packageRes));
		}		
	}
	private String addCntxtPath(Resource classRes, String jaxrsId) {
		String contextPath = "";
		StatementIterator statemntItr = rdfRepo.getStatements(null, EJCore.implementedBy, classRes);
		
		// art's parent will always be a class, and classes only implement one JaxRS path each
		// so we do not need to loop through the iterator
		if (!statemntItr.hasNext()) return jaxrsId;
		
		contextPath = RSECore.resourceToId(rdfRepo, statemntItr.next().getSubject(), false);
		if (isMethodToClassRel)
			jaxrsId = contextPath;
		else if (!contextPath.equals(EJCore.WEBROOT+"$.*")) {
			contextPath = contextPath.replace("$", ".");
			jaxrsId = jaxrsId.replace(EJCore.WEBROOT, contextPath);
		}
		return jaxrsId;
	}

	private List<ItemsAndFldrRes> resourcesToProcess = new ArrayList<ItemsAndFldrRes>();
	private static class ItemsAndFldrRes {
		public Resource itemRes;
		public Resource javaRes; 
		public Resource packageRes;
		public ItemsAndFldrRes(Resource _itemRes, Resource _javaRes, Resource _packageRes) {
			this.itemRes = _itemRes;
			this.javaRes = _javaRes;
			this.packageRes = _packageRes;
		}
	};
	
	
	// final processing
	// adding/generating statements for package/folder hierarchy
	@Override
    public void processProj(AtxaBuildVisitor builder, IProgressMonitor monitor) {
    	super.processProj(builder, monitor);
    	rdfRepo.commitTransaction();
		rdfRepo.startTransaction();
		
    	DepAndChildrenStrengthSummarizer dss = builder.getDSS();
    	for (ItemsAndFldrRes res : resourcesToProcess) {
    		dss.updateSrcFldrCache(res.packageRes);
    		dss.storeNCacheTypeType(res.itemRes, RJCore.refType, res.javaRes);
    		builder.runProcessors(res.itemRes, false);
    	}
    	resourcesToProcess.clear();
    	rdfRepo.commitTransaction();
		rdfRepo.startTransaction();
		
	}
}
