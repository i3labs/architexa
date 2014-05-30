package com.architexa.rse;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

public class OpenHelpAction implements IWorkbenchWindowActionDelegate {

	public void run(IAction action) {
		PlatformUI.getWorkbench().getHelpSystem().displayHelp();
	}

	public void selectionChanged(IAction action, ISelection selection) {}
	public void dispose() {}
	public void init(IWorkbenchWindow window) {}

}
