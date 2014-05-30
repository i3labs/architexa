package com.architexa.diagrams.chrono.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;

/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public class MethodDeclarationFinder extends ASTVisitor {

	ASTNode compilationUnit;
	List<MethodDeclaration> declarations = new ArrayList<MethodDeclaration>();

	public MethodDeclarationFinder(CompilationUnit cu) {
		this.compilationUnit = cu;
		cu.accept(this);
	}
	
	public MethodDeclarationFinder(ASTNode node) {
		this.compilationUnit = node;
		node.accept(this);
	}
	
	@Override
	public boolean visit(MethodDeclaration node) {
		declarations.add(node);
		return false;
	}

	/**
	 *
	 * @return the method declaration found at the given location or null if
	 * no method declaration exists there
	 */
	public MethodDeclaration findDeclaration(int offset) {
		for(MethodDeclaration declaration : declarations) {
			int start = declaration.getStartPosition();
			int end = declaration.getStartPosition() + declaration.getLength();
			if(start <= offset && offset <= end) return declaration;
		}
		return null;
	}

	/**
	 * 
	 * @return a list of all the method declarations in the compilation unit
	 */
	public List<MethodDeclaration> getAllDeclarations() {
		return declarations;
	}

}
