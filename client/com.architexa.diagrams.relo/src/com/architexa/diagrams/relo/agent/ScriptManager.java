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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import com.architexa.diagrams.relo.ReloPlugin;
import com.architexa.diagrams.relo.parts.ArtifactEditPart;
import com.architexa.org.eclipse.gef.EditPart;
import com.architexa.org.eclipse.gef.EditPartViewer;


/**
 * @author vineet
 * 
 * Support for scripting purposes
 */
public class ScriptManager extends AgentManager.SystemViewAgent {
    static final Logger logger = ReloPlugin.getLogger(ScriptManager.class);

    // we are making this static since there are really multiple instances of ScriptManager
	public static List<ArtifactEditPart> selection;
	
	static {
        selection = Collections.synchronizedList(new LinkedList<ArtifactEditPart>());
        //ConsoleView.setVariable("sel", selection, "Reflects current selection");
    }

	@Override
    public void selectedStateChanged(ArtifactEditPart part) {
		if (part.getSelected() == EditPart.SELECTED_PRIMARY || part.getSelected() == EditPart.SELECTED) {
			addToSel(part);
		} else if (part.getSelected() == EditPart.SELECTED_NONE) {
		    removeFromSel(part);
		}
	}

	@Override
	public void deletingChild(ArtifactEditPart part) {
	    removeFromSel(part);
    }

	private void addToSel(ArtifactEditPart part) {
		if (selection.contains(part)) return;
		selection.add(part);
		doSync(part.getViewer());
	}

	private void removeFromSel(ArtifactEditPart part) {
		selection.remove(part);
		doSync(part.getViewer());
	}
	
	private void doSync(EditPartViewer epViewer) {
		selection.clear();
		for (Object elem : epViewer.getSelectedEditParts()) {
			if (elem instanceof ArtifactEditPart)
				selection.add((ArtifactEditPart)elem);
		}
	}

	/**
	 * provided for debugging 
	 */
	public static void dmpSel(String prompt) {
        logger.error(prompt + " selSize: " + selection.size());
        for (Object selectedObj : selection) {
            logger.error("sel: " + selectedObj);
        }
	}

}