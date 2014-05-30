/**
 * Copyright (c) 2007-2008 Architexa, Inc
 */ 
package com.architexa.diagrams.strata.ui;

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

public class StrataEditorContributor extends ActionBarContributor {

	public StrataEditorContributor() {
	}

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

	@Override
	protected void declareGlobalActionKeys() {
		addGlobalActionKey(ActionFactory.PRINT.getId());
        addGlobalActionKey(ActionFactory.SELECT_ALL.getId());
	}

	@Override
    public void contributeToToolBar(IToolBarManager tbm) {
		tbm.add(new Separator());	
		String[] zoomStrings = new String[] {	ZoomManager.FIT_ALL, 
												ZoomManager.FIT_HEIGHT, 
												ZoomManager.FIT_WIDTH	};
		tbm.add(new ZoomComboContributionItem(getPage(), zoomStrings));
	}

//	@Override
//    public void contributeToMenu(IMenuManager menubar) {
//		super.contributeToMenu(menubar);
//		MenuManager viewMenu = new MenuManager("Layered Diagrams");
//		viewMenu.add(getAction(GEFActionConstants.ZOOM_IN));
//		viewMenu.add(getAction(GEFActionConstants.ZOOM_OUT));
//
//        menubar.insertAfter(IWorkbenchActionConstants.M_EDIT, viewMenu);
//	}
}
