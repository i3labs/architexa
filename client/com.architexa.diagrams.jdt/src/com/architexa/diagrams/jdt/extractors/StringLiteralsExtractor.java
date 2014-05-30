package com.architexa.diagrams.jdt.extractors;

import java.util.Iterator;

import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.openrdf.model.Resource;

import com.architexa.diagrams.jdt.RJCore;
import com.architexa.diagrams.jdt.builder.ReloASTExtractor;


/**
 * 
 * @author Elizabeth L. Murnane
 *
 */

public class StringLiteralsExtractor extends ReloASTExtractor {

	@Override
	public boolean visit(MethodDeclaration methodDecl) {

		Block body = methodDecl.getBody();
		if(body==null) return false;

		Iterator<?> statementsIter = body.statements().iterator();
		while(statementsIter.hasNext()) {
			Statement statement = (Statement) statementsIter.next();
			if(statement instanceof VariableDeclarationStatement) {
				VariableDeclarationStatement varDecl = (VariableDeclarationStatement) statement;
				Iterator<?> fragmentsIter = varDecl.fragments().iterator();
				while(fragmentsIter.hasNext()) {
					VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragmentsIter.next();
					if(fragment.toString().contains("\"")) {
						int start = fragment.toString().indexOf("\"");
						int end = fragment.toString().lastIndexOf("\"");
						if(end > start) {
							String literal = fragment.toString().substring(start+1, end);
							Resource res = bindingToResource(fragment.resolveBinding());
							rdfModel.addStatement(res, RJCore.stringLiteral, rdfModel.createBNode(literal));
						}
					}
				}
			}
		}
		return true;
	}

}
