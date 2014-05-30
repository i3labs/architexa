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
 *
 */
package com.architexa.diagrams.relo.jdt.browse;

import org.apache.log4j.Logger;
import org.eclipse.jdt.core.IJavaElement;
import org.openrdf.model.Resource;

import com.architexa.diagrams.RSECore;
import com.architexa.diagrams.jdt.JDTSelectionUtils;
import com.architexa.diagrams.jdt.RJCore;
import com.architexa.diagrams.jdt.builder.asm.AsmUtil;
import com.architexa.diagrams.jdt.model.CodeUnit;
import com.architexa.diagrams.jdt.model.UserCreatedFragment;
import com.architexa.diagrams.model.Artifact;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.model.ArtifactRel;
import com.architexa.diagrams.relo.agent.ReloBrowseModel;
import com.architexa.diagrams.relo.jdt.ReloJDTPlugin;
import com.architexa.store.ReloRdfRepository;
import com.architexa.store.StoreUtil;


/**
 * @author vineet
 *
 */
public abstract class AbstractJDTBrowseModel extends ReloBrowseModel {
	static final Logger logger = ReloJDTPlugin.getLogger(AbstractJDTBrowseModel.class);
    
  
    @Override
    public String getBundleSymbolicName() {
        return ReloJDTPlugin.getDefault().getBundle().getSymbolicName();
    }

	@Override
    public ArtifactFragment getArtifact(Object src) {
		ArtifactFragment retVal = null;
		// Use JDTSelUtils so that we get any spring elmts
		IJavaElement ije = JDTSelectionUtils.getSelectedJDTElement();
		if (src instanceof IJavaElement)
	        retVal = CodeUnit.getCodeUnit(getRepo(), (IJavaElement) src);
	    if (retVal == null)
	    	retVal = super.getArtifact(src);
	    
	    if (ije!=null && retVal == null)
	    	retVal = CodeUnit.getCodeUnit(getRepo(), ije);
    	// type not supported
	    if (retVal == null)
	    	return null;
	    	
	    // convert any pure ArtifactFragment's into CodeUnit (we don't want to
		// capture AnnotatedArtifact's and DerivedArtifact's)
	    if (retVal.getClass().equals(ArtifactFragment.class)) {
	    	if(RSECore.isUserCreated(getRepo(), retVal.getArt().elementRes) || isUserCreated(retVal))
	    		retVal = new UserCreatedFragment(retVal);
	    	else retVal = new CodeUnit(retVal);
	    }
	    
	    return retVal;
	}
    
    // @tag unify-core: Browse Models are only what should differentiate Relo,
    // Strata, etc, so we should not be reusing like this

    public static boolean isLoadableJavaArtifact(Artifact art, Resource artType, ReloRdfRepository repo) {
        if (artType == null)
            throw new IllegalArgumentException("Cached type needed for loadability");

        // we are right now removing project boundaries, for the traditional views
        if (CodeUnit.isJavaProject(repo, artType)) return false;
        
        
        // vs: why is below hack needed?
        if (art.elementRes.toString().equals(ReloRdfRepository.atxaRdfNamespace + RJCore.jdtWkspcNS + "$void")) return false;
        if (art.elementRes.toString().equals(ReloRdfRepository.atxaRdfNamespace + RJCore.jdtWkspcNS + "$boolean")) return false;
        if (art.elementRes.toString().equals(ReloRdfRepository.atxaRdfNamespace + RJCore.jdtWkspcNS + "$int")) return false;
        if (art.elementRes.toString().equals(ReloRdfRepository.atxaRdfNamespace + RJCore.jdtWkspcNS + "$byte")) return false;
        	
        // @tag investigate-code: We need to examine the policy here
        if (true) return true;

        if (CodeUnit.isJavaElementType(repo, artType)) return true;
            
        //// show only items that have the source available (and all packages)
        //if (CodeUnit.isPackage(repo, artType))
        //    return true;

        // XXX removed check for source to view in Relo ==> we need to implement a strategy for this
        // TODO: why did we even need to comment the below out, this should be provided everywhere
        //if ( repo.hasStatement(art.elementRes, RJCore.srcResource, null))
        //    return true;

        if ( repo.hasStatement(art.elementRes, repo.rdfType, null))
            return true;

        // @tag investigate-code: Not sure we really understand what the line below's implications are
        return true;
    }

    @Override
	public boolean isUserCreated(ArtifactFragment artFrag) {
    	return (artFrag instanceof UserCreatedFragment);
    }
    
    @Override
    public boolean artifactLoadable(Artifact art, Resource artType) {
        if (isLoadableJavaArtifact(art, artType, getRepo()))
			return true;
		else
			return false;
	}

    @Override
	public boolean containerArtifact(Artifact art, Resource artType) {
    	if (CodeUnit.isPackage(getRepo(), artType))
			return true;
    	else
			return false;
	}

    @Override
    public Resource getParentRes(ArtifactFragment childFrag, boolean isUserCreated,
    		ReloRdfRepository repo) {

    	String childArtString = childFrag.getArt().toString();
    	if (childFrag instanceof UserCreatedFragment) {
    		childArtString = ((UserCreatedFragment) childFrag).getFullName();
    	}
    	int parentStart = childArtString.indexOf("#");
    	int parentEnd = -1;

    	Resource childType = childFrag.queryType(getRepo());
    	if(RJCore.methodType.equals(childType) || RJCore.fieldType.equals(childType)) {
    		parentEnd = childArtString.lastIndexOf(".");
    	} else if(RJCore.classType.equals(childType) || RJCore.interfaceType.equals(childType)) {
    		parentEnd = childArtString.lastIndexOf("$");
    	}
    	if(parentStart==-1 || parentEnd<=parentStart) return null;

    	String parentString = childArtString.substring(parentStart+1, parentEnd);
    	Resource createdParentRes = AsmUtil.toWkspcResource(repo, parentString);
    	if(isUserCreated) {
    		// Add a stmt indicating that this parent is user created
    		repo.startTransaction();
    		repo.addStatement(createdParentRes, RSECore.userCreated, StoreUtil.createMemLiteral("true"));
    		repo.commitTransaction();
    	}
    	return createdParentRes;
    }

    @Override
    public Class<?> getUserCreatedFragmentClass() {
    	return UserCreatedFragment.class;
    }

    @Override
    public Resource createResForUserCreatedFrag(String fragName) {
    	return AsmUtil.toWkspcResource(getRepo(), fragName);
    }

    @Override
    public void setUserCreatedEnclosingFrag(ArtifactFragment child, ArtifactFragment parent) {
    	if(!(child instanceof UserCreatedFragment)) return;
    	((UserCreatedFragment)child).setEnclosingFrag(parent);
    }

    @Override
    public ArtifactFragment getUserCreatedEnclosingFrag(ArtifactFragment child) {
    	if(!(child instanceof UserCreatedFragment)) return null;
    	ArtifactFragment parent = ((UserCreatedFragment)child).getEnclosingParentFrag();
    	if(parent==null) return parent;

    	ReloRdfRepository repo = getRepo();
    	Resource childType = child.queryType(repo);
    	if(RJCore.packageType.equals(parent.queryType(repo)) &&
    			(RJCore.methodType.equals(childType) || RJCore.fieldType.equals(childType))) {
    		// Child is a method or field so a class must contain it, not a package. 
    		// Since user selected a package as the parent, create a class within that 
    		// package to enclose child
    		String packageName = parent.getArt().queryName(repo);
    		UserCreatedFragment classFrag = new UserCreatedFragment(this, packageName+"$"+"NewClass");
    		classFrag.setEnclosingFrag(parent); // package encloses the class
    		((UserCreatedFragment)child).setEnclosingFrag(classFrag);
    		return classFrag;
    	}

    	return ((UserCreatedFragment)child).getEnclosingParentFrag();
    }

    @Override
    public void setUserCreatedRelTypeToInheritance(ArtifactRel rel) {
    	rel.setType(RJCore.inherits);
    }
    @Override
    public void setUserCreatedRelTypeToCall(ArtifactRel rel) {
    	rel.setType(RJCore.calls);
    }
    @Override
    public void setUserCreatedRelTypeToOverride(ArtifactRel rel) {
    	rel.setType(RJCore.overrides);
    }

    @Override
	public ArtifactFragment findContainerParent(ArtifactFragment parentFrag) {
    	Resource type;
		while (true) {
			type = parentFrag.queryType(getRepo());
			if (RJCore.classType.equals(type) || RJCore.interfaceType.equals(type) 
					|| RJCore.packageType.equals(type))
				return parentFrag;
			ArtifactFragment pF = parentFrag.getParentArt();
			if (pF == null) return parentFrag;
			parentFrag = pF;
		}
	}
}
