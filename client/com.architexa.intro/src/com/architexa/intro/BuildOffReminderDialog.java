package com.architexa.intro;

import java.net.URL;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.architexa.intro.preferences.PreferenceConstants;

public class BuildOffReminderDialog extends Dialog{
	private Button dontShow;
	String showText = "Eclipse Auto Build is turned off which can lead to indexing outdated class files" +
		"\nIt is recommended to have the 'Project > Build Automatically' option selected " +
   		"\n\nIf 'Ant' is used for the build, it is recommended to configure " +
   		"\nArchitexa as mentioned here";
	
	String url = "http://www.architexa.com/user-guide/Configuration/Configure_for_Ant";
	
	public BuildOffReminderDialog(Shell shell) {
		super(shell);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Architexa: AutoBuild Switched Off");
    }
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		Label label = new Label(composite, SWT.WRAP);
		label.setText(showText.toString());
		
		Link diagramLink = new Link(composite , SWT.LEFT);
		diagramLink.setText("<a>"+url+"</a>");
		diagramLink.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {}
			public void widgetSelected(SelectionEvent e) {
				try {
						PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(new URL(url));
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}});
		dontShow = new Button(composite, SWT.CHECK);
		dontShow.setText("Do not show again");
		return composite;
	}
	 
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
    }
	
	@Override
	protected void okPressed() {
		checkAndSavePref();
		super.okPressed();
	}
	

	private void checkAndSavePref() {
		AtxaIntroPlugin.getDefault().getPreferenceStore().setValue(PreferenceConstants.BUILD_OFF_REMINDER_KEY, !dontShow.getSelection());
	}
}
