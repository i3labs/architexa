package com.architexa.diagrams.jdt;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.internal.corext.callhierarchy.CallLocation;

/**
 * 
 * Represents the implicit call to the superclass constructor 
 * that is made when a constructor body does not begin with an 
 * explicit constructor invocation and the constructor being 
 * declared is not part of the primordial class Object.
 * 
 * Also represents the implicit call made to the super class
 * constructor used when defining an anonymous class.
 * 
 */
public class ImplicitConstructorCallLocation extends CallLocation {

	IMethod constructor;
	IMethod superclassConstructor;
	IMethodBinding superclassConstructorBinding;

	/**
	 * 
	 * @param constructor the constructor that either contains no explicit
	 * constructor invocation or that is an anonymous class constructor
	 * @param superConstructor the constructor in the superclass that is implicitly invoked
	 * @param superConstructorBinding the method binding of the constructor in the
	 * superclass that is implicitly invoked
	 */
	public ImplicitConstructorCallLocation(IMethod constructor, IMethod superConstructor, IMethodBinding superConstructorBinding) {
		super(constructor, superConstructor, -1, -1, -1);
		this.constructor = constructor;
		this.superclassConstructor = superConstructor;
		this.superclassConstructorBinding = superConstructorBinding;
	}

	/**
	 * 
	 * @return the superclass constructor that is implicitly invoked
	 */
	public IMethod getSuperclassConstructor() {
		return superclassConstructor;
	}

	/**
	 * 
	 * @return the method binding of the superclass constructor
	 * that is implicitly invoked
	 */
	public IMethodBinding getSuperclassConstructorBinding() {
		return superclassConstructorBinding;
	}

	@Override
	/**
	 * If this represents the implicit call made to the super class
	 * constructor used when defining an anonymous class, returning
	 * the name of the super class constructor method along with the
	 * argument values (not types) passed to it. 
	 * Otherwise, returning "super()" because a constructor body is 
	 * implicitly assumed by the compiler to begin with "super();", an 
	 * invocation of the constructor of its direct superclass that 
	 * takes no arguments.
	 */
	public String getCallText() {
		if(constructor instanceof AnonymousClassConstructor) {
			String paramList = "";
			int i = 0;
			for(Expression passedArg : ((AnonymousClassConstructor)constructor).getPassedArguments()) {
				if(i>0) paramList = paramList.concat(", ");
				paramList = paramList.concat(passedArg.toString());
				i++;
			}
			return superclassConstructor.getElementName()+"("+paramList+")";
		}
		return ("super()");
	}

}
