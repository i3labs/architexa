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
 * Created on Jan 17, 2006
 */
package com.architexa.diagrams.commands;

import java.util.Map;

import org.apache.log4j.Logger;

import com.architexa.diagrams.Activator;
import com.architexa.diagrams.model.Artifact;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.model.DirectedRel;
import com.architexa.diagrams.parts.BasicRootController;
import com.architexa.diagrams.parts.NavAidsSpec;
import com.architexa.org.eclipse.gef.commands.CompoundCommand;


public final class AddNodeAndRelCmd extends CompoundCommand {
    static final Logger logger = Activator.getLogger(NavAidsSpec.class);
    private AddNodeCommand addNodeCmd;

    public AddNodeAndRelCmd(BasicRootController rc, ArtifactFragment relSrcAF, DirectedRel property, Artifact newArt, Map<Artifact, ArtifactFragment> addedArtToAF) {
		 super("Add Node and Rel");
		 	addNodeCmd = rc.getRootArtifact().addVisibleArt(this, newArt, addedArtToAF);
	        
	    	if (property != null) 
				add(new AddRelCommand(relSrcAF, property, addNodeCmd.getNewArtFrag()));
	}
	public AddNodeAndRelCmd(BasicRootController rc, Artifact newArt, Map<Artifact, ArtifactFragment> addedArtToAFMap) {
		 this(rc, null, null, newArt, addedArtToAFMap);
	}
	
	public ArtifactFragment getNewArtFrag() {
        return addNodeCmd.getNewArtFrag();
	}
	public ArtifactFragment getNewParentArtFrag() {
        return addNodeCmd.getNewParentArtFrag();
	}
	
}