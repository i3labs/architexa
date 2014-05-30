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
 * Created on Jun 21, 2004
 */
package com.architexa.diagrams.relo.agent;

import org.openrdf.model.URI;

import com.architexa.diagrams.relo.parts.ArtifactEditPart;
import com.architexa.org.eclipse.gef.commands.Command;
import com.architexa.org.eclipse.gef.commands.CompoundCommand;


/**
 * Shows the given relationship if the source and target are already visible
 * 
 * @author vineet
 */
public class IncludedRelationshipsAsserter extends AgentManager.ViewAgent {

	//private final URI relationRes;

	public IncludedRelationshipsAsserter(URI relationRes) {
		//this.relationRes = relationRes;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.EditPartListener#selectedStateChanged(org.eclipse.gef.EditPart)
	 */
	@Override
    public void childAdded(ArtifactEditPart child, int index) {
		child.execute(getCommonRelationshipsCommand(child));
	}

	public Command getCommonRelationshipsCommand(ArtifactEditPart child) {
        //System.err.println("IncludedRelationshipsAsserter: " + child);
        CompoundCommand cmnRelCmd = new CompoundCommand(); 
		// Automatic Relationship display now occurs in RootArtifact.showIncludedRelationships and is
		// executed along with any cmd that adds a node
        // child.showIncludedRelationships(cmnRelCmd, relationRes);
        return cmnRelCmd.unwrap();
	}

}