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
 * Created on Jan 7, 2005
 *
 */
package com.architexa.diagrams.jdt.builder;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.openrdf.model.Resource;

import com.architexa.diagrams.RSECore;
import com.architexa.diagrams.jdt.Activator;
import com.architexa.diagrams.jdt.extractors.CallHeirarchyExtractor;
import com.architexa.diagrams.jdt.extractors.ContainmentHeirarchyExtractor;
import com.architexa.diagrams.jdt.extractors.DebugVisitor;
import com.architexa.diagrams.jdt.extractors.ExternalCommentsExtractor;
import com.architexa.diagrams.jdt.extractors.InheritanceHeirarchyExtractor;
import com.architexa.diagrams.jdt.extractors.InternalCommentsExtractor;
import com.architexa.diagrams.jdt.extractors.MethodParametersExtractor;
import com.architexa.diagrams.jdt.extractors.SourceRangeExtractor;
import com.architexa.diagrams.jdt.extractors.StringLiteralsExtractor;
import com.architexa.diagrams.jdt.extractors.TypeMembersModifierExtractor;
import com.architexa.diagrams.jdt.extractors.TypeRefExtractor;
import com.architexa.rse.RSE;
import com.architexa.store.ReloRdfRepository;



class ReloJDTBuildVisitor extends AtxaBuildVisitor {
    static final Logger logger = Activator.getLogger(ReloJDTBuildVisitor.class);

    public ReloJDTBuildVisitor(ReloRdfRepository reloRdf, IProject project) {
        super(reloRdf, project);
    }

    //private boolean finishBuild = false;

    @Override
	public boolean visitResource(IResource resource, boolean remAnn, boolean addAnn) {
        //if (finishBuild) return false;
		if (resource.getType() != IResource.FILE)		return true;
		if (!resource.getName().endsWith(AtxaJDTBuilder.JAVA_SUFFIX))	return true;
		
		monitor.subTask("[" + (taskDone + 1)+ "/" + taskSize + "] Parsing for " + RSE.appName + ": " + resource.getName());

        //System.err.println("builder visitResource: " + resource.getName());
		
	    //Model memModel = StoreUtil.getMemModel();
		reloRdf.startTransaction();
	    ReloRdfRepository memModel = reloRdf;
	    List<Class<? extends ReloASTExtractor>> visitors = getVisitors();
	    Resource currFileRes = RSECore.eclipseResourceToRDFResource(reloRdf, resource);

        List<Resource> srcResources = null;
		CompilationUnit cu = null;

		if (remAnn)
            srcResources = ReloASTExtractor.getResourceForFile(reloRdf, currFileRes);

	    if (addAnn)
			cu = getCompilationUnit(resource);
    	
    	Iterator<Class<? extends ReloASTExtractor>> visIt = visitors.iterator();
    	while (visIt.hasNext()) {
    	    // instantiate
    		Class<? extends ReloASTExtractor> visitorClass = visIt.next();
    		ReloASTExtractor vis = null;
			try {
				vis = visitorClass.newInstance();
			    if (vis == null) continue;

			    vis.init(currFileRes, resource, reloRdf, memModel);
			    if (remAnn)	vis.removeAnnotations(srcResources);
			    if (addAnn && cu!=null) vis.addAnnotations(cu);
		    	vis.end();
		    	
			} catch (RuntimeException re) {
			    logger.error("Unexpected exception", re);
			    vis.dumpContext();
			} catch (Exception e) {
			    logger.error("Unexpected exception", e);
			}
    	}
    	
	    //rdfModel.add(memModel);
	    //memModel.close();
		reloRdf.commitTransaction();
	    
	    
	    // make sure to purge as much memory as we can
	    //rdfModel = null;
	    //ReloStore.defaultInstance.shutdown();
	    //rdfModel = ReloStore.defaultInstance.initializeModel(); 

        //if (taskDone > 10) finishBuild = true;
        monitor.worked(1);
        taskDone++;
        return true;
    }

    private List<Class<? extends ReloASTExtractor>> getVisitors() {
    	List<Class<? extends ReloASTExtractor>> visitors = new LinkedList<Class<? extends ReloASTExtractor>> ();
    	
    	visitors.add(DebugVisitor.class);

    	// system information (without these Relo-JDT will not function as expected)
    	visitors.add(SourceRangeExtractor.class);
    	
    	// java relation
    	visitors.add(CallHeirarchyExtractor.class);
    	visitors.add(ContainmentHeirarchyExtractor.class);
    	visitors.add(InheritanceHeirarchyExtractor.class);
    	visitors.add(MethodParametersExtractor.class);
    	visitors.add(TypeRefExtractor.class);
    	
    	// java properties
    	visitors.add(TypeMembersModifierExtractor.class);
    	
    	// comments and string literals
    	visitors.add(ExternalCommentsExtractor.class);
    	visitors.add(InternalCommentsExtractor.class);
    	visitors.add(StringLiteralsExtractor.class);
    	
        return visitors;
    }

    private CompilationUnit getCompilationUnit(IResource resource) {
    	try {
	        ICompilationUnit icu = (ICompilationUnit) JavaCore.create(resource);
	    	ASTParser parser = ASTParser.newParser(AST.JLS3);
	    	parser.setResolveBindings(true);
	    	parser.setSource(icu);
	    	CompilationUnit cu = (CompilationUnit) parser.createAST(monitor);
	        return cu;
    	} catch (IllegalStateException e) {
    		// thrown when source file is exclude from the java build  
    		logger.error("Illegal state to attempt parsing: " + resource, e);
    		return null;
    	}
    }

}