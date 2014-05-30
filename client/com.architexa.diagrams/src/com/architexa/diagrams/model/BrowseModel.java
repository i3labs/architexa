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
 * Created on Mar 5, 2005
 */
package com.architexa.diagrams.model;

import org.apache.log4j.Logger;
import org.openrdf.model.Resource;

import com.architexa.diagrams.Activator;
import com.architexa.store.ReloRdfRepository;
import com.architexa.store.RepositoryMgr;

/**
 * This class is home to the 'business rules' that would be common to all
 * Diagrams. Beyond that it does also have some support for language specific
 * functionality (currently Java)
 * 
 * Need to figure out the best home for some of the functionality. It has a few
 * components:
 * 
 * - the repository: shoudln't this be in the model? - the factory: this is by
 * default done elsewhere.
 * 
 * @author vineet
 */
public abstract class BrowseModel {
	static final Logger logger = Activator.getLogger(BrowseModel.class);


    public BrowseModel() {
    }
    
    public void setRepo(RepositoryMgr rdfModel) {
    	this.getRootArt().setRepo(rdfModel);
    	this.getRootArt().setBrowseModel(this);
    }
    public RepositoryMgr getRepo() {
    	return this.getRootArt().getRepo();
    }
    
    private RootArtifact rootArt = null;
    public RootArtifact getRootArt() {
    	return rootArt;
    }
    public void setRootArt(RootArtifact _rootArt) {
    	this.rootArt = _rootArt;
    }

	////////////////////////////////////////
	// Language Specific Business Rules
	///////////////////////////////////////

	public boolean artifactLoadable(Artifact art, Resource artType) {
	    return true;
	}

	public boolean containerArtifact(Artifact art, Resource artType) {
		return false;
	}

	////////////////////////////////////////
	// Language Specific Functionality
	///////////////////////////////////////
	
    /**
     * Needed for writing and instantiating to store
     */
    public abstract String getBundleSymbolicName();

    /**
	 * Allows conversion of Java objects (provided by external frameworks) to Artifacts
	 * 
	 * An Artifact is the representation of the model, it is basically an RDF resource
	 * with additional helper methods
	 */
	public ArtifactFragment getArtifact(Object src) {
		if (src == null)
	        logger.error("getArtifact received null src");
		else if (src instanceof ArtifactFragment)
			return (ArtifactFragment) src;
		else if (src instanceof Resource)
	        return new ArtifactFragment((Resource) src);
	    else if (src instanceof Artifact)
	        return new ArtifactFragment((Artifact)src);
	    else
	    	logger.error("Unexpected source type for getArtifact: " + src.getClass());
	    return null;
	}

	// Unavailable code handling (library frags, old team revision frags, palette created frags):
	
	abstract public Resource getParentRes(ArtifactFragment child, boolean isUserCreated, 
			ReloRdfRepository repo);

	// For user created (via palette) frags:
	abstract public Class<?> getUserCreatedFragmentClass();
	abstract public Resource createResForUserCreatedFrag(String className);
	abstract public void setUserCreatedEnclosingFrag(ArtifactFragment child, ArtifactFragment parent);
	abstract public ArtifactFragment getUserCreatedEnclosingFrag(ArtifactFragment child);
	abstract public void setUserCreatedRelTypeToInheritance(ArtifactRel rel);
	abstract public void setUserCreatedRelTypeToCall(ArtifactRel rel);
	abstract public void setUserCreatedRelTypeToOverride(ArtifactRel rel);
	abstract public boolean isUserCreated(ArtifactFragment artfrag);

	abstract public ArtifactFragment findContainerParent(ArtifactFragment parentFrag);

}
