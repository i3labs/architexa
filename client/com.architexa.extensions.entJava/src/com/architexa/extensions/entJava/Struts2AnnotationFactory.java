package com.architexa.extensions.entJava;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
import com.architexa.store.ReloRdfRepository;
import com.architexa.utils.log4j.EclipseLog4JUtils;

public class Struts2AnnotationFactory extends AtxaRDFBuildProcessor implements IASMAnnotationFactory, IStartup {
	static final Logger logger = EclipseLog4JUtils.getLogger("com.architexa.diagrams.jdt", Struts2AnnotationFactory.class);

	private static final String ACTION_ANNO = "Lorg/apache/struts2/convention/annotation/Action";
	private static final String NAMESPACE_ANNO = "Lorg/apache/struts2/convention/annotation/Namespace";
	private static final String RESULT_ANNO = "Lorg/apache/struts2/convention/annotation/Result";
	
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
	
	Map<String, List<String>> resToJspMap = new LinkedHashMap<String, List<String>>();

	Map<String,Struts2AnnotationCollector> classResToCollectorMap = new HashMap<String, Struts2AnnotationCollector>();
	public AnnotationVisitor getClassVisitor(ReloRdfRepository rdfRepo, Resource classRes, String descriptor) {
		if (descriptor == null) return null;
			
		if (descriptor.contains(NAMESPACE_ANNO)
				|| descriptor.contains(RESULT_ANNO)) {

			if (!classResToCollectorMap.containsKey(classRes.toString()))
				classResToCollectorMap.put(classRes.toString(),
						new Struts2AnnotationCollector(resToJspMap));

			return classResToCollectorMap.get(classRes.toString());
		}
		return null;
		
	}

	public AnnotationVisitor getMethdVisitor(ReloRdfRepository rdfRepo, Resource methdRes, String descriptor) {
		if (descriptor == null || !descriptor.contains(ACTION_ANNO)) return null;
		
		return new Struts2AnnotationCollector(resToJspMap);
	}
	
	// processing of resources and adding statements
	public void doneVisitingAnnotations(ReloRdfRepository rdfRepo, Resource projectRes, Resource res) {
		addAnnotationStatements(projectRes, res);
		resToJspMap.clear();
	}
	
	private void addAnnotationStatements(Resource projectRes, Resource res) {
		for (String strutsPathId : resToJspMap.keySet()) {
			if (strutsPathId == null || strutsPathId.length() == 0 || resToJspMap.get(strutsPathId).isEmpty()) continue;

			rdfRepo.commitTransaction();
			rdfRepo.startTransaction();
			String path = findParentPath(strutsPathId, res);
			
			String actionId = EJCore.getId(path.replaceAll("/", "."));
			Resource actionRes  = RSECore.idToResource(rdfRepo, EJCore.jdtExtWkspcNS, actionId);
			
			rdfRepo.addStatement(actionRes, rdfRepo.rdfType, EJCore.webPathType);
			rdfRepo.addStatement(actionRes, RSECore.name, "/" + actionId.substring(actionId.indexOf("$")+1));
			rdfRepo.addStatement(actionRes, RJCore.access, RJCore.publicAccess);
			Resource packageRes = AsmPackageSupport.getPackage(rdfRepo, AsmPackageSupport.getPckgNameFromClassID(actionId), projectRes);
			rdfRepo.addStatement(packageRes, RSECore.contains, actionRes);
			rdfRepo.addStatement(packageRes, EJCore.webFolder, RSECore.trueStatement);

			resourcesToProcess.add(new ItemsAndFldrRes(actionRes, res, packageRes));
			
			String forwardId = "";
			for (String forwardPath : resToJspMap.get(strutsPathId)) {
				URI type = EJCore.webPathType;
				forwardPath = EJCore.WEBROOT + forwardPath;
				forwardPath = forwardPath.replace("%", "");
				forwardId = EJCore.webFileToID(forwardPath);

				Resource fwdPackageRes = AsmPackageSupport.getPackage(rdfRepo, AsmPackageSupport.getPckgNameFromClassID(forwardId),	projectRes);
				Resource forwardRes = RSECore.idToResource(rdfRepo,	EJCore.jdtExtWkspcNS, forwardId);
				rdfRepo.addStatement(forwardRes, rdfRepo.rdfType, type);
				rdfRepo.addStatement(forwardRes, RSECore.name, forwardId.substring(forwardId.indexOf("$") + 1));
				rdfRepo.addStatement(forwardRes, RJCore.access,	RJCore.publicAccess);
				rdfRepo.addStatement(actionRes, EJCore.tilesCall, forwardRes);
				rdfRepo.addStatement(fwdPackageRes, RSECore.contains, forwardRes);
			}
			rdfRepo.addStatement(actionRes, EJCore.implementedBy, res);
			
			rdfRepo.commitTransaction();
			rdfRepo.startTransaction();
		}		
	}
	
	private String findParentPath(String strutsPathId, Resource res) {
		Artifact art = new Artifact(res);
		Resource parentRes = art.queryParentArtifact(rdfRepo).elementRes;
		Artifact artParent = new Artifact(parentRes);
		if (artParent.queryType(rdfRepo).equals(RJCore.classType)
				&& art.queryType(rdfRepo).equals(RJCore.methodType))
			return addCntxtPath(parentRes, strutsPathId);
		else 
			return EJCore.WEBROOT + strutsPathId;
	}
	 
	private String addCntxtPath(Resource classRes, String strutsId) {
		String contextPath = "";
		StatementIterator statemntItr = rdfRepo.getStatements(null,
				EJCore.implementedBy, classRes);
		if (!statemntItr.hasNext())
			return strutsId;
		// art's parent will always be a class, and classes only implement one
		// JaxRS path each
		// so we do not need to loop through the iterator
		contextPath = RSECore.resourceToId(rdfRepo, statemntItr.next()
				.getSubject(), false);
		if (!contextPath.equals(EJCore.WEBROOT + "$.*")) {
			contextPath = contextPath.replace("$", ".");
			strutsId = contextPath + strutsId;
		}
		return strutsId;
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
