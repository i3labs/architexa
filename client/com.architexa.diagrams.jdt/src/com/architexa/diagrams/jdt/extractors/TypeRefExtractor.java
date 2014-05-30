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

import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.ThisExpression;
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
public class TypeRefExtractor extends ReloASTExtractor {

    @Override
    public void removeAnnotations(ReloRdfRepository model, URI res) {
        model.removeStatements(res, RJCore.refType, null);
    }
    
    private void addRef(Resource srcRes, Resource dstRes) {
        rdfModel.addStatement(srcRes, RJCore.refType, dstRes);
    }
    
    private void addRefFromCaller(Resource dstRes) {
        Resource srcRes = CallHeirarchyExtractor.getCallingRes(this);
        addRef(srcRes, dstRes);
    }
    private void addRefFromOptionalCaller(Resource dstRes) {
        Resource srcRes = CallHeirarchyExtractor.getOptionalCallingRes(this);
        if (srcRes != null) addRef(srcRes, dstRes);
    }
    
    private void addMethodRefs(IMethodBinding methBinding) {
        Resource methodCallerRes = CallHeirarchyExtractor.getCallingRes(this);

        ITypeBinding[] paramTypes = methBinding.getParameterTypes();
        for (int i=0; i<paramTypes.length; i++)
            addRef(methodCallerRes, bindingToResource(paramTypes[i]));
        
        addRef(methodCallerRes, bindingToResource(methBinding.getReturnType()));
        addRef(methodCallerRes, bindingToResource(methBinding.getDeclaringClass()));
    }

    @Override
    public boolean visit(MethodInvocation invokedMethod) {
        addMethodRefs( invokedMethod.resolveMethodBinding() );
        return true;
    }

    @Override
    public boolean visit(SuperMethodInvocation invokedMethod) {
        addMethodRefs( invokedMethod.resolveMethodBinding() );
        return true;
    }

    
    @Override
    public boolean visit(CastExpression castExpr) {
        addRefFromCaller(bindingToResource(castExpr.getType().resolveBinding()));
        return true;
    }

    @Override
    public boolean visit(InstanceofExpression instOfExpr) {
        addRefFromCaller(bindingToResource(instOfExpr.getRightOperand().resolveBinding()));
        return true;
    }

	@Override
    public boolean visit(VariableDeclarationFragment variableDecl) {
		IVariableBinding variableBinding = variableDecl.resolveBinding();
		Resource referredTypeRes = bindingToResource(variableBinding.getType());

		FieldDeclaration fieldDecl = (FieldDeclaration) getParent(FieldDeclaration.class);
	    if (fieldDecl == null) {
	        // var. decl. in statements
	        Resource methodCallerRes = CallHeirarchyExtractor.getCallingRes(this);
			addRef(methodCallerRes, referredTypeRes);
	    } else {
	        // field declaration
	        Resource variableDeclRes = bindingToResource(variableBinding);
			addRef(variableDeclRes, referredTypeRes);
	    }		
		
		return true;
	}

    @Override
    public boolean visit(FieldAccess node) {
        addRefFromCaller(bindingToResource(node.resolveFieldBinding()));
        return true;
    }

    @Override
    public boolean visit(SuperFieldAccess node) {
        addRefFromCaller(bindingToResource(node.resolveFieldBinding()));
        return true;
    }
    
    @Override
    public boolean visit(ThisExpression node) {
        addRefFromCaller(bindingToResource(node.resolveTypeBinding()));
        return true;
    }

    @Override
    public boolean visit(QualifiedName node) {
        //System.err.println("processing: " + node.toString());
        addRefFromOptionalCaller(bindingToResource(node.resolveBinding()));
        return true;
    }

}
