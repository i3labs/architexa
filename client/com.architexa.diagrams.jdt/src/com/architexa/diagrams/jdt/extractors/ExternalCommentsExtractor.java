package com.architexa.diagrams.jdt.extractors;

import java.util.Iterator;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.openrdf.model.Resource;

import com.architexa.diagrams.jdt.RJCore;
import com.architexa.diagrams.jdt.builder.ReloASTExtractor;


/**
 * 
 * @author Elizabeth L. Murnane
 *
 */

public class ExternalCommentsExtractor extends ReloASTExtractor {

	@Override
	public boolean visit(Javadoc comment) {
		ASTNode parent = comment.getParent();

		if(ASTNode.TYPE_DECLARATION != parent.getNodeType() 
				&& ASTNode.METHOD_DECLARATION != parent.getNodeType()
				&& ASTNode.FIELD_DECLARATION != parent.getNodeType()) {
			return false;
		}		

		if(parent instanceof MethodDeclaration) {
			MethodDeclaration parentMethodDecl = (MethodDeclaration) parent;
			Resource res = bindingToResource(parentMethodDecl.resolveBinding());
			rdfModel.addStatement(res, RJCore.javaDoc, rdfModel.createURI(comment.toString()));
		} else if(parent instanceof TypeDeclaration) {
			TypeDeclaration parentTypeDecl = (TypeDeclaration) parent;
			Resource res = bindingToResource(parentTypeDecl.resolveBinding());
			rdfModel.addStatement(res, RJCore.javaDoc, rdfModel.createURI(comment.toString()));
		} else if (parent instanceof FieldDeclaration) {
			Iterator<?> fragmentsIter = ((FieldDeclaration)parent).fragments().iterator();
			while(fragmentsIter.hasNext()) {
				VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragmentsIter.next();
				Resource res = bindingToResource(fragment.resolveBinding());
				rdfModel.addStatement(res, RJCore.javaDoc, rdfModel.createURI(comment.toString()));
			}
		}
		return true;
	}

}
