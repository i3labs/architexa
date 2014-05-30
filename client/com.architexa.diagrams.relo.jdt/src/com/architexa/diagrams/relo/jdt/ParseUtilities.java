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
 * Created on Aug 14, 2004
 *
 */
package com.architexa.diagrams.relo.jdt;

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;

/**
 * @author vineet
 *
 */
public class ParseUtilities {
    static final Logger logger = ReloJDTPlugin.getLogger(ParseUtilities.class);

    /**
     * @param method
     * @return SourceRange
     * @throws JavaModelException
     */
    public static ISourceRange getBodyRange(IMethod method) throws JavaModelException {
    	ASTParser parser = ASTParser.newParser(AST.JLS3);
    	parser.setSource(method.getCompilationUnit());
    	CompilationUnit cu = (CompilationUnit) parser.createAST(null);
    	TypeDeclaration td = findTypeDeclaration(cu, method); 
    	MethodDeclaration md = findMethodDeclaration(td, method);
    	Block body = md.getBody();
    	final int startPos;
    	final int length;
    	List<?> bodyStatements = body.statements();
    	int stmnts = bodyStatements.size();
        if (stmnts > 0) {
            startPos = ((Statement) bodyStatements.get(0)).getStartPosition();
            Statement lastStmnt = (Statement) bodyStatements.get(stmnts - 1);
            length = lastStmnt.getStartPosition() + lastStmnt.getLength() - startPos;
        } else {
            startPos = body.getStartPosition();
            length = body.getLength();
        }
    	return new ISourceRange() {
            public int getLength() {
                return length;
            }
            public int getOffset() {
                return startPos;
            }};
    }

    /**
     * @param method
     * @return SourceRange
     * @throws JavaModelException
     */
    public static ISourceRange getMethodRange(IMethod method) throws JavaModelException {
    	ASTParser parser = ASTParser.newParser(AST.JLS3);
    	parser.setSource(method.getCompilationUnit());
    	CompilationUnit cu = (CompilationUnit) parser.createAST(null);
    	TypeDeclaration td = findTypeDeclaration(cu, method); 
    	final MethodDeclaration md = findMethodDeclaration(td, method);
    	return new ISourceRange() {
            public int getLength() {
                return md.getLength();
            }
            public int getOffset() {
                return md.getStartPosition();
            }};
    }

    static private boolean isNodeInRange(ASTNode node, ISourceRange sourceRange) {
        if ((sourceRange.getOffset() <= node.getStartPosition())
                && (sourceRange.getLength() >= node.getLength())) {
            return true;
        }
        return false;
    }

    static private MethodDeclaration findMethodDeclaration(TypeDeclaration type, IMethod method) throws JavaModelException {
    	ISourceRange sourceRange = method.getSourceRange();
    	for (Iterator<?> I = type.bodyDeclarations().iterator(); I.hasNext();) {
    		BodyDeclaration declaration = (BodyDeclaration) I.next();
    		if (!(declaration instanceof MethodDeclaration)) {
    			continue;
    		}
    		if (isNodeInRange(declaration, sourceRange)) {
    		    return (MethodDeclaration) declaration;
    		}
    	}
    	return null;
    }

    static private TypeDeclaration findTypeDeclaration(CompilationUnit cu, IMethod method) throws JavaModelException {
        return findTypeDeclaration(cu, method.getDeclaringType());
    }

    static private TypeDeclaration findTypeDeclaration(CompilationUnit cu, IType type) throws JavaModelException {
        IType declaringType = type.getDeclaringType();
        if (declaringType == null) {
            Iterator<?> topLevelTypeDeclIt = cu.types().iterator();
            while (topLevelTypeDeclIt.hasNext()) {
                AbstractTypeDeclaration td = (AbstractTypeDeclaration) topLevelTypeDeclIt.next();
                if (td.getName().getIdentifier().equals(type.getElementName()))
                    return (TypeDeclaration) td;
            }
        } else {
            TypeDeclaration declaringTD = findTypeDeclaration(cu, declaringType);
            TypeDeclaration[] siblingTD = declaringTD.getTypes();
            for (int i=0; i<siblingTD.length; i++) {
                if (siblingTD[i].getName().getIdentifier().equals(type.getElementName()))
                    return siblingTD[i];
            }
        }

        logger.error("Could not find type for: " + type.getFullyQualifiedName());
        return null;
    }

    
}
