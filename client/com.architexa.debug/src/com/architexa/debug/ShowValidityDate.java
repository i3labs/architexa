package com.architexa.debug;

import java.util.Date;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

public class ShowValidityDate implements IWorkbenchWindowActionDelegate{

	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	public void init(IWorkbenchWindow arg0) {
		// TODO Auto-generated method stub
		
	}

	public void run(IAction arg0) {
		showValidityDate();
	}

	private void showValidityDate() {
		MessageDialog dialog = new MessageDialog(PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getShell(), "Architexa: Valdity Date", null, getDebugStr(), MessageDialog.INFORMATION,
				new String[]{"OK"}, 1) {
			
		};
		dialog.open();
	}
	
	private String getDebugStr() {
		String str = "";
		str += "System Date: " + new Date().toString();
		return str;
	}

	public void selectionChanged(IAction arg0, ISelection arg1) {
		// TODO Auto-generated method stub
		
	}

}
