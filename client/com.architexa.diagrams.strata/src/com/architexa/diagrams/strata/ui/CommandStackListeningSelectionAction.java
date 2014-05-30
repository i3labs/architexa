package com.architexa.diagrams.strata.ui;

import java.util.EventObject;

import org.eclipse.ui.IWorkbenchPart;

import com.architexa.diagrams.strata.parts.StrataRootEditPart;
import com.architexa.diagrams.ui.RSEEditorViewCommon;
import com.architexa.org.eclipse.gef.commands.CommandStackListener;
import com.architexa.org.eclipse.gef.ui.actions.SelectionAction;

public class CommandStackListeningSelectionAction extends SelectionAction {

	public CommandStackListeningSelectionAction(IWorkbenchPart part) {
		super(part);
	}

	@Override
	protected boolean calculateEnabled() {
		return false;
	}
	
	 @Override
		public void update() {
			super.update();

			// other places get called too early - this gets called on the first
			// selection - which works for us
			if (!cslInit) {
				cslInit = true;
				getRootController().getViewer().getEditDomain().getCommandStack().addCommandStackListener(new CommandStackListener() {
					public void commandStackChanged(EventObject event) {
						CommandStackListeningSelectionAction.this.refresh();
					}});
			}
		}
		
		private boolean cslInit = false;

		StrataRootEditPart getRootController() {
			return (StrataRootEditPart) ((RSEEditorViewCommon.IRSEEditorViewCommon) getWorkbenchPart()).getRootController();
		}

}

