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
 * Created on Jan 16, 2006
 */
package com.architexa.diagrams.eclipse.gef;

import java.lang.reflect.Field;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;

import com.architexa.diagrams.Activator;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.SWTEventDispatcher;
import com.architexa.org.eclipse.draw2d.geometry.Rectangle;
import com.architexa.org.eclipse.gef.ContextMenuProvider;
import com.architexa.org.eclipse.gef.EditPartViewer;


public class GEFMenu {
    static final Logger logger = Activator.getLogger(GEFMenu.class);
    
    private final IFigure parentFig;
    private final EditPartViewer parentViewer;
    private org.eclipse.swt.widgets.Menu menu;

    public GEFMenu(EditPartViewer parentViewer, IFigure parentFig, MenuBuilder mb) {
        this.parentViewer = parentViewer;
        this.parentFig = parentFig;
        MenuManager menuManager = new MenuManager();

        mb.buildMenu(menuManager);
        
        Control parent = parentViewer.getControl();
        Rectangle figBounds = parentFig.getBounds().getCopy();
        parentFig.translateToAbsolute(figBounds);
        Point menuLocation = parent.toDisplay(figBounds.getTopRight().x, figBounds.getTopRight().y);

        menu = menuManager.createContextMenu(parent);
        menu.addMenuListener(new MenuAdapter() {
            @Override
            public void menuHidden(MenuEvent e) {
                hideMenu();
            }
        });
        menu.setLocation(menuLocation);
        menu.setVisible(true);
    }

    public GEFMenu(EditPartViewer parentViewer, IFigure parentFig, final ContextMenuProvider ctxMenu) {
        this(parentViewer, parentFig, new MenuBuilder() {
            public void buildMenu(MenuManager menuManager) {
                ctxMenu.buildContextMenu(menuManager);
            }});
    }
    
    private static final boolean forceGEFToUncaptureSWT = true;

    protected void hideMenu() {
        try {
            if (forceGEFToUncaptureSWT) {
                // BUG in SWTEventDispather: Delete button click Event is not being disposed of correctly.
            	// since this is an event that is consumed, new events are not triggered 
            	// until another 'consumed'/'clickable' event is triggered
            	// We need to set the event to null manually so the dispatcher does not get stuck
            	SWTEventDispatcher evDisp = (SWTEventDispatcher ) parentFig.internalGetEventDispatcher();

            	if (evDisp != null) {
            		Field currentEventField = SWTEventDispatcher.class.getDeclaredField("currentEvent");
            		currentEventField.setAccessible(true);
            		currentEventField.set(evDisp, null);
            		
            		// BUG in SWTEventDispather: we want to uncapture in SWTEventDispather
            		Field capturedField = SWTEventDispatcher.class.getDeclaredField("captured");
            		capturedField.setAccessible(true);
            		capturedField.setBoolean(evDisp, false);
            	}
            }
        } catch (Exception e) {
            logger.error("Unexpected exception", e);
        }

        parentViewer.getControl().getDisplay().asyncExec(new Runnable() {
            public void run() {
                if (menu != null && !menu.isDisposed()) 
                    menu.dispose();
                menu = null;
            }
        });
    }

}
