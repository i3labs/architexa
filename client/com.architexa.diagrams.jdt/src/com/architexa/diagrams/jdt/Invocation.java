package com.architexa.diagrams.jdt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;

import com.architexa.diagrams.jdt.utils.MethodInvocationFinder;
import com.architexa.store.ReloRdfRepository;
import com.architexa.store.StoreUtil;

/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public class Invocation {

	ASTNode invocation;
	IMethodBinding methodBinding;

	// A mapping from each anon class this invocation creates to the calls made 
	// by that anon class. Deeper anon classes made inside any of those calls are
	// NOT included in this Invocation's map; they would be in the map of the call
	// in the anon class that creates the deeper anon class. For example, for
	// baz(new Bar() { run() { bat(new Cat() { car() { x(); } }); y(); } });
	// the Invocation baz maps [Bar -> bat, y] and Invocation bat maps [Cat -> x()]
	private Map<AnonymousClassDeclaration, List<Invocation>> anonClassInvocations = null;

	public Invocation(ASTNode node) {
		if(nodeIsATypeOfInvocation(node)) invocation = node;
	}

	public Invocation(MethodInvocation methodInvocation) {
		invocation = methodInvocation;
	}

	public Invocation(SuperMethodInvocation superMethodInvocation) {
		invocation = superMethodInvocation;
	}

	public Invocation(ConstructorInvocation constructorInvocation) {
		invocation = constructorInvocation;
	}

	public Invocation(SuperConstructorInvocation superConstructorInvocation) {
		invocation = superConstructorInvocation;
	}

	//TODO: Figure out the proper way to associate a super constructor invocation created 
	//via AST.newSuperConstructorInvocation() with its binding
	public Invocation(SuperConstructorInvocation superConstructorInvocation, IMethodBinding binding) {
		invocation = superConstructorInvocation;
		methodBinding = binding;
	}

	public Invocation(ClassInstanceCreation classInstanceCreation) {
		invocation = classInstanceCreation;
	}

	public ASTNode getInvocation() {
		return invocation;
	}

	public Expression getExpression() {
		if(invocation instanceof MethodInvocation) return ((MethodInvocation)invocation).getExpression();
		if(invocation instanceof SuperConstructorInvocation) return ((SuperConstructorInvocation)invocation).getExpression();
		if(invocation instanceof ClassInstanceCreation) return ((ClassInstanceCreation)invocation).getExpression();
		return null;
	}

	/**
	 * This will return the name, including parameter types of the invoked method.
	 * For example, "foo()" will be returned for the invocation "a.foo()", and
	 * "bar(b, c.car(), d)" will be returned for the invocation "Baz.bar(b, c.car(), d)"
	 * @return the name of the method invoked by 
	 * this invocation, including parameter types.
	 *
	 */
	public String getName() {

		// Get the name of the method, ie "foo" for invocation a.foo()
		String name = null;
		if(invocation instanceof MethodInvocation) 
			name = ((MethodInvocation)invocation).getName().getIdentifier();
		if(invocation instanceof SuperMethodInvocation) 
			name = ((SuperMethodInvocation)invocation).getName().getIdentifier();
		if(invocation instanceof ConstructorInvocation) 
			name = "this";
		if(invocation instanceof SuperConstructorInvocation) 
			name = "super";
		if(invocation instanceof ClassInstanceCreation) 
			name = ((ClassInstanceCreation)invocation).getType().toString();
		if(name==null) return null;

		// And append the list of parameters, ie "()" for
		// a.foo() or "(b, c.car(), d)" for e.bar(b, c.car(), d)
		String paramList = "";
		int i = 0;
		for(Expression arg : getArguments()) {
			if(i>0) paramList = paramList.concat(", ");
			paramList = paramList.concat(arg.toString());
			i++;
		}

		return name+"("+paramList+")";
	}

	public final int getStartPosition() {
		return invocation.getStartPosition();
	}

	public final int getEndPosition() {
		return invocation.getStartPosition()+invocation.getLength();
	}

	public final int getLength() {
		return invocation.getLength();
	}

	public IMethodBinding resolveMethodBinding() {
		if(methodBinding != null) return methodBinding;
		if(invocation instanceof MethodInvocation) return ((MethodInvocation)invocation).resolveMethodBinding();
		if(invocation instanceof SuperMethodInvocation) return ((SuperMethodInvocation)invocation).resolveMethodBinding();
		if(invocation instanceof ConstructorInvocation) return ((ConstructorInvocation)invocation).resolveConstructorBinding();
		if(invocation instanceof SuperConstructorInvocation) return ((SuperConstructorInvocation)invocation).resolveConstructorBinding();
		if(invocation instanceof ClassInstanceCreation) return ((ClassInstanceCreation)invocation).resolveConstructorBinding();
		return null;
	}

	public void setMethodBinding(IMethodBinding methodBinding) {
		this.methodBinding = methodBinding;
	}

	/**
	 * Want to use IMethodBinding.getJavaElement() when dealing with nested
	 * class constructors since RJCore.bindingToJDTElement() will reach an error 
	 * and return null in these cases, and want to use RJCore.bindingToJDTElement() 
	 * when dealing with a CompilerGeneratedDefaultConstructor since 
	 * IMethodBinding.getJavaElement() will return null in these cases
	 */
	// TODO: Both will return null for the compiler generated default constructor
	// of a nested class. Need to handle this case
	public IMethod getMethodElement() {
		if(getInvocation() instanceof ClassInstanceCreation 
				&& ((ClassInstanceCreation)getInvocation()).getAnonymousClassDeclaration()!=null)
			return getAnonymousClassConstructorMethodElement(((ClassInstanceCreation)getInvocation()).getAnonymousClassDeclaration());

		IJavaElement methodElt = null;
		if(resolveMethodBinding()!=null && resolveMethodBinding().getJavaElement()!=null)
			methodElt = resolveMethodBinding().getJavaElement();
		else methodElt = RJCore.bindingToJDTElement(StoreUtil.getDefaultStoreRepository(), resolveMethodBinding());

		if (methodElt == null || !(methodElt instanceof IMethod)) return null;
		return (IMethod)methodElt;
	}

	private IMethod getAnonymousClassConstructorMethodElement(AnonymousClassDeclaration anonClass) {
		if(anonClass==null || anonClass.resolveBinding()==null || anonClass.resolveBinding().getSuperclass()==null) return null;

		// An anonymous class is considered a local class that extends the type
		// whose constructor is being invoked. For example, Foo is the superclass
		// of the anonymous class foo=new Foo(){}; 
		ITypeBinding superclassTypeBinding = anonClass.resolveBinding().getSuperclass();
		for(IMethodBinding method : superclassTypeBinding.getDeclaredMethods()) {
			// Find the constructor that corresponds to the one invoked to
			// create the anonymous class and return its java element
			if(!method.isConstructor()) continue;
			if(Arrays.equals(resolveMethodBinding().getParameterTypes(), method.getParameterTypes()))
				try {
					IJavaElement constructorElmt = method.getJavaElement();
					if(constructorElmt==null) {
						// we are dealing with a compiler generated default
						// constructor, so use RJCore.bindingToJDTElement()
						ReloRdfRepository repo = StoreUtil.getDefaultStoreRepository();
						constructorElmt = RJCore.bindingToJDTElement(repo, method);
					}
					return new AnonymousClassConstructor((IMethod) constructorElmt, method, anonClass);
				} catch (JavaModelException e) {
					e.printStackTrace();
				}
		}
		return null;
	}

	public List<Expression> getArguments() {
		List<?> argumentsNoType = new ArrayList<Object>();
		if(invocation instanceof MethodInvocation) argumentsNoType = ((MethodInvocation)invocation).arguments();
		else if(invocation instanceof SuperMethodInvocation) argumentsNoType = ((SuperMethodInvocation)invocation).arguments();
		else if(invocation instanceof ConstructorInvocation) argumentsNoType = ((ConstructorInvocation)invocation).arguments();
		else if(invocation instanceof SuperConstructorInvocation) argumentsNoType = ((SuperConstructorInvocation)invocation).arguments();
		else if(invocation instanceof ClassInstanceCreation) argumentsNoType = ((ClassInstanceCreation)invocation).arguments();

		List<Expression> arguments = new ArrayList<Expression>();
		for(Object o : argumentsNoType) arguments.add((Expression)o);		
		return arguments;
	}

	/**
	 * Returns a mapping from each anon class this invocation creates to the calls made 
	 * in that anon class. NOTE: Deeper anon classes made inside any of those calls are NOT
	 * included in this map; those calls can be retrieved by calling getInvocationsInAnonClasses() 
	 * on the call in the anon class that creates the deeper anon class.
	 * For example, for baz(new Bar(){ run(){ bat(new Cat(){ car(){ x(); } }); y(); } });
	 * calling this method on the Invocation baz will return a map [Bar -> bat, y] and 
	 * calling this method on the Invocation bat will return a map [Cat -> x]
	 * 
	 */
	public Map<AnonymousClassDeclaration, List<Invocation>> getInvocationsInAnonClasses() {
		if(anonClassInvocations==null) anonClassInvocations = findInvocationsInAnonClasses();
		return anonClassInvocations;
	}

	private Map<AnonymousClassDeclaration, List<Invocation>> findInvocationsInAnonClasses() {
		final Map<AnonymousClassDeclaration, List<Invocation>> anonInvocs = 
			new HashMap<AnonymousClassDeclaration, List<Invocation>>();
		getInvocation().accept(new ASTVisitor() {
			@Override
			public boolean visit(AnonymousClassDeclaration node) {

				// This invocation creates this anonymous class, so find the 
				// invocations that anon class makes and put them in the map
				MethodInvocationFinder invocFinder = new MethodInvocationFinder(node, false);
				List<Invocation> anonClassInvocations = invocFinder.getAllInvocations();
				anonInvocs.put(node, anonClassInvocations);

				// Return false because we only want to map the calls that "node" 
				// makes directly. If any of those calls create more anon classes, 
				// their call Invocations will map the deeper anon classes and calls
				return false;
			}
		});
		return anonInvocs;
	}

	@Override
	public String toString() {
		return getStringRepresentationOfInvocation(invocation);
	}

	public static String getStringRepresentationOfInvocation(ASTNode node) {
		if(node==null) return "";

		if(((node instanceof SuperConstructorInvocation) || node instanceof ConstructorInvocation) 
				&& node.toString().indexOf(";") > 0) return node.toString().substring(0, node.toString().indexOf(";"));
		return node.toString();
	}

	public static boolean nodeIsATypeOfInvocation(ASTNode node) {
		if(node==null) return false;

		return (node instanceof MethodInvocation ||
				node instanceof SuperMethodInvocation ||
				node instanceof ConstructorInvocation ||
				node instanceof SuperConstructorInvocation ||
				node instanceof ClassInstanceCreation);
	}

	public static List<Invocation> getEachInvocationInChain(Invocation chainedInvocation) {
		List<Invocation> invocations = new ArrayList<Invocation>();
		addEachInvocationToList(chainedInvocation, invocations);
		return invocations;
	}

	private static void addEachInvocationToList(Invocation invocation, List<Invocation> invocations) {
		if(invocation.resolveMethodBinding()!=null)	invocations.add(invocation);

		if(invocation.getExpression() instanceof MethodInvocation)
			addEachInvocationToList(new Invocation((MethodInvocation)invocation.getExpression()), invocations);
		else if(invocation.getExpression() instanceof SuperMethodInvocation)
			addEachInvocationToList(new Invocation((SuperMethodInvocation)invocation.getExpression()), invocations);
	}

}
