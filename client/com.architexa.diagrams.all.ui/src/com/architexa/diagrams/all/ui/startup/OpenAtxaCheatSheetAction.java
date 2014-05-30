package com.architexa.diagrams.all.ui.startup;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.cheatsheets.OpenCheatSheetAction;

public class OpenAtxaCheatSheetAction implements IWorkbenchWindowActionDelegate {

	final String cheatSheetId = "com.architexa.diagrams.all.ui.tutorialCheatSheet";
	public void run(IAction action) {
		OpenCheatSheetAction openAction = new OpenCheatSheetAction(cheatSheetId);
		openAction.run();
	}

	public void selectionChanged(IAction action, ISelection selection) {
		
	}

	public void dispose() {
		
	}

	public void init(IWorkbenchWindow window) {
		
	}

}
