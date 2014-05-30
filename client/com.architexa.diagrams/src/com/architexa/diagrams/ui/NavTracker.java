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
 * Created on Aug 12, 2005
 */
package com.architexa.diagrams.ui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.openrdf.model.URI;

import com.architexa.diagrams.Activator;
import com.architexa.diagrams.model.DocPath;


/**
 * TODO {P5}: Make the navigation paths not keep on increasing.
 * 
 * @author vineet
 */
public abstract class NavTracker {
    static final Logger logger = Activator.getLogger(NavTracker.class);
    
    protected final ISelectionListener selProcessor = new ISelectionListener() {
        public void selectionChanged(IWorkbenchPart selectedPart, ISelection selection) {
        	try {
        		if (selection instanceof IStructuredSelection) {
                    Iterator<?> sSelIt = ((IStructuredSelection)selection).iterator();
                    while (sSelIt.hasNext()) {
            			processSelection(selectedPart, sSelIt.next());
                    }
            	} else if (selection instanceof ITextSelection) {
        			processSelection(selectedPart, (ITextSelection) selection);
            	}
        	} catch (Throwable t) {
        		logger.error("Unexpected exception", t);
        	}
        }
    };

    protected List<Object> navigationPaths;
    
    public static final int navEventsMaxTrackingSize = 500;
    public static final int navEventsFlushSize = navEventsMaxTrackingSize/2;

    public NavTracker() {
        navigationPaths = new ArrayList<Object>(navEventsMaxTrackingSize);
    }
    
    protected boolean addListeners() {
        IWorkbenchWindow[] wwin = PlatformUI.getWorkbench().getWorkbenchWindows();
        for (IWorkbenchWindow tgtWkbnchWin : wwin) {
            tgtWkbnchWin.getSelectionService().addPostSelectionListener(selProcessor);
        }
        
        return true;
    }
    protected void removeListeners() {
        IWorkbenchWindow[] wwin = PlatformUI.getWorkbench().getWorkbenchWindows();
        for (IWorkbenchWindow tgtWkbnchWin : wwin) {
            tgtWkbnchWin.getSelectionService().removePostSelectionListener(selProcessor);
        }
    }
    
    private void checkNavPathsSize() {
        if (navigationPaths.size() < navEventsMaxTrackingSize) return;

        // remove first 100 objects
        List<Object> origNavPaths = navigationPaths;
        navigationPaths = new ArrayList<Object>(navEventsMaxTrackingSize);
        navigationPaths.addAll(origNavPaths.subList(navEventsMaxTrackingSize-navEventsFlushSize, origNavPaths.size()));
    }

    protected void addNavigationItem(IWorkbenchPart selectedPart, Object selElement) {
        //System.err.println("Navigated: " + OpenReloSessionAction2.dump(selElement));
        checkNavPathsSize();
        navigationPaths.add(selElement);
    }

    protected void addNavigationPath(Object prevElement, URI addingRel, Object selElement) {
        //System.err.println("Navigated: " + OpenReloSessionAction2.dump(new DocPath(prevElement, addingRel, selElement)));
        checkNavPathsSize();
        navigationPaths.add(new DocPath(prevElement, addingRel, selElement));
    }

    /*
     * TODO: There will be bugs in tracking when there are two windows, however,
     * we might want to get this code having two seperate instances (per
     * workbench window)
     */
    
    ////////////////////////////////////////////////////////////
    /// Actual Selection Processing Code
    ////////////////////////////////////////////////////////////
    
    
    protected void processSelection(IWorkbenchPart selectedPart, ITextSelection textSelection) {}
    
    protected void processSelection(IWorkbenchPart selectedPart, Object selectionItem) {}


}
