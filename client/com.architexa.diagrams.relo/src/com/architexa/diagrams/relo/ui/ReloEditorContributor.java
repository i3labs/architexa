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
 * Created on Jun 12, 2004
 *
 */
package com.architexa.diagrams.relo.ui;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.actions.ActionFactory;

import com.architexa.org.eclipse.gef.editparts.ZoomManager;
import com.architexa.org.eclipse.gef.ui.actions.ActionBarContributor;
import com.architexa.org.eclipse.gef.ui.actions.DeleteRetargetAction;
import com.architexa.org.eclipse.gef.ui.actions.RedoRetargetAction;
import com.architexa.org.eclipse.gef.ui.actions.UndoRetargetAction;
import com.architexa.org.eclipse.gef.ui.actions.ZoomComboContributionItem;
import com.architexa.org.eclipse.gef.ui.actions.ZoomInRetargetAction;
import com.architexa.org.eclipse.gef.ui.actions.ZoomOutRetargetAction;

/**
 * @author vineet
 *
 */
public class ReloEditorContributor extends ActionBarContributor {
	
	/* (non-Javadoc)
	 * @see org.eclipse.gef.ui.actions.ActionBarContributor#buildActions()
	 */
	@Override
    protected void buildActions() {
		addRetargetAction(new UndoRetargetAction());
		addRetargetAction(new RedoRetargetAction());
		addRetargetAction(new DeleteRetargetAction());
		
		addRetargetAction(new ZoomInRetargetAction());
		addRetargetAction(new ZoomOutRetargetAction());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gef.ui.actions.ActionBarContributor#declareGlobalActionKeys()
	 */
	@Override
    protected void declareGlobalActionKeys() {
		addGlobalActionKey(ActionFactory.PRINT.getId());
        addGlobalActionKey(ActionFactory.SELECT_ALL.getId());
	}


	/**
	 * @see org.eclipse.ui.part.EditorActionBarContributor#contributeToToolBar(IToolBarManager)
	 */
	@Override
    public void contributeToToolBar(IToolBarManager tbm) {
		//tbm.add(getAction(ActionFactory.UNDO.getId()));
		//tbm.add(getAction(ActionFactory.REDO.getId()));
		
		tbm.add(new Separator());	
		String[] zoomStrings = new String[] {	ZoomManager.FIT_ALL, 
												ZoomManager.FIT_HEIGHT, 
												ZoomManager.FIT_WIDTH	};
		tbm.add(new ZoomComboContributionItem(getPage(), zoomStrings));
	}

	/**
	 * @see org.eclipse.ui.part.EditorActionBarContributor#contributeToMenu(IMenuManager)
	 */
//	@Override
//    public void contributeToMenu(IMenuManager menubar) {
//		super.contributeToMenu(menubar);
//		MenuManager viewMenu = new MenuManager("Class Diagrams");
//		viewMenu.add(getAction(GEFActionConstants.ZOOM_IN));
//		viewMenu.add(getAction(GEFActionConstants.ZOOM_OUT));
//        //viewMenu.add(new Separator("model"));
//        //TODO: browseModel should add here
//
//        menubar.insertAfter(IWorkbenchActionConstants.M_EDIT, viewMenu);
//	}



}
