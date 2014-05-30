/* 
 * Copyright (c) 2004-2006 Massachusetts Institute of Technology. This code was
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
 * Created on Oct 2, 2006
 */
package com.architexa.diagrams.jdt.builder;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.openrdf.model.Resource;

import com.architexa.diagrams.jdt.Activator;
import com.architexa.diagrams.model.Artifact;
import com.architexa.store.ReloRdfRepository;


/**
 * RDFBuildProcessors process the RDF made available from the extractors and
 * generate more RDF. They exist for one of two reasons:
 * 
 * o To allow for an easier/more-intuitive model from either the developers
 * perspective or the users perspective
 * 
 * o To do caching, so that we can extract this data faster
 * 
 * It will be nice/interesting to define what protocol the 'process' methods
 * take. For now they are only classes & interfaces, but generalizing before a
 * concrete use-case is a bad idea - we are therefore leaving the name, but not
 * supporting the functionality.
 * 
 * @author vineet
 * TODO: integrate the progress monitor here
 */
public abstract class AtxaRDFBuildProcessor {
    protected Artifact projArt;
    protected ReloRdfRepository rdfRepo;
    protected IProject project;
    private List<Artifact> childList;
    
	public static final String EXTENSION_POINT_ID = Activator.PLUGIN_ID + ".atxaBuildProcessor";

    public void init(ReloRdfRepository rdfRepo, Artifact projArt, IProject project) {
        this.rdfRepo = rdfRepo;
        this.projArt = projArt;
        this.project = project;
    }

    public void processRes(Resource classRes) {
        // per resource processing
    }
    public void cleanRes(Resource classRes) {
        // per resource processing
    }
    
    public void processProj(AtxaBuildVisitor atxaBuildVisitor, IProgressMonitor progressMonitor) {
    }
    
    public void cleanProj(AtxaBuildVisitor atxaBuildVisitor, IProgressMonitor progressMonitor) {
    }
    
    public List<Artifact> getProjectChildrenList () {
		if (childList == null) childList = projArt.queryChildrenArtifacts(rdfRepo);
    	return childList;
    }

    protected List<String> packgList = null;
	protected void createPackgList() {
		if (project != null) {
			String projName = project.getName();
			packgList = ResourceQueueManager.getUncheckedPackageListForProject(projName);
		}
		if (packgList == null)
			packgList = new ArrayList<String>();
	}
	public void clearPackgList() {
		packgList = new ArrayList<String>();
	}
}
