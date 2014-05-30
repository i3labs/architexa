package com.architexa.intro;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.architexa.rse.AccountSettings;
import com.architexa.rse.update.UpdateAction;

public class UpgradeInstallDialog extends Dialog{

	private Button dontShow;
	String Subclipse;
	String Spring;
	StringBuffer showText = new StringBuffer();
	
	public UpgradeInstallDialog(Shell shell, boolean hasSubclipse, boolean hasSpring) {
		super(shell);
		createText(hasSpring, hasSubclipse);
	}

	private void createText(boolean hasSpring, boolean hasSubclipse) {
		showText.append("It seems that you have the following installed:");
		if (hasSubclipse)
			showText.append("\n>> Subclipse");
		if (hasSpring)
			showText.append("\n>> Spring");
		
		showText.append("\n\nArchitexa provides advanced functionalities for these features.");
		
		if (hasSubclipse)
			showText.append("\n>> Subclipse support:" +
					"\n\tArchitexa - extensions" +
					"\n\t\t-- Architexa RSE - Subclipse Integeration\n");
		if (hasSpring)
			showText.append("\n>> Spring support:" +
					"\n\tArchitexa - other (experimental)" +
					"\n\t\t-- Architexa RSE - Enterprise Java Support" +
					"\n\t\t-- Architexa RSE - Enterprise Java Support - Spring\n");
					
		showText.append("\nPlease click 'Install Now' to get these features.");
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Architexa RSE Upgrade");
    }
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		Label label = new Label(composite, SWT.WRAP);
		label.setText(showText.toString());
		
		dontShow = new Button(composite, SWT.CHECK);
		dontShow.setText("Do not show again");
		return composite;
	}
	 
//	@Override
//	protected Control createContents(Composite parent) {
//		Composite composite = (Composite) super.createDialogArea(parent);
//		Label label = new Label(composite, SWT.WRAP);
//		label.setText("It seems that you have subclipse installed");
//		
//		dontShow = new Button(composite, SWT.CHECK);
//		dontShow.setText("Do not show again");
//		return composite;
//	}
	
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, "Install Now", true);
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
    }
	
	@Override
	protected void okPressed() {
		super.okPressed();
		new UpdateAction(true).run();
	}
	
	@Override
	protected void cancelPressed() {
		checkAndSavePref();
		super.cancelPressed();
	}


	private void checkAndSavePref() {
		AccountSettings.setSubclipseReminder(dontShow.getSelection());
	}
}
