package com.architexa.diagrams.jdt;

import org.apache.log4j.Logger;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IInitializer;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeParameter;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.internal.core.SourceType;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;

import com.architexa.store.ReloRdfRepository;
import com.architexa.store.StoreUtil;

public class RJMapToId {
    private static final Logger logger = Activator.getLogger(RJMapToId.class);

    private static String objectParamType = "Object";

    /* Conversion between main types in the RSE-JDT world...
	 * Main participants: 
	 *  Resource (from RDF), 
	 *  IBinding (via ASTNode.resolveBinding() - provided by ASTParser), 
	 *  IJavaElemment (via Eclipse JDT)
	 * 
	 * IBinding -> Resource
	 * IJavaElement -> Resource
	 * Resource -> IJavaElement
	 * Resource -> IBinding/ASTNode [not providing since it doesn't make sense]
	 * IJavaElement <=> IBinding/ASTNode [needed only by the system, so not worrying]
	 */
	
    private interface IdSource {
        // returns closes element that can be instantiated 
        public IdSource getGroundedSrc();
        public boolean isNull();
        public String getName();
        public boolean isPackage(); 
        public boolean isType(); 
        public boolean isNestedType(); 
        public boolean isInitializer();
        public boolean isMethod(); 
        public boolean isMethodCons();
        public IdSource getParent();
        public String getDbgID();
        public Class<?> getDbgWrappedType();
        
        // we do a small optimization by not including the fully qualified name
		// for the param. types and only including the type name
        public String getParameterTypes(String delim);
        
        public String getErasedTypeName();
    };
    
    private static class BindingWrapper implements IdSource {
        private final IBinding binding;
        public BindingWrapper(IBinding binding) { this.binding = binding; }
        public boolean isNull() { return (binding==null); }
        public String getName() { return binding.getName(); }
        public boolean isPackage() { return (binding instanceof IPackageBinding); } 
        public boolean isInitializer() { return false; }
        public boolean isMethod() { return (binding instanceof IMethodBinding); } 
        public boolean isMethodCons() { return (binding instanceof IMethodBinding && ((IMethodBinding)binding).isConstructor()); } 
        public boolean isType() { return (binding instanceof ITypeBinding); } 
        public boolean isNestedType() { return (binding instanceof ITypeBinding && ((ITypeBinding)binding).isNested()); } 
        public IdSource getGroundedSrc() {
            // all bindings that we get should be instantiatable
            return this;
        }
        public IdSource getParent() {
            IBinding parentBinding = null;
            if (binding instanceof ITypeBinding) {
                parentBinding = ((ITypeBinding) binding).getDeclaringClass();
                if (parentBinding == null) parentBinding = ((ITypeBinding) binding).getPackage();
            } else if (binding instanceof IMethodBinding)
                parentBinding = ((IMethodBinding) binding).getDeclaringClass();
            else if (binding instanceof IVariableBinding)
                parentBinding = ((IVariableBinding) binding).getDeclaringClass();
            else
            	logger.error("Parent Id requested for unexpected binding type: " + binding.getClass());

            return new BindingWrapper(parentBinding);
        }
        public String getParameterTypes(String delim) {
            String types = "";
            ITypeBinding[] paramTypes = ((IMethodBinding) binding).getParameterTypes();
            for (int i=0; i<paramTypes.length; i++) {
                types += paramTypes[i].getErasure().getName() + delim;
            }
            return types;
        }
        public String getErasedTypeName() {
        	// the assumption is that we have a type binding
        	return ((ITypeBinding)binding).getErasure().getName();
        }
        public String getDbgID() {
            return binding.getKey();
        }
        public Class<?> getDbgWrappedType() {
            return binding.getClass();
        }
    };
    
    private static class JDTElementWrapper implements IdSource {
        private final IJavaElement element;
        public JDTElementWrapper(IJavaElement element) {
    		IJavaElement ije = element;

    		if (element instanceof IClassFile) {
				try {
					ije = ((IClassFile) element).getType();
				} catch (Throwable e) {
					logger.error("Unexpected Error while obtaining type for: " + ((IClassFile)element).getHandleIdentifier(), e);
				}
			}

       		this.element = ije;
        }
        public boolean isNull() { return (element==null); }
        public String getName() {
        	if (element instanceof IType && element.getElementName().length() == 0) {
        		return "" + getParentAnonClassCnt((IType) element);
        	}
        	if(element instanceof IInitializer) return "<clinit>";
        	if(element.getElementName().length()==0) return "(default)";
            return element.getElementName(); 
        }
        public boolean isPackage() { return (element instanceof IPackageFragment); }
        public boolean isType() { return (element instanceof IType); } 
        public boolean isNestedType() {
			try {
				if (!(element instanceof IType)) return false;
				return IJEUtils.isNestedType((IType)element);
			} catch (JavaModelException e) {
				logger.error("Error while trying to get id", e);
				return false;
			}
		} 
        public boolean isAnonConstructor() {
        	if (element instanceof AnonymousClassConstructor) 
        		return true;
        	return false;
        }
        public boolean isInitializer() { return (element instanceof IInitializer); }
        public boolean isMethod() { return (element instanceof IMethod); }
        public boolean isMethodCons() {
			try {
				return (element instanceof IMethod && ((IMethod) element).isConstructor());
			} catch (JavaModelException e) {
				logger.error("Error while trying to get id", e);
				return false;
			}
		} 
        private boolean isClassFile() { return (element instanceof IClassFile); }
        public IdSource getGroundedSrc() {
            if (element == null) return null;

            if (element instanceof IType 
            		|| element instanceof IInitializer
                    || element instanceof IMethod
                    || element instanceof IField
                    || element instanceof IPackageFragment )
                return this;
            if(isClassFile()) {
            	return new JDTElementWrapper(((IClassFile)element).getPrimaryElement());
            }

            if (element instanceof ICompilationUnit)
                return new JDTElementWrapper(((ICompilationUnit)element).findPrimaryType());

            return (new JDTElementWrapper(element.getParent())).getGroundedSrc();
        }
        public IdSource getParent() {
            IJavaElement parentElement = ((IMember) element).getDeclaringType();
            if (parentElement == null) parentElement = ((IType) element).getPackageFragment();
            return new JDTElementWrapper(parentElement);
        }
        public String getParameterTypes(String delim) {
        	// Handling for generic methods like:
        	// public <T> void fromArrayToCollection(T[] a, Collection<T> c) { ...
        	// or
        	// public class MyClass<S> { public void findList(Blueprint blueprint, 
        	// S searchRecord, int startPosition, int resultSize) { ...
        	// 
        	// The eclipse compiler will replace T/S with Object in the method's
        	// description that the architexa builder then uses to create the method's
        	// Resource. So we need to do the same thing now (replace T/S with Object)
        	// when creating the String of parameter types used to create a Resource
        	// for a method binding or ijavaelement. For the above two examples, the
        	// parameter types string should look like Object, Collection
        	// and Blueprint, Object, int, int

        	IType declaringType = ((IMethod)element).getDeclaringType();

        	String types = "";
        	String[] paramTypes = ((IMethod)element).getParameterTypes();
        	for (int i=0; i<paramTypes.length; i++) {

        		// If method param includes a generic type anywhere, replace it with 
        		// "Object" to match eclipse compiler's handling of generics in params

        		// Handle changing parameter like foo(T) -> foo(Object)
        		String simpleParamType = Signature.getTypeErasure(Signature.getSignatureSimpleName(paramTypes[i]));
        		if(methodParamTypeIsGeneric(declaringType, (IMethod)element, simpleParamType))
        			simpleParamType = objectParamType ;

        		// If have a parameter like foo(Collection<T>), not necessary to try to
        		// change Collection<T> -> Collection<Object> because the compiler just
        		// treats that param as "Collection" without the type parameter. 
        		// simpleParamType at this point is just "Collection" so there is no 
        		// generic type to replace. Code to change Collection<T> to
        		// Collection<Object> is below and commented out in case it is ever needed. 
//        		String[] typeArgsOfParam = Signature.getTypeArguments(paramTypes[i]);
//        		for(String ta : typeArgsOfParam) {
//        			String taSimpName = Signature.getSignatureSimpleName(ta);
//        			if(methodParamTypeIsGeneric(declaringType, ((IMethod)element), taSimpName))
//        				simpleParamType = simpleParamType.replace(taSimpName, objectParamType);
//        		}

        		// Handle changing parameter like foo(T[]) -> foo(Object[])
        		String simpleArrayParamType = Signature.getSignatureSimpleName(Signature.getElementType(paramTypes[i]));
        		if(methodParamTypeIsGeneric(declaringType, (IMethod)element, simpleArrayParamType))
        			simpleParamType = simpleParamType.replace(simpleArrayParamType, objectParamType);

        		types += simpleParamType + delim;
        	}
        	return types;
        }
        /**
         * 
         * Returns true if either:
         * the given method is a generic method, for example,
         * public <T> void myMethod(T[] a, Collection<T> c) { ...
         * OR
         * the method's declaring class is a generic class with 
         * a generic type that matches the type of the given method parameter, for
         * example, public class MyClass<S> { public foo(S bar) ...
         * Returns false otherwise
         * @param declaringClass is MyClass<S>
         * @param method is myMethod(T[] a, Collection<T> c) or foo(S bar)
         * @param methodParam is T or S
         */
        private boolean methodParamTypeIsGeneric(IType declaringClass, 
        		IMethod method, String methodParam) {

        	// First test whether method is a generic method whose 
        	// type parameter matches the given method parameter
        	try {

        		// The call getTypeParameters() requires the method to have
        		// element info, which is null for an AnonymousClassConstructor
        		// and will cause a newNotPresentException
        		if(method instanceof AnonymousClassConstructor) return false;

        		for(ITypeParameter pa: method.getTypeParameters()) {
        			// For public <T> void myMethod(T[] a, Collection<T> c), simpleSig is T
        			String simpleSig = Signature.getTypeErasure(pa.getElementName());
        			if(simpleSig!=null && simpleSig.equals(methodParam)) 
        				return true;
        		}
        	} catch (Exception e) {
        		logger.error("Unexpected exception while getting " +
        				"return type arguments for method "+method, e);
        	}

        	// Not a matching generic method, so see if the method is in a generic
        	// class with a type parameter that matches the given method parameter
        	if(declaringClass==null) return false;
        	try {
        		for(ITypeParameter declaringTypeParam : declaringClass.getTypeParameters()) {
        			// For public class MyClass<S>, simpleSig is "S"
        			String simpleSig = declaringTypeParam.getElementName();
        			if(simpleSig!=null && simpleSig.equals(methodParam)) 
        				return true;
        		}
        	} catch (Exception e) {
        		logger.error("Unexpected exception while getting formal " +
        				"type parameter signatures for type "+declaringClass);
        	}
        	return false;
        }
        public String getErasedTypeName() {
        	// the assumption is that we have a type
            return Signature.getTypeErasure( ((IType)element).getElementName() );
        }
        public String getDbgID() {
            return element.getHandleIdentifier();
        }
        public Class<?> getDbgWrappedType() {
            return element.getClass();
        }
    	/**
    	 * Given an anon inner class, this method returns the count that the parent
    	 * has for this class.
    	 * 
    	 * This is done by finding the parent method and the parent class, then
    	 * going through all the methods in the parent class, to add up all the
    	 * previous anon classes.
    	 */
    	private static int getParentAnonClassCnt(IType anonClass) {
    		try {
	    		IMember anonClassContainerMember = null;
				IType parentClass = null;
				if (anonClass.getParent() instanceof IMember) {
	    			anonClassContainerMember = (IMember) anonClass.getParent();
	    			parentClass = anonClassContainerMember.getDeclaringType();
	    		} else if (anonClass.getParent() instanceof IClassFile) {
					parentClass = ((IClassFile)anonClass.getParent()).getType();
					if (!parentClass.exists()) parentClass = null;
	    			logger.error("Adding Anonymous inner class to a CLASS not inside a method: " + anonClass.getFullyQualifiedName());
	    		}
				if (parentClass == null) {
	    			logger.error("Parent Class not foud for Anonymous inner class: " + anonClass.getFullyQualifiedName());
					return 0;
				}
	    		int parentAnonClassCnt = anonClass.getOccurrenceCount();
	            //for Eclipse 3.1 compatability
	            //int parentAnonClassCnt = ((SourceRefElement)anonClass).occurrenceCount;
    			boolean foundAnonClassContainerMember = false;
    			for (IField parentClassChildFld : parentClass.getFields()) {
    				if (parentClassChildFld.equals(anonClassContainerMember)) foundAnonClassContainerMember = true;
    				if (foundAnonClassContainerMember) break;
    				for (IJavaElement fldChild : parentClassChildFld.getChildren()) {
    					if (fldChild instanceof IType && ((IType)fldChild).isAnonymous())  parentAnonClassCnt++;
    				}
    			}
    			for (IMethod parentClassChildMeth : parentClass.getMethods()) {
    				if (parentClassChildMeth.equals(anonClassContainerMember)) foundAnonClassContainerMember = true;
    				if (foundAnonClassContainerMember) break;
    				for (IJavaElement methChild : parentClassChildMeth.getChildren()) {
    					if (methChild instanceof IType && ((IType)methChild).isAnonymous())  parentAnonClassCnt++;
    				}
    			}
        		return parentAnonClassCnt;
    		} catch (JavaModelException e) {	logger.error("Unexpected Exception", e);	}
    		return 0;
    	}
    };

    private static String getId(IdSource idSrc) {
        try {
            if (!idSrc.isNull()) idSrc = idSrc.getGroundedSrc();
            if (idSrc==null || idSrc.isNull()) return "";
            
            String bindingId = null;
            
            boolean isAnon = false;
			if (idSrc.isPackage()){
                bindingId = idSrc.getName();
            }else if (idSrc.isType()){
                bindingId = getId(idSrc.getParent());
                if (bindingId.equals("(default)")) bindingId = "";
                	bindingId += "$" + idSrc.getName();
            }else if (idSrc instanceof JDTElementWrapper && ((JDTElementWrapper) idSrc).isAnonConstructor()) {
            	bindingId = getId(idSrc.getParent()) + "." + idSrc.getParent().getName();
            	isAnon  = true;
            } else
                bindingId = getId(idSrc.getParent()) + "." + idSrc.getName();
            
            // add parameters names
			if (idSrc.isInitializer()) {
				bindingId += "()"; // static initializers take no parameters
			} else if (idSrc.isMethod()) {
                bindingId += "(";
                
                // If it is an anonymous class not defined by a static member, 
                // add the declaring class name as the first parameter
                if (isAnon && !RJCore.isStatic((IMember)((JDTElementWrapper)idSrc).element.getParent())) {
                	String[] classNameStrings = bindingId.split("\\$");
                	// get name of the declaring class (i.e. string between last and second to last "$")
                	String classContainingAnonClass = classNameStrings[classNameStrings.length - 2];
                	bindingId = bindingId+classContainingAnonClass+",";
                	
                   	// If the anonymous class extends or implements a class that is an 
                	// inner class inside a different class than the one in which the 
                	// anonymous class is defined, for example
        			/*public class C {
						public void method() {
							OuterClass outer = new OuterClass();
							OuterClass.InnerClass anonInner = outer.new InnerClass() {};
						}
					}*/
                	// then we also need to include in the binding id the name of the 
                	// the outer class that contains the inner class that the anon class 
                	// is an instance of
                	ReloRdfRepository repo = StoreUtil.getDefaultStoreRepository();
                	IJavaElement parent = ((JDTElementWrapper)idSrc).element.getParent();
                	Resource parentRes = RJCore.jdtElementToResource(repo, parent);
                	Resource superClass = (Resource) repo.getStatement(parentRes, RJCore.inherits, null).getObject();
                	IJavaElement superClassElt = RJCore.resourceToJDTElement(repo, superClass);
                	if(superClassElt!=null && superClassElt.getParent() instanceof SourceType) {
                		// anon class extends or implements an inner class.. 
                		String outerClassName = superClassElt.getParent().getElementName();
                		if(!classContainingAnonClass.equals(outerClassName)) {
                			// ..and that inner class is contained in a class 
                			// different than the declaring class (which was 
                			// appended above), so also append the outer class name
                			bindingId += outerClassName+",";
                		}
                	}
                }
                
                // add this for nested classes if we are in a constructor
				// we need to do a getParent() twice to: constructor -> nested
				// class -> parent class
                if (idSrc.getParent().isNestedType() && idSrc.isMethodCons()
                		&& !isAnon) // anon handled above
                	bindingId += idSrc.getParent().getParent().getErasedTypeName() + ",";

                bindingId += idSrc.getParameterTypes(",") + ")";
            }
            return bindingId;
        } catch (Throwable t) {
            logger.error("Error while trying to getId " + idSrc.getDbgID() + " type: " + idSrc.getDbgWrappedType(), t);
            return "";
        }
    }

    public static String getId(IBinding binding) {
    	return getId(new BindingWrapper(binding));
    }

    public static String getId(IJavaElement element) {
    	return getId(new JDTElementWrapper(element));
    }
	public static String getId(Resource res) {
        if (!(res instanceof URI))
        	throw new IllegalArgumentException();
        else
            return ((URI)res).getLocalName();
	}

	public static final String pckgIDToPckgFldrID(String id) {
		return id + ".*";
	}

	public static final boolean isPckgID(String tgtAFName) {
		return tgtAFName.endsWith(".*");
	}

	
	public static String getParentPackage(String elemName) {
		if(elemName == null) return null;
		
		int ndx = -1;
		
		ndx = elemName.lastIndexOf('$');
		if (ndx == 0 ) return "(default)";
		if (ndx != -1) return elemName.substring(0, ndx);
		
		ndx = elemName.lastIndexOf('.');
		if (ndx == -1) return null;
		return elemName.substring(0, ndx);
	}
}
