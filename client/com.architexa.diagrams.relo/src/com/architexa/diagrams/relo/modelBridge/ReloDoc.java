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
 * Created on Jun 12, 2004
 *
 */
package com.architexa.diagrams.relo.modelBridge;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.openrdf.model.URI;

import com.architexa.diagrams.RSECore;
import com.architexa.diagrams.commands.AddRelCommand;
import com.architexa.diagrams.model.Artifact;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.model.ArtifactRel;
import com.architexa.diagrams.model.ColorDPolicy;
import com.architexa.diagrams.model.DerivedArtifact;
import com.architexa.diagrams.model.DirectedRel;
import com.architexa.diagrams.model.RootArtifact;
import com.architexa.diagrams.parts.PointPositionedDiagramPolicy;
import com.architexa.intro.preferences.LibraryPreferences;
import com.architexa.org.eclipse.gef.commands.CompoundCommand;
import com.architexa.store.ReloRdfRepository;


/**
 * @author vineet
 * 
 */
public class ReloDoc extends RootArtifact {
    
    public ReloDoc() {
        setInstanceRes(RSECore.docRoot);
	}

	@Override
    public ArtifactFragment getArtifact(Object src) {
		ArtifactFragment retVal = super.getArtifact(src);
		ArtifactFragment.ensureInstalledPolicy(retVal, PointPositionedDiagramPolicy.DefaultKey, PointPositionedDiagramPolicy.class);
		ArtifactFragment.ensureInstalledPolicy(retVal, ColorDPolicy.DefaultKey, ColorDPolicy.class);
	    return retVal;
	}
    
    ////
    // support for inputs. 
    //  We are really caching the input's that are loaded later on - we don't 
    //  need to do that, and should ideally just be updating the document
    //  model instead
    ////

	// these should really not be stored here for action later, they should have
	// been acted upon before here
	// [1]
    private ReloRdfRepository inputRepo = null;
    
    public void setInputRepo(ReloRdfRepository rdfRepo) {
        this.inputRepo = rdfRepo;
    }

    public ReloRdfRepository getInputRDFRepo() {
        return inputRepo;
    }

    
    // [2]
	// collection of Artifacts or type that browse model can convert to Artifacts
	private List<?> inputItems = Collections.EMPTY_LIST;

	public void setInputItems(List<?> lst) {
		inputItems = lst;
	}

	public List<?> getInputItems() {
		return inputItems;
	}
	public void showIncludedRelationships(CompoundCommand tgtCmd, ArtifactFragment afList) {
		showIncludedRelationships(tgtCmd, Arrays.asList(new ArtifactFragment[] {afList}));
	}
    
	 /**
	 * Shows the given relationship if the source and target are already visible
	 * @param relationRes
	 */
	public void showIncludedRelationships(CompoundCommand tgtCmd, List<ArtifactFragment> afsToAdd) {
		for (ArtifactFragment curArt : afsToAdd) {
			if (curArt instanceof DerivedArtifact) continue;
			showIncludedRelationshipsForArt(tgtCmd, curArt, afsToAdd);
		}
	}

	private ArtifactFragment getMatchingArts(Artifact fwdArt, List<ArtifactFragment> afsToAdd) {
		if (afsToAdd == null || !(afsToAdd instanceof List<?>)) return null;
    	for(ArtifactFragment af : afsToAdd) {
			if (af.getArt().equals(fwdArt))  return af;
		}
		return null;
	}

	/**
	 * @param curArt - either one of the new items or if only one item then the single item 
	 * @param afsToAdd - new items that are being created to show connection between
	 */
	private void showIncludedRelationshipsForArt(CompoundCommand tgtCmd, ArtifactFragment curArt, List<ArtifactFragment> afsToAdd) {
    	URI relationRes = RSECore.createRseUri("jdt#inherits");
		for (Artifact fwdArt : curArt.getArt().queryArtList(getRepo(), DirectedRel.getFwd(relationRes))) {
			List<ArtifactFragment> matchingShownChildren = getMatchingNestedShownChildren(fwdArt);
			if (getMatchingArts(fwdArt, afsToAdd) != null) 
				tgtCmd.add(new AddRelCommand(curArt, new DirectedRel(relationRes, true), getMatchingArts(fwdArt, afsToAdd)));
			for (ArtifactFragment matchingShownChild : matchingShownChildren) {
				if ((!relInDocument(curArt, matchingShownChild)))
					tgtCmd.add(new AddRelCommand(curArt, new DirectedRel(relationRes, true), matchingShownChild));
			}
		}
		for (Artifact revArt : curArt.getArt().queryArtList(getRepo(), DirectedRel.getRev(relationRes))) {
			List<ArtifactFragment> matchingShownChildren = getMatchingNestedShownChildren(revArt);
			if (getMatchingArts(revArt, afsToAdd) != null) 
				tgtCmd.add(new AddRelCommand(getMatchingArts(revArt, afsToAdd), new DirectedRel(relationRes, true),curArt ));
			for (ArtifactFragment matchingShownChild : matchingShownChildren) {
				if ((!relInDocument(curArt, matchingShownChild)))
					tgtCmd.add(new AddRelCommand(matchingShownChild, new DirectedRel(relationRes, true), curArt));
			}
		}
    }
	
	private boolean relInDocument(ArtifactFragment curArt, ArtifactFragment matchingArt){
		URI relationRes = RSECore.createRseUri("jdt#inherits");
		if (curArt.targetConnectionsContains(new ArtifactRel(curArt, matchingArt, relationRes)) 
				|| curArt.sourceConnectionsContains(new ArtifactRel(curArt, matchingArt, relationRes)) )
				return true;
		return false;
	}

	@Override
	public boolean isLibCodeInDiagram() {
		return LibraryPreferences.isReloLibCodeInDiagram();
	}

}
