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
 * Created on Jan 24, 2005
 */
package com.architexa.diagrams.eclipse.gef;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;

import com.architexa.diagrams.Activator;
import com.architexa.org.eclipse.draw2d.ChangeEvent;
import com.architexa.org.eclipse.draw2d.ChangeListener;
import com.architexa.org.eclipse.draw2d.Clickable;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.Toggle;
import com.architexa.org.eclipse.gef.EditPartViewer;


/**
 * @author vineet
 *
 */
public abstract class MenuButton extends Toggle {
    static final Logger logger = Activator.getLogger(MenuButton.class);
    
    private EditPartViewer parentViewer;

    public MenuButton(IFigure contents, final EditPartViewer parentViewer) {
        super(contents, Clickable.STYLE_BUTTON);
        this.parentViewer = parentViewer;

        setRolloverEnabled(true);
        
        // the change listener actually creates the menu
		addChangeListener(new ButtonChangeListener());
    }

    public abstract void buildMenu(IMenuManager menu);

    
    private final class ButtonChangeListener implements ChangeListener {

        public void showMenu() {
            getModel().setSelected(true);
        }

        public void hideMenu() {
            getModel().setSelected(false);
            getModel().setPressed(false);
            getModel().setArmed(false);
        }

        public void handleStateChanged(ChangeEvent event) {
            //logEvent(event);

            if (event.getPropertyName().equals("pressed")) {
                if (getModel().isPressed()) {

                    showMenu();

        		    //ConsoleView.log("Showing menu");
                    new GEFMenu(parentViewer, MenuButton.this, new MenuBuilder() {
                        public void buildMenu(MenuManager menuManager) {
                            MenuButton.this.buildMenu(menuManager);
                        }
                    }) {
                        @Override
                        protected void hideMenu() {
                            ButtonChangeListener.this.hideMenu();
                            super.hideMenu();
                        }
                    };

            		
                }
            }
        }

    }


}
