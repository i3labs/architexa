package com.architexa.diagrams.generate.team;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jface.text.IDocument;

public class TeamUtil {

	/**
	 * Creates an AST from the contents of the given IDocument
	 */
	public static CompilationUnit convertIDocumentToCompilationUnit(IDocument doc, IProgressMonitor monitor) {

		ASTParser parser = ASTParser.newParser(AST.JLS3);
		char[] completeDocText = doc.get().toCharArray();
		parser.setSource(completeDocText);
		ASTNode ast = parser.createAST(monitor);

		while(ast!=null && !(ast instanceof CompilationUnit)) ast = ast.getParent();
		return (CompilationUnit) ast;
	}

	public static String getPackageName(CompilationUnit fileCU) {
		if(fileCU.getPackage()!=null && fileCU.getPackage().getName()!=null
				&& fileCU.getPackage().getName().getFullyQualifiedName()!=null)
			return fileCU.getPackage().getName().getFullyQualifiedName();

		String fileTxt = fileCU.toString();
		return getPackageName(fileTxt);
	}

	private static String getPackageName(String fileTxt) {
		String packageName = "";
		// Using String.contains() and not String.startsWith()
		// because file text may have header comments before
		// package declaration. Safe to use contains() because
		// "package" is a keyword, so only use of "package " 
		// could be for package declaration.
		if (fileTxt.contains("package ")) {
			int startOfPackageDecl = fileTxt.indexOf("package ");
			fileTxt = fileTxt.substring(startOfPackageDecl); 
			// first thing in fileTxt string is now package decl, not any comment headers
			packageName = fileTxt.substring(8,fileTxt.indexOf(";")); // "package " is 8 chars
		}
		return packageName ;
	}

}
