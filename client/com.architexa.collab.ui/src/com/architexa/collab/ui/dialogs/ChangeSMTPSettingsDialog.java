package com.architexa.collab.ui.dialogs;

import java.net.ConnectException;
import java.net.UnknownHostException;

import javax.mail.AuthenticationFailedException;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.architexa.collab.UIUtils;
import com.architexa.collab.ui.ShareByEmailAction;
import com.architexa.rse.AccountSettings;

public class ChangeSMTPSettingsDialog extends Dialog {

	Text serverField;
	Text portField;
	Text usernameField;
	Text passwordField;

	String server;
	String port;
	String username;
	String password;

	public ChangeSMTPSettingsDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText("SMTP Settings");
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite dialogArea = (Composite) super.createDialogArea(parent);
		((GridLayout)dialogArea.getLayout()).numColumns = 2;

		// SMTP Server
		Label serverLabel = new Label(dialogArea, SWT.LEFT);
		serverLabel.setText("Server: ");

		serverField = new Text(dialogArea, SWT.SINGLE | SWT.BORDER);
		GridData serverFieldData = new GridData(GridData.FILL_HORIZONTAL);
		serverFieldData.minimumWidth = 200;
		serverField.setLayoutData(serverFieldData);
		serverField.setText(AccountSettings.getStoredSMTPServer());

		// Port
		Label portLabel = new Label(dialogArea, SWT.LEFT);
		portLabel.setText("Port: ");

		portField = new Text(dialogArea, SWT.SINGLE | SWT.BORDER);
		GridData portFieldData = new GridData(GridData.FILL_HORIZONTAL);
		portFieldData.minimumWidth = 200;
		portField.setLayoutData(portFieldData);
		portField.setText(AccountSettings.getStoredSMTPPort());

		// Username
		Label usernameLabel = new Label(dialogArea, SWT.LEFT);
		usernameLabel.setText("Username: ");

		usernameField = new Text(dialogArea, SWT.SINGLE | SWT.BORDER);
		GridData usernameFieldData = new GridData(GridData.FILL_HORIZONTAL);
		usernameFieldData.minimumWidth = 200;
		usernameField.setLayoutData(usernameFieldData);
		usernameField.setText(AccountSettings.getStoredSMTPUsername());

		// Password
		Label passwordLabel = new Label(dialogArea, SWT.LEFT);
		passwordLabel.setText("Password: ");

		passwordField = new Text(dialogArea, SWT.SINGLE | SWT.PASSWORD | SWT.BORDER);
		GridData passwordFieldData = new GridData(GridData.FILL_HORIZONTAL);
		passwordFieldData.minimumWidth = 200;
		passwordField.setLayoutData(passwordFieldData);
		passwordField.setText(AccountSettings.getStoredSMTPPassword());

		return dialogArea;
	}

	@Override
	protected void okPressed() {

		String enteredServer = serverField.getText();
		String enteredPort = portField.getText();

		// Check that Server and Port fields are not empty
		if(enteredServer==null || "".equals(enteredServer.trim())
				|| enteredPort==null || "".equals(enteredPort.trim())) {
			UIUtils.errorPromptDialog("Please enter a SMTP Server and Port Number").open();
			resetOKButton();
			return;
		}

		// Check that entered Port is a number
		int portNum = 25;
		try {
			portNum = Integer.parseInt(enteredPort);
		} catch(NumberFormatException e) {
			UIUtils.errorPromptDialog("Port should be a number").open();
			resetOKButton();
			return;
		}

		// Test the given SMTP server by trying to connect
		// to it with the provided username and password
		getButton(IDialogConstants.OK_ID).setText("Testing...");
		String enteredUsername = usernameField.getText();
		String enteredPassword = passwordField.getText();
		Exception e = ShareByEmailAction.testSMTPServer(enteredServer, portNum, 
				enteredUsername, enteredPassword);
		if(e!=null) {
			if(e instanceof AuthenticationFailedException) {
				UIUtils.errorPromptDialog("Unable to authenticate. Be sure the " +
				"username and password you entered are correct.").open();
				resetOKButton();
				return;
			}
			if(e.getCause() instanceof UnknownHostException) {
				UIUtils.errorPromptDialog("Unknown SMTP host: " +enteredServer).open();
				resetOKButton();
				return;
			}
			if(e.getCause() instanceof ConnectException) {
				UIUtils.errorPromptDialog("Could not connect to " +
						"supplied SMTP host: "+enteredServer).open();
				resetOKButton();
				return;
			}
			System.out.println("cause: " + e.getCause());
			e.printStackTrace();
			UIUtils.errorPromptDialog(e.getMessage()).open();
			resetOKButton();
			return;
		}

		server = enteredServer;
		port = enteredPort;
		username = enteredUsername;
		password = enteredPassword;

		super.okPressed();
	}

	private void resetOKButton() {
		getButton(IDialogConstants.OK_ID).setText("OK");
	}

	public String getServer() {
		return server;
	}

	public String getPort() {
		return port;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

}
