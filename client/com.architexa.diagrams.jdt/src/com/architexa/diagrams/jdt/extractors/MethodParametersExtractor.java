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

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;

import com.architexa.diagrams.jdt.Activator;
import com.architexa.diagrams.jdt.RJCore;
import com.architexa.diagrams.jdt.builder.ReloASTExtractor;
import com.architexa.diagrams.jdt.utils.MethodParametersSupport;
import com.architexa.store.ReloRdfRepository;


/**
 * @author vineet
 *
 */
public class MethodParametersExtractor extends ReloASTExtractor {
    static final Logger logger = Activator.getLogger(MethodParametersExtractor.class);

    @Override
    public void removeAnnotations(ReloRdfRepository model, URI res) {
        model.removeStatements(res, RJCore.parameter, null);
        model.removeStatements(res, RJCore.returnType, null);
        model.removeStatements(res, MethodParametersSupport.parameterCachedLabel, null);
    }
    
	@Override
    public boolean visit(MethodDeclaration methodDecl) {
	    Resource methodDeclRes = bindingToResource(methodDecl.resolveBinding());
		
		final List<Resource> parametersRes = new LinkedList<Resource>();
        for (Object arg0 : methodDecl.parameters()) {
            SingleVariableDeclaration svd = (SingleVariableDeclaration) arg0;
            parametersRes.add(bindingToResource(svd.getType().resolveBinding()));
        }

		Resource paramaterList = rdfModel.createList(parametersRes.iterator());
		rdfModel.addStatement(methodDeclRes, RJCore.parameter, paramaterList);


        Type retType = methodDecl.getReturnType2();
        if (retType != null) {
            Resource methodDeclTypeRes = bindingToResource(retType.resolveBinding());
            rdfModel.addStatement(methodDeclRes, RJCore.returnType, methodDeclTypeRes);
        }

        rdfModel.addStatement(methodDeclRes, MethodParametersSupport.parameterCachedLabel, getParamLabel(methodDecl));
        
		return true;
	}

    private String getParamLabel(MethodDeclaration methodDecl) {
        String retVal = "(";
        
        String methodSig = "";
        boolean first = true;
        try {
            for (Object arg0 : methodDecl.parameters()) {
                SingleVariableDeclaration svd = (SingleVariableDeclaration) arg0;
                if (first) first = false; else methodSig += ",";
                methodSig += svd.getType().resolveBinding().getName();
            }
        } catch (Exception e) {
            methodSig = "...";
            logger.error("Unexpected Exception", e);
        }
        
        retVal += methodSig;
        retVal += "): ";
        try {
            Type retType = methodDecl.getReturnType2();
            if (retType != null)
                retVal += retType.resolveBinding().getName();
            else
                retVal += "void";
        } catch (Exception e) {
            retVal += "void";
            logger.error("Unexpected Exception", e);
        }
        return retVal;
    }

}
