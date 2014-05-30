package com.architexa.diagrams.jdt;


import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.SourceMethod;
import org.openrdf.model.Resource;
import org.openrdf.model.Value;

import com.architexa.diagrams.model.Artifact;
import com.architexa.store.ReloRdfRepository;
import com.architexa.store.StoreUtil;

/**
 * IBinding.getJavaElement() returns null in cases where the binding 
 * corresponds to the constructor of an anonymous class. Use
 * AnonymousClassConstructor to represent the java element for such
 * constructors.
 * 
 * @author Elizabeth L. Murnane
 *
 */
public class AnonymousClassConstructor extends SourceMethod implements IMethod {

	private static final Logger logger = Activator.getLogger(AnonymousClassConstructor.class);

	IMethod superclassConstructor;
	IMethodBinding superclassConstructorBinding;
	IType anonClassDecl;

	String name = Signature.SIG_VOID;
	List<String> argumentTypes = new ArrayList<String>();
	List<Expression> passedArguments = new ArrayList<Expression>();

	/**
	 * 
	 * @param constructor The java element of the constructor 
	 * that was invoked to create the anonymous class
	 * @param superclassConstructorBinding The method binding of the constructor
	 * that was invoked to create the anonymous class
	 * @param anonClassASTNode The AST node of the anonymous class declaration
	 * @param name 
	 * @throws JavaModelException 
	 */
	public AnonymousClassConstructor(IMethod constructor, IMethodBinding superclassConstructorBinding, 
			AnonymousClassDeclaration anonClassASTNode) throws JavaModelException {
		super((JavaElement)anonClassASTNode.resolveBinding().getJavaElement(), constructor.getElementName(), constructor.getParameterTypes());
		this.superclassConstructor = constructor;
		this.superclassConstructorBinding = superclassConstructorBinding;
		this.anonClassDecl = (IType)anonClassASTNode.resolveBinding().getJavaElement();

		if (anonClassASTNode.getParent() instanceof ClassInstanceCreation) {
			ClassInstanceCreation classCreation = (ClassInstanceCreation)anonClassASTNode.getParent();
			for(Object arg : classCreation.arguments()) {
				passedArguments.add((Expression)arg);
			}
		}
		for(ITypeBinding param : superclassConstructorBinding.getParameterTypes()) {
			argumentTypes.add(param.getName());
		}
		if (anonClassDecl.getSuperclassName() != null) {
			this.name = anonClassDecl.getSuperclassName();
			this.name = this.name+"(";
			String arguments = "";
			int i=0;
			while(i < argumentTypes.size()) {
				String argName = argumentTypes.get(i);
				if(i>0) arguments = arguments+", ";
				arguments = arguments+argName;
				i++;
			}
			this.name = this.name+arguments+")";
		}
	}

	/**
	 * 
	 * @return the declaration of the constructor of the class that
	 * the anonymous class extends or implements
	 * 
	 * If the constructor is compiler generated, as in the case when
	 * the anonymous class's superclass is an interface, the returned
	 * method will be of type CompilerGeneratedDefaultConstructor
	 */
	public IMethod getSuperclassConstructor() {
		return superclassConstructor;
	}

	/**
	 * 
	 * @return the method binding of the constructor of the class that
	 * the anonymous class extends or implements
	 * 
	 */
	public IMethodBinding getSuperclassConstructorBinding() {
		return superclassConstructorBinding;
	}

	public ImplicitConstructorCallLocation getImplicitCallToSuperConstructor() {
		return new ImplicitConstructorCallLocation(
				this, getSuperclassConstructor(), getSuperclassConstructorBinding());
	}

	public IType getAnonymousClassDeclaration() {
		return anonClassDecl;
	}

	public String getName(){
		return name;
	}

	/**
	 * 
	 * @return the position of this anonymous class among all anonymous 
	 * classes defined in the source. Numbering starts at 1 (i.e. the
	 * first anonymous class declared in the source has an occurrence
	 * position of 1).
	 */
	public String getAnonClassOccurrencePosition() {
		ReloRdfRepository repo = StoreUtil.getDefaultStoreRepository();
		Resource anonClassRes = RJCore.jdtElementToResource(repo, anonClassDecl);
		String name = repo.queryName(anonClassRes);
		if (name!=null)
			return name;
		
		String anonClassString = anonClassDecl.toString();
		int numIndex = anonClassString.indexOf("#")+1;
		if(numIndex>0) // (if doesn't contain '#', numIndex will equal 0)
			return anonClassString.substring(numIndex, numIndex+1);

		return "";
	}

	/**
	 * 
	 * @return a list of the types that this anonymous
	 * class constructor takes as arguments
	 */
	public List<String> getArgumentTypes() {
		return argumentTypes;
	}

	/**
	 * 
	 * @return a list of the actual values passed
	 * as arguments in the call made to create this
	 * anonymous class
	 */
	public List<Expression> getPassedArguments() {
		return passedArguments;
	}

	@Override
	public String getReturnType() throws JavaModelException {
		return Signature.SIG_VOID;
	}

	@Override
	public boolean isConstructor() throws JavaModelException {
		return true;
	}

	@Override
	public String toString() {
		String s = super.toString();
		s = toStringIfAnonImplementsInterface(s);
		s = s.replace("(not open)", "");
		return s;
	}

	public String getAnonClassNavAidString(String callLocationText) {
		int endOfTextBeforeBrace = callLocationText.indexOf("{");
		if(endOfTextBeforeBrace==-1) endOfTextBeforeBrace = callLocationText.length();
		String anonClassText = callLocationText.substring(0, endOfTextBeforeBrace);
		String occurrencePosition =	getAnonClassOccurrencePosition();
		anonClassText = anonClassText.trim()+" #"+occurrencePosition;
		return anonClassText;
	}

	// super.toString() will return Object() since this anon class implements
	// an interface. Don't want "Object()", want "[interface name]()"
	private String toStringIfAnonImplementsInterface(String s) {
		ReloRdfRepository repo = StoreUtil.getDefaultStoreRepository();
		Resource anonClassDeclRes = RJCore.jdtElementToResource(repo, anonClassDecl);
		Value superclass = repo.getStatement(anonClassDeclRes, RJCore.inherits, null).getObject();
		if(superclass==null) return s;

		Artifact tempSuperclassArt = new Artifact((Resource)superclass);
		Resource type = tempSuperclassArt.queryType(repo);
		if(!RJCore.interfaceType.equals(type)) return s;

		String interfaceName = "super";
		try {
			interfaceName = anonClassDecl.getSuperclassName();
		} catch (JavaModelException e) {
			logger.error("Unexpected exception when getting super " +
					"class name of anonymous class " + anonClassDeclRes, e);
		}
		s = s.replace("Object()", interfaceName+"()");
		return s;
	}
}
