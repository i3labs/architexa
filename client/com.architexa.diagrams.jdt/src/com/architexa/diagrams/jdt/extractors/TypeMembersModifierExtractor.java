/* 
 * Copyright (c) 2004-2005 Massachusetts Institute of Technology. This code was
 * developed as part of the Haystack (http://haystack.lcs.mit.edu/) research 
 * project at MIT. Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation files (the 
 * "Software"), to deal in the Software without restriction, including without 
 * limitation the rights to use, copy, modify, merge, publish, distribute, 
 * sublicense, and/or sell copies of the Software, and to permit persons to whom
 * the Software is furnished to do so, subject to the following conditions: 
 * 
 * The above copyright notice and this permission notice shall be included in 
 * all copies or substantial portions of the Software. 
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER  
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, 
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE. 
 */
/*
 * Created on Dec 26, 2004
 *
 */
package com.architexa.diagrams.jdt.extractors;

import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;

import com.architexa.diagrams.jdt.RJCore;
import com.architexa.diagrams.jdt.builder.ReloASTExtractor;
import com.architexa.store.ReloRdfRepository;


/**
 * @author vineet
 *
 */
public class TypeMembersModifierExtractor extends ReloASTExtractor {

    @Override
    public void removeAnnotations(ReloRdfRepository model, URI res) {
        model.removeStatements(res, RJCore.access, null);
    }
    
	@Override
    public boolean visit(MethodDeclaration methodDecl) {
	    Resource methodDeclRes = bindingToResource(methodDecl.resolveBinding());
		addModifiers(methodDeclRes, methodDecl.getModifiers());
		return true;
	}

	@Override
    public boolean visit(VariableDeclarationFragment variableDecl) {
	    FieldDeclaration fieldDecl = (FieldDeclaration) getParent(FieldDeclaration.class);
	    if (fieldDecl == null) return true;	// var. decl. in statements

	    Resource variableDeclRes = bindingToResource(variableDecl.resolveBinding());
		addModifiers(variableDeclRes, fieldDecl.getModifiers());
		return true;
	}

    @Override
    public boolean visit(TypeDeclaration typeDecl) {
        Resource typeDeclRes = bindingToResource(typeDecl.resolveBinding());
        addModifiers(typeDeclRes, typeDecl.getModifiers());
        return true;
    }

	protected void addModifiers(Resource memberDeclRes, int modifiers) {
		if (Modifier.isPublic(modifiers))
		    rdfModel.addStatement(memberDeclRes, RJCore.access, RJCore.publicAccess);
        else if (Modifier.isProtected(modifiers))
		    rdfModel.addStatement(memberDeclRes, RJCore.access, RJCore.protectedAccess);
        else if (Modifier.isPrivate(modifiers))
		    rdfModel.addStatement(memberDeclRes, RJCore.access, RJCore.privateAccess);
        else
		    rdfModel.addStatement(memberDeclRes, RJCore.access, RJCore.noAccess);

		// should also add static, final, abstract, etc.
	}
}
