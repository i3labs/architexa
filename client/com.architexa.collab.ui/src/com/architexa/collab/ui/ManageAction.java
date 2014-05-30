package com.architexa.collab.ui;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;

public class ManageAction extends Action implements IWorkbenchWindowActionDelegate{

	public ManageAction() {
		super("Manage Account Preferences...", Activator.getImageDescriptor("icons/manage.png"));
	}

	public void run(IAction action) {}
	
	@Override
	public void run() {
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		PreferenceDialog pref = PreferencesUtil.createPreferenceDialogOn(shell, "CollabPreferencePage", null, null);
		if (pref != null) pref.open();
	}

	public void dispose() {}
	public void init(IWorkbenchWindow window) {}
	public void selectionChanged(IAction action, ISelection selection) {}
}
