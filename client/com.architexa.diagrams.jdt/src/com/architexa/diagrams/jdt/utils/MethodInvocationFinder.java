package com.architexa.diagrams.jdt.utils;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;

import com.architexa.diagrams.jdt.Invocation;


/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public class MethodInvocationFinder extends ASTVisitor {

	List<Invocation> invocations = new ArrayList<Invocation>();
	private boolean visitChildren = false;

	public MethodInvocationFinder(CompilationUnit cu) {
		cu.accept(this);
	}

	public MethodInvocationFinder(MethodDeclaration methodDeclaration) {
		methodDeclaration.accept(this);
	}

	public MethodInvocationFinder(MethodDeclaration methodDeclaration, boolean visitChildren) {
		this.visitChildren = visitChildren;
		methodDeclaration.accept(this);
	}
	
	public MethodInvocationFinder(ASTNode node) {
		node.accept(this);
	}

	public MethodInvocationFinder(ASTNode node, boolean visitChildren) {
		this.visitChildren = visitChildren;
		node.accept(this);
	}
	
	@Override
	public boolean visit(MethodInvocation node) {
		invocations.add(new Invocation(node));
		return visitChildren;
	}

	@Override
	public boolean visit(SuperMethodInvocation node) {
		invocations.add(new Invocation(node));
		return visitChildren;
	}

	@Override
	public boolean visit(ConstructorInvocation node) {
		invocations.add(new Invocation(node));
		return visitChildren;
	}

	@Override
	public boolean visit(SuperConstructorInvocation node) {
		invocations.add(new Invocation(node));
		return visitChildren;
	}

	@Override
	public boolean visit(ClassInstanceCreation node) {
		invocations.add(new Invocation(node));
		return visitChildren;
	}

	/**
	 *
	 * @return the method invocation found at the given location or null if
	 * no method invocation exists there
	 */
	public Invocation findInvocation(int offset) {
		List<Invocation> invocationsContainingOffset = new ArrayList<Invocation>();
		for(Invocation invocation : invocations) {
			int start = invocation.getStartPosition();
			int end = invocation.getStartPosition() + invocation.getLength();
			if(start <= offset && offset <= end) invocationsContainingOffset.add(invocation);			
		}
		if(invocationsContainingOffset.size()==0) return null;
		return invocationsContainingOffset.get(invocationsContainingOffset.size()-1);
	}

	/**
	 * 
	 * @return a list of all the method invocations made in the compilation unit
	 */
	public List<Invocation> getAllInvocations() {
		return invocations;
	}

}
