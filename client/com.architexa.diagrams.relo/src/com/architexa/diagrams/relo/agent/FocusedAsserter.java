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
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.widgets.Display;

import com.architexa.diagrams.relo.parts.ArtifactEditPart;
import com.architexa.org.eclipse.gef.EditPart;
import com.architexa.org.eclipse.gef.commands.Command;
import com.architexa.org.eclipse.gef.commands.CompoundCommand;


/**
 * @author vineet
 * 
 * Manages focus. Does: 1] Old focus is suggested to decrease DL 2] New focus is
 * suggested to show parent 3] New focus is suggested to increase DL
 */
public class FocusedAsserter extends AgentManager.ViewAgent {

	private Map<ArtifactEditPart,Runnable> scheduledRunnableMap = Collections.synchronizedMap(new HashMap<ArtifactEditPart,Runnable> ());
	private void selectionSchedule(final ArtifactEditPart part, final Command cmd) {
		Runnable r = new Runnable() {
			public void run() {
				scheduledRunnableMap.remove(part);
				part.execute(cmd);
			}
		};
		Display.getDefault().timerExec(500, r);
        scheduledRunnableMap.put(part, r);
	}
	private boolean isSelectionScheduled(ArtifactEditPart part) {
		return scheduledRunnableMap.containsKey(part);
	}
	private void removeSchedule(ArtifactEditPart part) {
		Runnable r = (Runnable) scheduledRunnableMap.get(part);
		scheduledRunnableMap.remove(part);
		Display.getDefault().timerExec(-1, r);
	}

	@Override
    public void selectedStateChanged(ArtifactEditPart part) {

		if (part.getSelected() == EditPart.SELECTED_PRIMARY) {
			selectionSchedule(part, getNewSelectionCommand(part));
		} else if (part.getSelected() == EditPart.SELECTED_NONE) {
			if (isSelectionScheduled(part)) {
				removeSchedule(part);
			} else {
				part.execute(getOldSelectionCommand(part));
			}
		}
	}

	public Command getNewSelectionCommand(final ArtifactEditPart childArtEP) {
		return new Command() {
			@Override
            public void execute() {
				if (childArtEP instanceof ArtifactEditPart) {
				    ArtifactEditPart childAEP = (ArtifactEditPart) childArtEP;
					
                    //childAEP.realizeParent();
                    CompoundCommand relaizeParentCmd = new CompoundCommand();
                    childAEP.realizeParent(relaizeParentCmd);
                    relaizeParentCmd.execute();
                    
					childAEP.suggestDetailLevelIncrease();
				}
			}
		};
	}

	public Command getOldSelectionCommand(final ArtifactEditPart childArtEP) {
		return new Command() {
			@Override
            public void execute() {
				childArtEP.suggestDetailLevelDecrease();
			}
		};
	}

}