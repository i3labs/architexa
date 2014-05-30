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

import java.util.Iterator;


import com.architexa.diagrams.relo.commands.SetComposableCommand;
import com.architexa.diagrams.relo.modelBridge.JoinedRelType;
import com.architexa.diagrams.relo.parts.ArtifactEditPart;
import com.architexa.org.eclipse.gef.commands.Command;


/**
 * Ensures that all elements on a valid path (given by rel) are shown
 * 
 * @author vineet
 * 
 * TODO: support domain range restriction
 * TODO: support having complex relationships with only some variables being shown 
 */
public class PathElementsAsserter extends AgentManager.ViewAgent {

	private final JoinedRelType rel;

	public PathElementsAsserter(JoinedRelType rel) {
		this.rel = rel;
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
		return new SetComposableCommand(child) {
			@Override
            public void execute() {
				Iterator<?> childIt = setParam.iterator();
				while (childIt.hasNext()) {
					Object currChild = childIt.next();
					if (currChild instanceof ArtifactEditPart)
						((ArtifactEditPart) currChild).showPathElements(rel);
				}
			}
		};
	}

}