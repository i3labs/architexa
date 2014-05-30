package com.architexa.diagrams.jdt.builder.asm;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IResource;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.openrdf.model.Resource;
import org.openrdf.sesame.sail.StatementIterator;

import com.architexa.diagrams.RSECore;
import com.architexa.diagrams.jdt.Activator;
import com.architexa.diagrams.jdt.RJCore;
import com.architexa.diagrams.jdt.builder.AtxaBuildVisitor;
import com.architexa.diagrams.jdt.model.CUSupport;
import com.architexa.diagrams.jdt.utils.MethodParametersSupport;
import com.architexa.diagrams.model.Artifact;
import com.architexa.store.ReloRdfRepository;


/**
 * This is the root visitor for parsing Java Class files to RDF statements used
 * by Relo. Relevant class data is parsed and added to the Relo RDF database.
 */
public class AsmClassSupport implements ClassVisitor {
	static final Logger logger = Activator.getLogger(AsmClassSupport.class);

	static final String OBJECT_TYPE_NAME = Type.getType(Object.class).getInternalName();

	private static final int UNKNOWN_ACCESS = 65536;

	private IResource eclipseClassResource;
	private Resource projectRes;
	private ReloRdfRepository rdfRepo;
	private Resource classRes;
	private Resource sourceResource = RSECore.createRseUri("asm#NONE");
	private String classLabel;
	private String fqClassName;
	private String rdfClassname;

    private final String projName;

	private final DepAndChildrenStrengthSummarizer dss;

	public AsmClassSupport(ReloRdfRepository repo, DepAndChildrenStrengthSummarizer dss, Resource projectRes, String projName, IResource eclipseClassResource) {
		this.rdfRepo = repo;
		this.dss = dss;
		this.projectRes = projectRes;
        this.projName = projName;
		this.eclipseClassResource = eclipseClassResource;
	}
    
    public Resource getClassRes() {
        return classRes;
    }
    
	public static List<Resource> removeClassStatement(ReloRdfRepository reloRdf, String className, AtxaBuildVisitor buildVisitor) {
		Resource classRes = AsmUtil.getClassRes(className.replace(AtxaBuildVisitor.ClassExt, ""), reloRdf);
		if(classRes==null) return new ArrayList<Resource>();
		
		StatementIterator iter = reloRdf.getStatements(classRes, RSECore.contains, null);
		Set<Resource> classMembersToRemove = new HashSet<Resource> (25);
		while(iter.hasNext()) {
			Resource containedRes = (Resource) iter.next().getObject();

			// we get called on events from changes .class files as well, so we
			// get independent remove requests for the nested classes as well
			// and therefore should skip them here - otherwise we might have
			// already removed them and added new updated statements just to
			// remove them again and keep the repository in an old/bad state
			// ...we do delete relationships from nested methods, fields, etc.
			// ... ... should this be recursive? no, since classes would be
			// ... ... the only need for that.
	    	Resource containedResType = new Artifact(containedRes).queryWarnedType(reloRdf);
			if (CUSupport.isType(containedResType)) continue;

			// we ideally want to remove right here - but the rdf repository is
			// returning the same values multiple times otherwise. So we just
			// make a copy.
			classMembersToRemove.add(containedRes);
		}
		iter.close();

		List<Resource> resList = new ArrayList<Resource>();
		for (Resource containedRes : classMembersToRemove) {
			reloRdf.removeStatements(containedRes, null, null);
			resList.add(containedRes);
		}
		reloRdf.removeStatements(classRes, null, null);
		resList.add(classRes);
		return resList;
	}
    

	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		Type classType = AsmUtil.internalNameToType(name);
		//if (signature != null)
		//	new SignatureReader(signature).accept(new AsmSignatureVisitor(reloRdf, classRes));
		this.fqClassName = name;
		this.classLabel = name.substring(name.lastIndexOf('/') + 1);
		this.rdfClassname = AsmUtil.getRdfClassName(name);
		this.classRes = addClassOrInterface(classType, access, projectRes);
		this.dss.updateSrcClassCache(this.classRes);
		rdfRepo.addNameStatement(classRes, RSECore.name, classLabel);
		signature = translateClassSig(signature);
		if (signature!=null)
			rdfRepo.addStatement(classRes, RSECore.classSig, signature);
		addInheritance(superName, false);
		for (String iface : interfaces) {
			addInheritance(iface, true);
		}
	}


	private String translateClassSig(String sig) {
		if (sig==null || !sig.startsWith("<")) return null;
		
		while (sig.contains(":")) {
			int colonNdx = sig.indexOf(":");
			int semiColonNdx = sig.indexOf(";")+1;
			try { // we support simple generics like <K> or <K,V>
				String type = sig.substring(colonNdx, semiColonNdx);
				sig = sig.replace(type, ",");
			} catch (Throwable t) {
//				for complicated/nested  generics such as "<K::Ljava/lang/Comparable<-TK;>;V:Ljava/lang/Object;>Ljava/lang/Object;"
				return null;
			}
		}
		sig = sig.substring(0, sig.lastIndexOf(">")+1);
		sig = sig.replace(",>", ">");
		return sig;
	}

	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		return PluggableAsmAnnotationVisitorSupport.getClassVisitors(rdfRepo, classRes, desc);
	}


	public void visitAttribute(Attribute attr) {
		PluggableAsmAnnotationVisitorSupport.doneVisitingAnnotations(rdfRepo, projectRes, classRes);
	}


	public void visitEnd() {
		PluggableAsmAnnotationVisitorSupport.doneVisitingAnnotations(rdfRepo, projectRes, classRes);
	}


	public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
		PluggableAsmAnnotationVisitorSupport.doneVisitingAnnotations(rdfRepo, projectRes, classRes);
		//if (desc != null)
		//	new SignatureReader(desc).accept(new AsmSignatureVisitor(reloRdf, classRes));
		//if (signature != null)
		//	new SignatureReader(desc).accept(new AsmSignatureVisitor(reloRdf, classRes));
		Resource fieldRes = AsmUtil.toWkspcResource(rdfRepo, rdfClassname + "." + name);
		rdfRepo.addStatement(classRes, RSECore.contains, fieldRes);
		rdfRepo.addNameStatement(fieldRes, RSECore.name, name);
		rdfRepo.addStatement(fieldRes, RJCore.access, AsmUtil.getAccessModifierResource(access));

		Type fieldType = Type.getType(desc);
		Resource fieldTypeRes = AsmUtil.toReloClassResource(rdfRepo, fieldType);
		dss.storeNCacheFieldType(fieldRes, RJCore.refType, fieldTypeRes);
		rdfRepo.addStatement(fieldRes, RJCore.srcResource, sourceResource);
		rdfRepo.addTypeStatement(fieldRes, RJCore.fieldType);
		rdfRepo.addInitializedStatement(fieldRes, RSECore.initialized, true);

		return null;
	}

	public void visitInnerClass(String name, String outerName, String innerName, int access) {
		PluggableAsmAnnotationVisitorSupport.doneVisitingAnnotations(rdfRepo, projectRes, classRes);
		if (outerName == null)
			outerName = name.substring(0, name.lastIndexOf("$"));

		Type outerType = AsmUtil.internalNameToType(outerName);
		Type innerType = AsmUtil.internalNameToType(name);

		rdfRepo.addStatement(AsmUtil.toReloClassResource(rdfRepo, outerType), RSECore.contains, AsmUtil.toReloClassResource(rdfRepo, innerType));
	}

	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		PluggableAsmAnnotationVisitorSupport.doneVisitingAnnotations(rdfRepo, projectRes, classRes);
		
		StringBuilder buff = new StringBuilder(rdfClassname).append('.');

		String displayName = name;
		boolean anonymousMethod = false;
		if (displayName.equals("<init>")) {
			displayName = classLabel;

			// for nested classes
			if (displayName.contains("$"))
				displayName = displayName.substring(displayName.lastIndexOf('$') + 1);
		}
		
		// for anonymous methods
		if (isAnonMethod(displayName)) {
			anonymousMethod = true;
			displayName = displayName.substring(displayName.lastIndexOf('$') + 1);
		}
		
		buff.append(AsmUtil.getMethodSignature(displayName, desc));

		Resource methodRes = AsmUtil.toWkspcResource(rdfRepo, buff.toString());

		//new SignatureReader(desc).accept(new AsmSignatureVisitor(reloRdf, methodRes));

		rdfRepo.addStatement(classRes, RSECore.contains, methodRes);
		rdfRepo.addNameStatement(methodRes, RSECore.name, displayName);
		rdfRepo.addStatement(methodRes, RJCore.access, AsmUtil.getAccessModifierResource(access));
		rdfRepo.addTypeStatement(methodRes, RJCore.methodType);
		rdfRepo.addStatement(methodRes, MethodParametersSupport.parameterCachedLabel, AsmUtil.getMethodSignature(null, desc));
		rdfRepo.addStatement(methodRes, RJCore.srcResource, sourceResource);
		if (anonymousMethod) {
			rdfRepo.addStatement(methodRes, AsmMethodSupport.anonymousMethodType, true);
		}

		Type[] types = Type.getArgumentTypes(desc);

		if (types.length > 0) {
			List<Resource> params = new ArrayList<Resource>(types.length);

			for (Type t : types) {
				Resource paramResource = AsmUtil.toReloClassResource(rdfRepo, t);
				dss.storeNCacheMethTypeWeak(methodRes, RJCore.refType, paramResource);
				params.add(paramResource);
			}

			rdfRepo.addStatement(methodRes, RJCore.parameter, rdfRepo.createList(params));

			Resource retTypeResource = AsmUtil.toReloClassResource(rdfRepo, Type.getReturnType(desc));
			dss.storeNCacheMethTypeWeak(methodRes, RJCore.refType, retTypeResource);
			rdfRepo.addStatement(methodRes, RJCore.returnType, retTypeResource);
		}

		return new AsmMethodSupport(rdfRepo, dss, methodRes, projectRes);
	}

	private static boolean isAnonMethod(String displayName) {
		if (displayName.contains("$")) {
			String subString = displayName.substring(displayName.lastIndexOf('$') + 1);
			if (subString.equals("") || Character.isDigit(subString.charAt(0))) 
				return true;
		}
		return false;
	}

	public void visitOuterClass(String owner, String name, String desc) {
		PluggableAsmAnnotationVisitorSupport.doneVisitingAnnotations(rdfRepo, projectRes, classRes);
		// For some reason this guy never gets called in pre-1.5 classes.
		// LOG.info("visitOuterClass() owner=" + owner + ", name=" + name + ",
		// desc=" + desc);
	}

	/**
	 * Tries to use the ICompilationUnit if available, otherwise creates a fake
	 * entry based upon the class-name and package path.
	 */
	public void visitSource(String source, String debug) {
		if (eclipseClassResource == null) {
			sourceResource = getEncodedResource(source);
			rdfRepo.addNameStatement(sourceResource, RSECore.name, 
					fqClassName.substring(0, fqClassName.lastIndexOf('/') + 1) + source);
		} else {
			sourceResource = RSECore.eclipseResourceToRDFResource(rdfRepo,eclipseClassResource);
			rdfRepo.addNameStatement(sourceResource, RSECore.name, eclipseClassResource.getName());
		}

		if (sourceResource == null) {
			logger.error("Source Resource for " + this.classLabel + " is NULL");
		}

		rdfRepo.addStatement(classRes, RJCore.srcResource, sourceResource);
	}

	private Resource addClassOrInterface(Type type, int access, Resource projectResource) {
		Resource classRes = AsmUtil.getClassRes(type.getClassName(), rdfRepo);
		if (AsmUtil.isPrimitive(type)) {
			return classRes;
		}

		Resource packageRes = AsmPackageSupport.getPackage(rdfRepo, type, projectResource);
//		boolean isInitialized = RSECore.isInitialized(rdfRepo, packageRes);
//		if (!isInitialized) return classRes;
		dss.updateSrcFldrCache(packageRes);

		// don't add package contains if this is an inner class
		if (this.fqClassName.indexOf("$") == -1)
			rdfRepo.addStatement(packageRes, RSECore.contains, classRes);

		if ((access & Opcodes.ACC_INTERFACE) == Opcodes.ACC_INTERFACE) {
			rdfRepo.addStatement(classRes, RJCore.isInterface, RJCore.interfaceType);
		} else if ((access & UNKNOWN_ACCESS) == UNKNOWN_ACCESS) {
			// do nothing
		} else {
			 rdfRepo.addTypeStatement(classRes, RJCore.classType);
		}

		if ((access & UNKNOWN_ACCESS) == 0) {
			rdfRepo.addStatement(classRes, RJCore.access, AsmUtil.getAccessModifierResource(access));
		}

		rdfRepo.addNameStatement(classRes, RSECore.name, AsmUtil.getShortClassname(type));
		rdfRepo.addInitializedStatement(classRes, RSECore.initialized, true);

		return classRes;
	}

	private void addInheritance(String superName, boolean interfaceType) {
		if ((superName == null) || superName.equals(OBJECT_TYPE_NAME)) {
			return;
		}

		Resource superResource = AsmClassUtil.getType(rdfRepo, AsmUtil.internalNameToType(superName).getClassName(), interfaceType);
//		boolean isInitialized = RSECore.isInitialized(rdfRepo, superResource);
//		if (!isInitialized) return;

		if (interfaceType)
			dss.storeNCacheTypeType(classRes, RJCore.refType, superResource);
		dss.storeNCacheTypeType(classRes, RJCore.inherits, superResource);
	}

	private Resource getEncodedResource(String s) {
		StringBuilder buff = new StringBuilder(100);
		buff.append(RJCore.jdtWkspcNS).append("/").append(projName);

		if (s.length() > 0) {
			buff.append("/").append(s);
		}

		return RSECore.createRseUri(ReloRdfRepository.atxaRdfNamespace
				+ RSECore.encodeId(s));
	}
}
