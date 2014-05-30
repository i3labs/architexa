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
package com.architexa.diagrams.relo.jdt.actions;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

import com.architexa.diagrams.relo.ReloPlugin;
import com.architexa.diagrams.relo.jdt.ReloJDTPlugin;
import com.architexa.diagrams.relo.jdt.browse.LiveJDTTracker;


/**
 * Called for when there is no selection
 * 
 * Provides support for using the history / navigation paths
 * 
 * @author vineet
 *
 */
public class OpenReloSessionAction2 implements IWorkbenchWindowActionDelegate {
    static final Logger logger = ReloJDTPlugin.getLogger(OpenReloSessionAction2.class);

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {}
	public void selectionChanged(IAction action, ISelection selection) {}
    public void dispose() {}
    public void init(IWorkbenchWindow window) {}

    
    /**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		try {
            logger.info("Opening Relo Viz for Java Element");

            List<?> navPaths = LiveJDTTracker.getNavigationPaths();

            Shell dlgShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
            OpenReloSessionTreeDialog navSelDlg = new OpenReloSessionTreeDialog(dlgShell, navPaths);
            
            navSelDlg.open();
            if (navSelDlg.getReturnCode() == Window.CANCEL) return;
            
            final IWorkbenchWindow activeWorkbenchWindow = ReloPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow();
            OpenForBrowsingAction.openReloViz(activeWorkbenchWindow, navPaths, null, null, null, null, null);

        } catch (Exception e) {
		    logger.error("Unexpected exception", e);
		}
	}

}
