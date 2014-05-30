package com.architexa.diagrams.jdt.builder.asm;

import org.apache.log4j.Logger;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;

import com.architexa.diagrams.RSECore;
import com.architexa.diagrams.jdt.Activator;
import com.architexa.diagrams.jdt.RJCore;
import com.architexa.store.ReloRdfRepository;


public class AsmMethodSupport implements MethodVisitor {
	static final Logger logger = Activator.getLogger(AsmMethodSupport.class);

	// VS: no annotations support for now 
	//private final AnnotationVisitor annotationVisitor;

    private static final URI createReloURI(String str) {
    	return RSECore.createRseUri(str);
    }
	public static final URI anonymousMethodType = createReloURI("jdt#anonymousMethod");

	// private volatile int sourceEnd = -1;
	// private volatile int sourceStart = -1;
	private Resource methodRes;
	private Resource projectRes;
	
	private ReloRdfRepository rdfRepo;

	private final DepAndChildrenStrengthSummarizer dss;

	public AsmMethodSupport(ReloRdfRepository repo, DepAndChildrenStrengthSummarizer dss, Resource methodRes, Resource projectRes) {
		this.rdfRepo = repo;
		this.dss = dss;
		//this.annotationVisitor = new AsmAnnotationVisitor(repo);
		this.methodRes = methodRes;
		this.projectRes = projectRes;
	}

	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		return PluggableAsmAnnotationVisitorSupport.getMethdVisitors(rdfRepo, methodRes, desc);
	}

	public AnnotationVisitor visitAnnotationDefault() {
		//return annotationVisitor;
		return null;
	}

	public AnnotationVisitor visitParameterAnnotation(int parameter, String desc, boolean visible) {
		//return annotationVisitor;
		return null;
	}
	
	public void visitAttribute(Attribute attr) {
	}

	public void visitCode() {
		PluggableAsmAnnotationVisitorSupport.doneVisitingAnnotations(rdfRepo, projectRes, methodRes);
	}

	public void visitEnd() {
		rdfRepo.addInitializedStatement(methodRes, RSECore.initialized, true);
	}

	public void visitFieldInsn(int opcode, String owner, String name, String desc) {
		Resource fieldRes = AsmUtil.toWkspcResource(rdfRepo, AsmUtil.getRdfClassName(owner) + "." + name);
		// below test removed now that we provide preference for showing library code
//		boolean isInitialized = RSECore.isInitialized(rdfRepo, fieldRes);
//		if (!isInitialized) return;
		dss.storeNCacheMethType(methodRes, RJCore.refType, fieldRes);
	}

	public void visitIincInsn(int var, int increment) {
	}

	public void visitInsn(int opcode) {
	}

	public void visitIntInsn(int opcode, int operand) {
	}

	public void visitJumpInsn(int opcode, Label label) {
	}

	public void visitLabel(Label label) {
	}

	public void visitLdcInsn(Object cst) {
	}

	public void visitLineNumber(int line, Label start) {
		// if(sourceStart < 0) {sourceStart = line;}
		//
		// if(sourceEnd < line) {sourceEnd = line;}
	}

	public void visitLocalVariable(
			String name, String desc, String signature,
			Label start, Label end, int index) {
		Resource varType = AsmUtil.toReloClassResource(rdfRepo, Type.getType(desc));
		// below test removed now that we provide preference for showing library code
//		boolean isInitialized = RSECore.isInitialized(rdfRepo, varType);
//		if (!isInitialized) return;
		dss.storeNCacheMethType(methodRes, RJCore.refType, varType);
	}

	public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
	}

	public void visitMaxs(int maxStack, int maxLocals) {
	}

	// visit a method call 
	public void visitMethodInsn(int opcode, String owner, String name, String desc) {
		/*if(methodRes.toString().contains("<clinit>") &&
				Opcodes.???==opcode) {
			// Only care about a method call made within a static 
			// initialization block, not a method call to initialize 
			// a static field made from outside the block (i.e. 
			// directly within a class definition)
			// For example:
			// class Foo {
			//    static Foo MY_FOO = createFoo(); 
			//    static Bar MY_BAR = createBar();   (1)
			//    static { 
			//      start();                         (2)
			//      MY_FOO.foo();                    (3)
			//      MY_BAR.bar();                    (4)
			//    }                     
			// }
			// We only want to add statements representing calls like
			// (2), (3), and (4) which have opcodes Opcodes.INVOKESTATIC, 
			// Opcodes.INVOKESPECIAL, and Opcodes.INVOKEVIRTUAL respectively,
			// while ignoring call (1), which has opcode Opcodes.???
			return;
		}*/

		Type ownerType = AsmUtil.internalNameToType(owner);

		if (owner.equals(AsmClassSupport.OBJECT_TYPE_NAME) && name.equals("<init>")) {
			return;
		}

		int idx = name.lastIndexOf('$');
		if (idx != -1) name = name.substring(idx + 1);
		// {these seem to be anonymous *methods* generated by the compiler}

		StringBuilder buff = new StringBuilder(AsmUtil.getRdfClassName(owner)).append('.');

		if (name.equals("<init>")) {
			name = ownerType.getClassName();
			// strip package name
			name = name.substring(name.lastIndexOf('.') + 1);

			// for calls inside nested classes - strip parent name (already in class name)
			if (name.contains("$"))
				name = name.substring(name.lastIndexOf('$') + 1);
		}

		buff.append(AsmUtil.getMethodSignature(name, desc));

		Resource calledMethodRes = rdfRepo.getDefaultURI(RJCore.jdtWkspcNS, buff.toString());
		Resource calledMethodOwnerRes = AsmUtil.toReloClassResource(rdfRepo, ownerType);
		// below test removed now that we provide preference for showing library code
//		boolean isInitialized = RSECore.isInitialized(rdfRepo, calledMethodOwnerRes);
//		if (!isInitialized) return;
		
		dss.storeNCacheMethMeth(methodRes, RJCore.calls, calledMethodRes, calledMethodOwnerRes);
		//dssp.addToRepoAndCacheMethMeth(rdfRepo, methodRes, RJCore.refType, calledMethodRes);
		dss.storeNCacheMethType(methodRes, RJCore.refType, calledMethodOwnerRes);
        //logger.info(methodRes + " --> " + calledMethodRes);

		for (Type t : Type.getArgumentTypes(desc)) {
			dss.storeNCacheMethTypeWeak(methodRes, RJCore.refType, AsmUtil.toReloClassResource(rdfRepo, t));
		}
		Type returnType = Type.getReturnType(desc);
		// only want a ref statement if return type
		// isn't primitive (void, boolean, int, etc)
		if(!AsmUtil.isPrimitive(returnType)) {
			dss.storeNCacheMethTypeWeak(methodRes, RJCore.refType, AsmUtil.toReloClassResource(rdfRepo, returnType));
		}
	}

	public void visitMultiANewArrayInsn(String desc, int dims) {
	}


	public void visitTableSwitchInsn(int min, int max, Label dflt, Label[] labels) {
	}

	public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
	}

	public void visitTypeInsn(int opcode, String desc) {
		Resource typeResource = AsmUtil.toReloClassResource(rdfRepo, AsmUtil.internalNameToType(desc));
		// below test removed now that we provide preference for showing library code
//		boolean isInitialized = RSECore.isInitialized(rdfRepo, typeResource);
//		if (!isInitialized) return;
		dss.storeNCacheMethType(methodRes, RJCore.refType, typeResource);
	}

	public void visitVarInsn(int opcode, int var) {
	}

	public void visitFrame(int arg0, int arg1, Object[] arg2, int arg3, Object[] arg4) {
	}
}
