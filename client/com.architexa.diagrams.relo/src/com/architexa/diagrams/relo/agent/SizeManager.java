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
import java.util.Map;


import com.architexa.diagrams.relo.ReloPlugin;
import com.architexa.diagrams.relo.commands.SetComposableCommand;
import com.architexa.diagrams.relo.parts.AbstractReloEditPart;
import com.architexa.diagrams.relo.parts.ArtifactEditPart;
import com.architexa.diagrams.relo.parts.ReloController;
import com.architexa.org.eclipse.draw2d.geometry.Dimension;
import com.architexa.org.eclipse.draw2d.geometry.Rectangle;
import com.architexa.org.eclipse.gef.commands.Command;
import com.architexa.org.eclipse.gef.editparts.ScalableFreeformRootEditPart;



/**
 * @author vineet
 *  
 */
public class SizeManager extends AgentManager.ViewAgent {

	Dimension windowDim = null;

	public SizeManager() {
		// aproximate window size based on screen size
		Rectangle windowRect = new Rectangle(ReloPlugin.getDefault()
				.getWorkbench().getActiveWorkbenchWindow().getShell()
				.getBounds());
		windowRect.scale(0.66);
		windowDim = windowRect.getSize();

	}

	@Override
    public void childAdded(ArtifactEditPart child, int index) {
		child.execute(getSizeManagerCommand(child));
	}

	public Command getSizeManagerCommand(ArtifactEditPart child) {

		return new SetComposableCommand(child) {
			@Override
            public void execute() {
				Iterator<?> spi = setParam.iterator();
				AbstractReloEditPart childAVEP = null;
				while (spi.hasNext()) {
					childAVEP = (AbstractReloEditPart) spi.next();
					if (childAVEP.isActive()) {
						break;
					}
				}
				if (!childAVEP.isActive()) {
					// all children are not active
					return;
				}
				//ScalableRootEditPart rootSFREP = (ScalableRootEditPart) childAVEP.getRoot();
				ScalableFreeformRootEditPart rootSFREP = (ScalableFreeformRootEditPart) childAVEP.getRoot();
				Dimension contentDim = rootSFREP.getContentPane().getBounds().getSize();
				// TODO if there is only one element, then realizeChildren
				if (windowDim.height < contentDim.height
						|| windowDim.width < contentDim.width) {
					
					ReloController vc = (ReloController) rootSFREP.getContents();
					Map<?,?> editPartRegistry = childAVEP.getViewer().getEditPartRegistry();
					Iterator<?> epIt = vc.getModelChildren().iterator();
					while (epIt.hasNext()) {
						ArtifactEditPart aep = (ArtifactEditPart) editPartRegistry.get(epIt.next());
						aep.suggestDetailLevelDecrease();
					}

				}
			}
		};
	}

}