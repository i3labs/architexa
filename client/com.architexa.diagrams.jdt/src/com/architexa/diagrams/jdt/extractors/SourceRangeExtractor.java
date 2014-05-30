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

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.openrdf.model.Resource;

import com.architexa.diagrams.RSECore;
import com.architexa.diagrams.jdt.RJCore;
import com.architexa.diagrams.jdt.builder.ReloASTExtractor;
import com.architexa.store.ReloRdfRepository;


/**
 * @author vineet
 *
 */
public class SourceRangeExtractor extends ReloASTExtractor {
    
    @Override
    public void init(Resource currFileRes, IResource currFile, ReloRdfRepository projectModel, ReloRdfRepository rdfModel) {
        super.init(currFileRes, currFile, projectModel, rdfModel);
        rdfModel.addNameStatement(currFileRes, RSECore.name, currFile.getName());
    }

	private void declareNode(ASTNode node, Resource res) {
	    rdfModel.addStatement(res, RJCore.srcResource, currFileRes);
	    rdfModel.addStatement(res, RJCore.srcStart, Integer.toString(node.getStartPosition()));
	    rdfModel.addStatement(res, RJCore.srcLength, Integer.toString(node.getLength()));
    }

	@Override
    public boolean visit(TypeDeclaration typeDecl) {
        declareNode(typeDecl, bindingToResource(typeDecl.resolveBinding()));
		return true;
	}

    @Override
    public boolean visit(MethodDeclaration methodDecl) {
        declareNode(methodDecl, bindingToResource(methodDecl.resolveBinding()));
		return true;
	}

	@Override
    public boolean visit(VariableDeclarationFragment variableDecl) {
	    int ctxDist = getContextDistance(FieldDeclaration.class);
	    if (ctxDist > 1) return true;	// var. decl. in statements

	    declareNode(variableDecl, bindingToResource(variableDecl.resolveBinding()));
		return true;
	}
}
