package com.architexa.internal.debug;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class GetUserInfoDialog extends Dialog{

	private static final String dlgHeader = "Architexa RSE: Generate Unique Id";
	private static final String dlgPrompt = "Enter User Information";
	private Text emailField;
	private Text validity;
	private Text userKeyField;
	private String emailEntered;
	private int validityEntered;
	private String userKeyEntered; 
	
	protected GetUserInfoDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(dlgHeader);
	}
	
	@Override
	protected Control createContents(Composite parent) {
		// create the top level composite for the dialog
		Composite composite = new Composite(parent, 0);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 0;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		applyDialogFont(composite);
		// initialize the dialog units
		initializeDialogUnits(composite);
		// create the dialog area and button bar
		dialogArea = createDialogArea(composite);
		buttonBar = createButtonBar(composite);
		return composite;
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		((GridLayout)parent.getLayout()).marginLeft = 5;

		Composite dialogArea = (Composite) super.createDialogArea(parent);
		((GridLayout)dialogArea.getLayout()).marginWidth = 0;

		Label introLabel = new Label(dialogArea, SWT.LEFT);
		introLabel.setText(dlgPrompt);

		Composite userInfoArea = new Composite(dialogArea, SWT.NONE);
		GridLayout loginAreaLayout = new GridLayout();
		loginAreaLayout.numColumns = 2;
		userInfoArea.setLayout(loginAreaLayout);


		Label emailLabel = new Label(userInfoArea, SWT.LEFT);
		emailLabel.setText("Email: ");

		emailField = new Text(userInfoArea, SWT.SINGLE | SWT.BORDER);
		GridData emailFieldData = new GridData(GridData.FILL_HORIZONTAL);
		emailFieldData.minimumWidth = 200;
		emailField.setLayoutData(emailFieldData);

		Label validityLabel = new Label(userInfoArea, SWT.LEFT);
		validityLabel.setText("Validity: ");

		validity = new Text(userInfoArea, SWT.SINGLE | SWT.BORDER);
		GridData validityFieldData = new GridData(GridData.FILL_HORIZONTAL);
		validity.setLayoutData(validityFieldData);


		Label userKeyLabel = new Label(userInfoArea, SWT.LEFT);
		userKeyLabel.setText("UserKey: ");

		userKeyField = new Text(userInfoArea, SWT.SINGLE | SWT.BORDER);
		GridData userKeyFieldData = new GridData(GridData.FILL_HORIZONTAL);
		userKeyField.setLayoutData(userKeyFieldData);
		
		return dialogArea;
	}
	
	@Override
	public void okPressed(){
		try{
			okPressed_Internal();
		}catch(Throwable t){
			t.printStackTrace();
		}
	}
	
	protected void okPressed_Internal() {

		emailEntered = emailField.getText();
		validityEntered = Integer.parseInt(validity.getText());
		userKeyEntered = userKeyField.getText();
		super.okPressed();
	}

	public String getEmailEntered() {
		return emailEntered;
	}

	public int getValidityEntered() {
		return validityEntered;
	}

	public String getUserKeyEntered() {
		return userKeyEntered;
	}
}

