package com.architexa.debug;

import java.util.Calendar;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

public class ToggleValidityDate implements IWorkbenchWindowActionDelegate{

	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	public void init(IWorkbenchWindow arg0) {
		// TODO Auto-generated method stub
		
	}

	public void run(IAction arg0) {
	}

	private long getValidityString() {
		Calendar cal = Calendar.getInstance();
		String str = ((Long)cal.getTimeInMillis()).toString();
		InputDialog dialog = new InputDialog(PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getShell(), "Architexa: Toggle Valdity Date", null, str, null);
		if (dialog.open() == Window.OK) {
			return Long.parseLong(dialog.getValue());
		}
		return 0;
	}

	public void selectionChanged(IAction arg0, ISelection arg1) {
		// TODO Auto-generated method stub
		
	}

}
