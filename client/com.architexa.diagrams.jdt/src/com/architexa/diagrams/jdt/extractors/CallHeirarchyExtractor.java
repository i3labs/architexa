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

import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;

import com.architexa.diagrams.jdt.RJCore;
import com.architexa.diagrams.jdt.builder.ReloASTExtractor;
import com.architexa.store.ReloRdfRepository;


/**
 * @author vineet
 *
 */
public class CallHeirarchyExtractor extends ReloASTExtractor {
	
    @Override
    public void removeAnnotations(ReloRdfRepository model, URI res) {
        model.removeStatements(res, RJCore.calls, null);
    }

    public static Resource getCallingRes(ReloASTExtractor extractor) {
        Resource retRes = getOptionalCallingRes(extractor);
        if (retRes == null)
            throw new IllegalArgumentException("No calling res parent of type: " + extractor.getParent());
        return retRes;
    }
    
    public static Resource getOptionalCallingRes(ReloASTExtractor extractor) {
        // get calling method
        MethodDeclaration parentMethodDecl = (MethodDeclaration) extractor.getAncestor(MethodDeclaration.class);
        if (parentMethodDecl != null) return extractor.bindingToResource(parentMethodDecl.resolveBinding());

	    // get calls in initializers
	    TypeDeclaration parentTypeDecl = (TypeDeclaration) extractor.getParent(TypeDeclaration.class);
        if (parentTypeDecl != null) return extractor.bindingToResource(parentTypeDecl.resolveBinding());
        
        // there are no calling resources!
        return null;
    }
	
	@Override
    public boolean visit(MethodInvocation invokedMethod) {
	    Resource invokedMethodRes = bindingToResource(invokedMethod.resolveMethodBinding());
        rdfModel.addStatement(getCallingRes(this), RJCore.calls, invokedMethodRes);
		return true;
	}

	@Override
    public boolean visit(ClassInstanceCreation invokedConstructor) {
	    Resource invokedMethodRes = bindingToResource(invokedConstructor.resolveConstructorBinding());
		rdfModel.addStatement(getCallingRes(this), RJCore.calls, invokedMethodRes);
		return true;
	}


}
