package com.architexa.collab.ui.dialogs;

import java.util.Arrays;
import java.util.List;

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
import com.architexa.collab.ui.ContactUtils;
import com.architexa.rse.AccountSettings;

public class AddOrEditContactDialog extends Dialog {

	Text emailField;
	String contactEmail = null;
	String emailToEdit = null;

	/**
	 * 
	 * @param parentShell
	 * @param emailToEdit the email address of an existing contact that the user has
	 * selected to edit via the pref page's Edit Contact button or null if he has 
	 * pressed the Add Contact button to add an entirely new contact
	 */
	public AddOrEditContactDialog(Shell parentShell, String emailToEdit) {
		super(parentShell);
		if(emailToEdit!=null && !"".equals(emailToEdit.trim())) 
			this.emailToEdit = emailToEdit;
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		String addOrEdit = emailToEdit!=null?"Edit":"Add";
		shell.setText(addOrEdit+" Contact");
	}

	@Override
	protected Control createDialogArea(Composite parent) {

		Composite dialogArea = (Composite) super.createDialogArea(parent);
		((GridLayout)dialogArea.getLayout()).numColumns = 2;

		Label emailLabel = new Label(dialogArea, SWT.LEFT);
		emailLabel.setText("Email of new contact: ");

		emailField = new Text(dialogArea, SWT.SINGLE | SWT.BORDER);
		GridData emailFieldData = new GridData(GridData.FILL_HORIZONTAL);
		emailFieldData.minimumWidth = 200;
		emailField.setLayoutData(emailFieldData);

		// if editing, display email being edited
		if(emailToEdit!=null) emailField.setText(emailToEdit);

		return dialogArea;
	}

	@Override
	protected void okPressed() {

		String enteredEmail = (emailField.getText()==null)?"":emailField.getText().trim();
		if("".equals(enteredEmail)) {
			UIUtils
			.errorPromptDialog("Please enter an email for the contact")
			.open();
			return;
		}
		if(!enteredEmail.contains("@")) {
			UIUtils
			.errorPromptDialog("Bad email address", "Please enter a " +
			"valid email address in the form username@domain.com")
			.open();
			return;
		}

		try {
			// Don't add duplicate entry if recipient already in address book
			List<String> alreadyStoredContacts = Arrays.asList(AccountSettings.getStoredContacts());
			if(alreadyStoredContacts.contains(enteredEmail)) {
				UIUtils
				.errorPromptDialog("That email already exists in your address book")
				.open();
				return;
			}
			ContactUtils.addContactToAddressBook(enteredEmail);
		} catch (Exception e) {
			UIUtils
			.errorPromptDialog("Could not add contact")
			.open();

			contactEmail = null;
			super.okPressed();
			return;
		}

		contactEmail = enteredEmail;

		super.okPressed();
	}

	public String getEmail() {
		return contactEmail;
	}

}
