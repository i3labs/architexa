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
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;

import com.architexa.diagrams.RSECore;
import com.architexa.diagrams.jdt.builder.ReloASTExtractor;
import com.architexa.store.ReloRdfRepository;


/**
 * @author vineet
 *
 */
public class ContainmentHeirarchyExtractor extends ReloASTExtractor {

    Resource currProjRes = null;

    @Override
    public void init(Resource currFileRes, IResource currFile, ReloRdfRepository projectModel, ReloRdfRepository rdfModel) {
        super.init(currFileRes, currFile, projectModel, rdfModel);
        currProjRes = eclipseResourceToRDFResource(currFile.getProject());
    }
    
    @Override
    public void removeAnnotations(ReloRdfRepository model, URI res) {
        model.removeStatements(res, RSECore.contains, null);
    }
    
    private final void addContains(Resource parent, Resource child) {
        rdfModel.addStatement(parent, RSECore.contains, child);
    }
    
    @Override
    public boolean visit(PackageDeclaration pckgDecl) {
        Resource pkgDeclRes = bindingToResource(pckgDecl.resolveBinding());
        addContains(currProjRes, pkgDeclRes);
		return true;
	}

	@Override
    public boolean visit(TypeDeclaration typeDecl) {
	    TypeDeclaration parentType = (TypeDeclaration) getParent(TypeDeclaration.class);
	    Resource parentRes;
	    if (parentType != null) {
	        // embedded type
			parentRes = bindingToResource(parentType.resolveBinding());
	    } else {
	        // top-level type (parent is package)
			CompilationUnit cu = (CompilationUnit) getRequiredParent(CompilationUnit.class);
			PackageDeclaration pckgDecl = cu.getPackage();
			if (pckgDecl == null) return true;	// default package

			parentRes = bindingToResource(pckgDecl.resolveBinding());
	    }

	    Resource typeDeclRes = bindingToResource(typeDecl.resolveBinding());
		addContains(parentRes, typeDeclRes);
		return true;
	}

	@Override
    public boolean visit(MethodDeclaration methodDecl) {
		TypeDeclaration typeDecl = (TypeDeclaration) getRequiredParent(TypeDeclaration.class);
		Resource typeDeclRes = bindingToResource(typeDecl.resolveBinding());
		Resource methodDeclRes = bindingToResource(methodDecl.resolveBinding());
		addContains(typeDeclRes, methodDeclRes);
		return true;
	}

	@Override
    public boolean visit(VariableDeclarationFragment variableDecl) {
	    // ignore var. decl. in statements (looking for only field declarations)
        if (getParent(MethodDeclaration.class) != null) return true;
        if (getParent(Initializer.class) != null) return true;

        TypeDeclaration typeDecl = (TypeDeclaration) getRequiredParent(TypeDeclaration.class);
		Resource typeDeclRes = bindingToResource(typeDecl.resolveBinding());
		Resource variableDeclRes = bindingToResource(variableDecl.resolveBinding());
		addContains(typeDeclRes, variableDeclRes);
		return true;
	}
}
