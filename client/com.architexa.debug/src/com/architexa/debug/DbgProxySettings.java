package com.architexa.debug;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

import com.architexa.collab.CollabPlugin;

public class DbgProxySettings implements IWorkbenchWindowActionDelegate{

	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	public void init(IWorkbenchWindow arg0) {
		// TODO Auto-generated method stub
		
	}

	public void run(IAction arg0) {
		debugProxySettings();
	}


	private void debugProxySettings() {
		MessageDialog dialog = new MessageDialog(PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getShell(), "Architexa: Proxy Settings", null, CollabPlugin.getDebugStr(), MessageDialog.INFORMATION,
				new String[]{"OK"}, 1) {
			
		};
		dialog.open();
	}
	
	public void selectionChanged(IAction arg0, ISelection arg1) {
		// TODO Auto-generated method stub
		
	}

}
