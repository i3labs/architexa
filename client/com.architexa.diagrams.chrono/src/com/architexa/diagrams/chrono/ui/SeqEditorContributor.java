package com.architexa.diagrams.chrono.ui;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.actions.ActionFactory;

import com.architexa.org.eclipse.gef.editparts.ZoomManager;
import com.architexa.org.eclipse.gef.ui.actions.ActionBarContributor;
import com.architexa.org.eclipse.gef.ui.actions.RedoRetargetAction;
import com.architexa.org.eclipse.gef.ui.actions.UndoRetargetAction;
import com.architexa.org.eclipse.gef.ui.actions.ZoomComboContributionItem;
import com.architexa.org.eclipse.gef.ui.actions.ZoomInRetargetAction;
import com.architexa.org.eclipse.gef.ui.actions.ZoomOutRetargetAction;

/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public class SeqEditorContributor extends ActionBarContributor {

	@Override
	protected void buildActions() {
		addRetargetAction(new UndoRetargetAction());
		addRetargetAction(new RedoRetargetAction());
		addRetargetAction(new ZoomInRetargetAction());
		addRetargetAction(new ZoomOutRetargetAction());
	}

	@Override
    public void contributeToToolBar(IToolBarManager tbm) {
		tbm.add(new Separator());	
		String[] zoomStrings = new String[] {	ZoomManager.FIT_ALL, 
												ZoomManager.FIT_HEIGHT, 
												ZoomManager.FIT_WIDTH	};
		tbm.add(new ZoomComboContributionItem(getPage(), zoomStrings));
	}

	@Override
	protected void declareGlobalActionKeys() {
		addGlobalActionKey(ActionFactory.PRINT.getId());
	}

}
