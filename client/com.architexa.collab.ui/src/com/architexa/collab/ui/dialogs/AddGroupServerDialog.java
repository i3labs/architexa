package com.architexa.collab.ui.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.architexa.collab.UIUtils;

/**
 * 
 * @author Vineet Sinha
 *
 */
public class AddGroupServerDialog extends Dialog {

	Text serverURLField;
	String serverURL = null;

	public AddGroupServerDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText("Add Group Server");
	}

	@Override
	protected Control createDialogArea(Composite parent) {

		Composite dialogArea = (Composite) super.createDialogArea(parent);
		((GridLayout)dialogArea.getLayout()).numColumns = 2;

		Label lbl;
		
		lbl = new Label(dialogArea, SWT.LEFT);
		lbl.setText("URL of Group Server: ");

		serverURLField = new Text(dialogArea, SWT.SINGLE | SWT.BORDER);
		GridData serverURLFieldData = new GridData(GridData.FILL_HORIZONTAL);
		serverURLFieldData.minimumWidth = 200;
		serverURLField.setLayoutData(serverURLFieldData);

		lbl = new Label(dialogArea, SWT.LEFT);
		lbl.setText("Eg.: http://architexa.company.com/");
		GridData exampleFieldData = new GridData(GridData.FILL_HORIZONTAL);
		exampleFieldData.horizontalSpan = 2;
		lbl.setLayoutData(exampleFieldData);
		
		/*
		lbl = new Label(dialogArea, SWT.LEFT);
		lbl.setText("URL of Group Server: ");

		serverURLField = new Text(dialogArea, SWT.SINGLE | SWT.BORDER);
		GridData serverURLFieldData = new GridData(GridData.FILL_HORIZONTAL);
		serverURLFieldData.minimumWidth = 200;
		serverURLField.setLayoutData(serverURLFieldData);
		
		lbl = new Label(dialogArea, SWT.LEFT);
		lbl.setText("URL of Group Server: ");

		serverURLField = new Text(dialogArea, SWT.SINGLE | SWT.BORDER);
		GridData serverURLFieldData = new GridData(GridData.FILL_HORIZONTAL);
		serverURLFieldData.minimumWidth = 200;
		serverURLField.setLayoutData(serverURLFieldData);
		*/

		return dialogArea;
	}

	@Override
	protected void okPressed() {

		if(serverURLField.getText()==null || "".equals(serverURLField.getText().trim())) {
			UIUtils
				.errorPromptDialog("Please enter the URL of the new group server")
				.open();
			return;
		}

		serverURL = serverURLField.getText();

		super.okPressed();
	}

	public String getServerURL() {
		return serverURL;
	}

}
