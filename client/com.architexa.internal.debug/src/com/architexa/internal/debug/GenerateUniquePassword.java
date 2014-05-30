package com.architexa.internal.debug;

import java.util.Calendar;

import org.apache.commons.codec.binary.Base64;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

public class GenerateUniquePassword implements IWorkbenchWindowActionDelegate {

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void init(IWorkbenchWindow window) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void run(IAction action) {
		getUniqueKey();
	}

	private void getUniqueKey() {
		
		String str = generateKey();
		if (str == null) return;
		
		InputDialog dialog = new InputDialog(PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getShell(), "Architexa: Unique Id", "Generated PassKey", str, null);
		dialog.open();
	}

	private String generateKey() {
		GetUserInfoDialog dialog = new GetUserInfoDialog(PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getShell());
		dialog.open();
		if (dialog.getReturnCode() == Dialog.OK) {
			String email = dialog.getEmailEntered();
			int validity = dialog.getValidityEntered();
			String key = dialog.getUserKeyEntered();
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DATE, validity);
			long expiry = cal.getTimeInMillis();
			String passKey = new String(Base64.encodeBase64((email + "::" + key + "::" + expiry).getBytes()));
			passKey = "atxa::" + passKey;
			return passKey;
		}
		return null;
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub
		
	}

}
