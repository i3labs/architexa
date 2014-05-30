package com.architexa.extensions.entJava.spring;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IStartup;
import org.objectweb.asm.AnnotationVisitor;
import org.openrdf.model.Resource;
import org.openrdf.sesame.sail.StatementIterator;

import com.architexa.diagrams.RSECore;
import com.architexa.diagrams.jdt.RJCore;
import com.architexa.diagrams.jdt.builder.AtxaBuildVisitor;
import com.architexa.diagrams.jdt.builder.AtxaRDFBuildProcessor;
import com.architexa.diagrams.jdt.builder.asm.PluggableAsmAnnotationVisitorSupport;
import com.architexa.diagrams.jdt.builder.asm.PluggableAsmAnnotationVisitorSupport.IASMAnnotationFactory;
import com.architexa.diagrams.jdt.model.CUSupport;
import com.architexa.diagrams.jdt.services.PluggableBuildProcessor;
import com.architexa.diagrams.model.Artifact;
import com.architexa.diagrams.services.PluggableTypeGuesser;
import com.architexa.extensions.entJava.EJCore;
import com.architexa.store.ReloRdfRepository;
import com.architexa.utils.log4j.EclipseLog4JUtils;

public class SpringAnnotationFactory extends AtxaRDFBuildProcessor implements IASMAnnotationFactory, IStartup {
	static final Logger logger = EclipseLog4JUtils.getLogger("com.architexa.diagrams.jdt", SpringAnnotationFactory.class);
	private static final String REPOSITORY_ANNO = "springframework/stereotype/Repository";
	private static final String SERVICE_ANNO = "springframework/stereotype/Service";
	private static final String CONTROLLER_ANNO = "springframework/stereotype/Controller";

	private static final String AUTOWIRED_ANNO = "springframework/beans/factory/annotation/Autowired";
	
//	private static final String PATH_ANNO = "javax/ws/rs/Path";
//	private static final String GET_ANNO = "javax/ws/rs/GET";
//	private static final String PUT_ANNO = "javax/ws/rs/PUT";
//	private static final CharSequence POST_ANNO = "javax/ws/rs/POST";
	
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
		PluggableBuildProcessor.registerLast(this);
	}

	// Collection of resources to process
	boolean isMethodToClassRel = false;
	private List<Resource> beans = new ArrayList<Resource>();
	List<Resource> autowirePropMthds = new ArrayList<Resource>();
	
    @Override
	public void processProj(AtxaBuildVisitor atxaBuildVisitor, IProgressMonitor progressMonitor) {
    	if (!autowirePropMthds.isEmpty()) {
			addAutowireStatements();
		}
    	if (!beans.isEmpty()) {
			addBeanStatements();
		}
    }
	
	public AnnotationVisitor getClassVisitor(ReloRdfRepository rdfRepo, Resource classRes, String descriptor) {
		if (descriptor.contains(REPOSITORY_ANNO) || descriptor.contains(SERVICE_ANNO) || descriptor.contains(CONTROLLER_ANNO))
			beans.add(classRes);
		return null;
	}

	public AnnotationVisitor getMethdVisitor(ReloRdfRepository rdfRepo, Resource methdRes, String descriptor) {
		if (descriptor.contains(AUTOWIRED_ANNO))
			autowirePropMthds.add(methdRes);
		return null;
	}
	
	private void addBeanStatements() {
		rdfRepo.commitTransaction();
		rdfRepo.startTransaction();
		for (Resource bean : beans) {
    		rdfRepo.addStatement(bean, EJCore.springBeanClassProp, RSECore.trueStatement);
		}
		rdfRepo.commitTransaction();
		rdfRepo.startTransaction();
		beans.clear();
	}
	private void addAutowireStatements() {
		rdfRepo.commitTransaction();
		rdfRepo.startTransaction();
		for (Resource method : autowirePropMthds) {
			Artifact art = new Artifact(method);
			Artifact x = art.queryParentArtifact(rdfRepo);
			if (x==null) continue; 
			Resource parentRes = x.elementRes;
			
			StatementIterator si2 = rdfRepo.getStatements(method, RJCore.refType, null);
			while (si2.hasNext()) {
				Resource refType = (Resource) si2.next().getObject();
				Artifact art2 = new Artifact(refType);
				Resource type = art2.queryType(rdfRepo);
				
				if (CUSupport.isType(type)
						&& !parentRes.equals(art2.elementRes)
						&& method.toString().contains(art2.queryName(rdfRepo))) {
					rdfRepo.addStatement(art2.elementRes, EJCore.springBeanPropertyRef, method);
				}
					
			}
			
			// get the methods type references
			List<Resource> methodTypeRefs = new ArrayList<Resource>();
			rdfRepo.getResourcesFor(methodTypeRefs, method, RJCore.refType, null);
			
			

			// get fields the class contains
			StatementIterator si = rdfRepo.getStatements(parentRes, RSECore.contains, null);
			while (si.hasNext()) {
				Resource childRes = (Resource) si.next().getObject();
				Resource type = PluggableTypeGuesser.getType(childRes, rdfRepo);
				if (type == RJCore.fieldType) {
					for (Resource paramRefType : methodTypeRefs) { 
						// for each method type ref, if there is 
						if (rdfRepo.hasStatement(childRes, RJCore.refType, paramRefType)) {
							rdfRepo.addStatement(childRes, EJCore.springBeanFieldProp, RSECore.trueStatement);
							List<Resource> imps = new ArrayList<Resource>();
							
							rdfRepo.getResourcesFor(imps, null, RJCore.inherits, paramRefType);
							rdfRepo.getResourcesFor(imps, paramRefType, RJCore.inherits, null);
							for (Resource bean : imps) {
								if (rdfRepo.hasStatement(bean, EJCore.springBeanClassProp, RSECore.trueStatement))
									rdfRepo.addStatement(childRes, EJCore.springBeanPropertyRef, bean);
							}
						}
					}
				}
					
			}
			si.close();
		}		
		rdfRepo.commitTransaction();
		rdfRepo.startTransaction();
		autowirePropMthds.clear();
	}
	public void doneVisitingAnnotations(ReloRdfRepository rdfRepo, Resource projectRes, Resource res) {}

}
