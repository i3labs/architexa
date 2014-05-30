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
package com.architexa.diagrams.jdt.builder;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.collections.Closure;
import org.apache.commons.collections.CollectionUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.sesame.sail.StatementIterator;

import com.architexa.diagrams.RSECore;
import com.architexa.diagrams.jdt.RJCore;
import com.architexa.store.ReloRdfRepository;


/**
 * @author vineet
 *
 * Maintains a context and store for visits
 */
public class ReloASTExtractor extends ASTVisitor {
    LinkedList<ASTNode> context = new LinkedList<ASTNode> ();
    
    // in memory model for current file (eclipse resource)
    protected ReloRdfRepository rdfModel = null;
    
    protected Resource currFileRes = null;
    protected IResource currFile = null;
    protected ReloRdfRepository projectModel = null;

    public void init(Resource currFileRes, IResource currFile, ReloRdfRepository projectModel, ReloRdfRepository rdfModel) {
        this.currFileRes = currFileRes;
        this.currFile = currFile;
    	this.projectModel = projectModel;
    	this.rdfModel = rdfModel;
    }

    public void removeAnnotations(List<Resource> srcResources) {
        CollectionUtils.forAllDo(srcResources, new Closure() {
            public void execute(Object arg0) {
                removeAnnotations(projectModel, (URI) arg0);
            }});
    }

    public void removeAnnotations(ReloRdfRepository model, URI res) {
    }

    public void addAnnotations(CompilationUnit cu) {
    	cu.accept(this);
    }

    /**
	 * Called after the visits
	 */
	public void end() {
	}

	public Resource eclipseResourceToRDFResource(IProject curProj) {
	    Resource retVal = RSECore.eclipseProjectToRDFResource(rdfModel, curProj);
	    if (!projectModel.contains(retVal, RSECore.initialized, true) &&
	            !rdfModel.contains(retVal, RSECore.initialized, true)) {
            initResource(retVal, curProj);
        }
	    return retVal;
	}
    
	public Resource bindingToResource(IBinding binding) {
	    Resource retVal = RJCore.bindingToResource(rdfModel, binding);
	    if (!projectModel.contains(retVal, RSECore.initialized, true) &&
	            !rdfModel.contains(retVal, RSECore.initialized, true)) {
            initResource(retVal, binding);
        }
	    return retVal;
	}

	protected void initResource(Resource res, IProject proj) {
	    rdfModel.addTypeStatement(res, RJCore.projectType);
	    rdfModel.addNameStatement(res, RSECore.name, proj.getName());
	    rdfModel.addStatement(res, RSECore.initialized, Boolean.toString(true));
	}
	
	protected void initResource(Resource res, IBinding binding) {
		switch (binding.getKind()) {
		case IBinding.METHOD:
		    rdfModel.addTypeStatement(res, RJCore.methodType);
			break;
		case IBinding.PACKAGE:
		    rdfModel.addTypeStatement(res, RJCore.packageType);
		    rdfModel.addStatement(res, RSECore.initialized, Boolean.toString(true));
			break;
		case IBinding.TYPE:
			if (((ITypeBinding)binding).isInterface())
			    rdfModel.addStatement(res, RJCore.isInterface, RJCore.interfaceType);
			else
			    rdfModel.addTypeStatement(res, RJCore.classType);
			break;
		case IBinding.VARIABLE:
		    FieldDeclaration fieldDecl = (FieldDeclaration) getParent(FieldDeclaration.class);
			if (fieldDecl != null) {
			    // field declaration
			    rdfModel.addTypeStatement(res, RJCore.fieldType);
			} else {
			    // variable declaration in methods
			}
			break;
		}
	    rdfModel.addNameStatement(res, RSECore.name, binding.getName());
	    rdfModel.addInitializedStatement(res, RSECore.initialized, Boolean.toString(true));
	}
	

	// below methods provide support for building context

	@Override
    public void preVisit(ASTNode node) {
		context.addFirst(node);
		super.preVisit(node);
	}

	@Override
    public void postVisit(ASTNode node) {
	    context.removeFirst();
		super.postVisit(node);
	}
	
	public ASTNode getParent() {
		return context.get(context.size() - 1);
	}
	
    public ASTNode getParent(Class<?> inst) {
        Iterator<ASTNode> it = context.iterator();
        it.next();  // the first items is itself, the second one is parent
        while (it.hasNext()) {
            ASTNode node = it.next();
            if (inst.isInstance(node)) {
                return node;
            }
        }
        return null;
    }

    /**
     * Returns the oldest parent that has the required type
     */
    public ASTNode getAncestor(Class<?> inst) {
        ASTNode retVal = null;
        Iterator<ASTNode> it = context.iterator();
        it.next();  // the first items is itself, the second one is parent
        while (it.hasNext()) {
            ASTNode node = it.next();
            if (inst.isInstance(node)) {
                retVal = node;
            }
        }
        return retVal;
    }

	public int getContextDistance(Class<?> inst) {
	    int cnt = 0;
	    Iterator<ASTNode> it = context.iterator();
	    it.next();	// the first items is itself, the second one is parent
	    while (it.hasNext()) {
			ASTNode node = it.next();
			cnt ++;
			if (inst.isInstance(node)) {
				return cnt;
			}
	    }
		return -1;
	}

	public ASTNode getRequiredParent(Class<?> inst) {
        ASTNode retVal = getParent(inst);
        if (retVal == null)
            throw new IllegalArgumentException("No parent of type: " + inst);
        return retVal;
    }
	
	// using deprecated method, non-deprecated method is only available in Eclipse 3.2 
	@SuppressWarnings("deprecation")
	public void dumpContext() {
	    System.err.println("Extractor context:");
	    Iterator<ASTNode> it = context.iterator();
	    while (it.hasNext()) {
			ASTNode node = it.next();
            System.err.print(" " + node.getClass()); 
            System.err.print("[" + node.getStartPosition() + "+" + node.getLength());
            if (node.getRoot() instanceof CompilationUnit)
                System.err.print("/line:" + ((CompilationUnit)node.getRoot()).lineNumber(node.getLength()));
            System.err.println("]: " + getNodeKey(node));
        }
	}
    
    private static final String getNodeKey(ASTNode node) {
        try {
            Method m = node.getClass().getMethod("resolveBinding");
            if (m != null) {
                IBinding nodeBinding = (IBinding) m.invoke(node);
                if (nodeBinding != null)
                    return nodeBinding.getKey();
            }
        } catch (Throwable t) {}
        return "";
    }

    public static List<Resource> getResourceForFile(ReloRdfRepository rdfModel, Resource currFileRes) {
        List<Resource> retVal = new ArrayList<Resource> (20);
	    StatementIterator si = rdfModel.getStatements(null, RJCore.srcResource, currFileRes);
	    while (si.hasNext()) {
	        retVal.add(si.next().getSubject());
	    }
        si.close();
        return retVal;
    }

}
