package com.architexa.collab.ui;

import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

import com.architexa.collab.ui.dialogs.AddOrEditContactDialog;
import com.architexa.collab.ui.dialogs.ChangeSMTPSettingsDialog;
import com.architexa.diagrams.ui.ImageCache;
import com.architexa.rse.AccountSettings;


/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public class CollabPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	static final Logger logger = Activator.getLogger(CollabPreferencePage.class);
	
//	private static final String activationPrompt = "Need an Account? Register Online";
//	private static final String url = "http://my.architexa.com/users/signup";
	
	private String originalStoredEmail = AccountSettings.getStoredAccountEmail();
	private String originalStoredPassword = AccountSettings.getStoredAccountPassword();

	TabFolder tabFolder;
	TabItem groupsItem;
	TabItem smtpItem;

	private Text smtpServerText;
	private Text smtpPortText;
	private Text smtpAccountEmailText;
	private Text smtpAccountPasswordText;

	public CollabPreferencePage() {
		super();
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}

	public TabItem selectSMTPPreferenceTab() {
		tabFolder.setSelection(smtpItem);
		return smtpItem;
	}

	@Override
	protected Control createContents(Composite parent) {

		tabFolder = new TabFolder(parent, SWT.TOP);

		TabItem addressBookItem = new TabItem(tabFolder, SWT.NONE);
		addressBookItem.setText("Address Book");
		addressBookItem.setImage(ImageCache.calcImageFromDescriptor(Activator.getImageDescriptor("icons/x-office-address-book.png")));
		Control addressBookContents = createAddressBookTabContents(tabFolder);
		addressBookItem.setControl(addressBookContents);

		smtpItem = new TabItem(tabFolder, SWT.NONE);
		smtpItem.setText("SMTP");
		smtpItem.setImage(ImageCache.calcImageFromDescriptor(Activator.getImageDescriptor(ShareByEmailAction.shareByEmailIconPath)));
		Control smtpServerContents = createSmtpServerTabContents(tabFolder);
		smtpItem.setControl(smtpServerContents);

		tabFolder.pack();
		return tabFolder;
	}

	@Override
	public boolean performCancel() {
		AccountSettings.setStoredAccount(originalStoredEmail, originalStoredPassword);
		return super.performCancel();
	}

	@Override
	// Resets default values for settings ONLY on selected tab
	protected void performDefaults() {

		if(tabFolder.getSelection().length==0) {
			logger.error("Unable to restore defaults on collab " +
			"preference page because no tab selected.");
			return;
		}

		TabItem selectedTab = tabFolder.getSelection()[0];
		if (groupsItem.equals(selectedTab)) {
			// Reset to default account
			AccountSettings.restoreDefaultStoredAccount();
			ContactUtils.clearSrvrContacts();
			AccountSettings.clearAddressBook();
		} else if (smtpItem.equals(selectedTab)) {
			// Reset default smtp settings
			AccountSettings.restoreDefaultSMTPServerSettings();
			smtpServerText.setText(AccountSettings.getDefaultStoredSMTPServer());
			smtpPortText.setText(AccountSettings.getDefaultStoredSMTPPort());
			smtpAccountEmailText.setText(AccountSettings.getDefaultStoredSMTPUsername());
			smtpAccountPasswordText.setText(AccountSettings.getDefaultStoredSMTPPassword());
		}

		super.performDefaults();
	}

	public void init(IWorkbench workbench) {}

	private Control createAddressBookTabContents(TabFolder tabFolder) {
		Composite container = new Composite(tabFolder, SWT.NONE);
		GridLayout containerLayout = new GridLayout(2, false);
		containerLayout.marginWidth = 10;
		container.setLayout(containerLayout);

		final org.eclipse.swt.widgets.List emailList = 
			new org.eclipse.swt.widgets.List(container, 
					SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
		GridData emailListData = new GridData(SWT.FILL, SWT.FILL, false, true);
		emailListData.widthHint = 250;
		emailList.setLayoutData(emailListData);

		// make a copy so stored isn't modified and we can trim extra spaces
		TreeSet<String> alphabeticalContacts = new TreeSet<String>(CollabUIUtils.getAlphabeticalIgnoringCaseComparator());
		for(String contact : AccountSettings.getStoredContacts()) {
			String trimmedContact = contact.trim();
			// ignore empty contact
			if(!"".equals(trimmedContact)) alphabeticalContacts.add(trimmedContact);
		}

		for(String contact : alphabeticalContacts) {
			emailList.add(contact.trim());
		}

		Composite buttonColumn = new Composite(container, SWT.NONE);
		GridLayout buttonColumnLayout = new GridLayout(1, true);
		buttonColumn.setLayout(buttonColumnLayout);
		buttonColumn.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Button addContactButton = new Button(buttonColumn, SWT.PUSH);
		addContactButton.setText("Add Contact");
		addContactButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		addContactButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				addContact(emailList, null);
			}
		});

		final Button editContactButton = new Button(buttonColumn, SWT.PUSH);
		editContactButton.setText("Edit Contact");
		editContactButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		editContactButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// "editing" by adding the changed email and removing
				// the original email from the address book
				String selectedContact = emailList.getSelection()[0];
				if(addContact(emailList, selectedContact))
					removeContact(emailList, selectedContact);
			}
		});
		// A single contact must be selected for Edit Contact button to be enabled 
		editContactButton.setEnabled(false);
		emailList.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				editContactButton.setEnabled(emailList.getSelectionCount()==1);
			}
		});

		final Button removeContactButton = new Button(buttonColumn, SWT.PUSH);
		removeContactButton.setText("Remove Contact");
		removeContactButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		removeContactButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				for(String selectedContact : emailList.getSelection()) {
					removeContact(emailList, selectedContact);
				}
			}
		});
		// A contact must be selected for Remove Contact button to be enabled 
		removeContactButton.setEnabled(false);
		emailList.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				removeContactButton.setEnabled(true);
			}
		});

		return container;
	}

	/*
	 * 
	 * @param emailList the displayed list of email addresses in the address book
	 * @param emailToEdit the email address of an existing contact that the user has
	 * selected to edit via the Edit Contact button or null if he has pressed the
	 * Add Contact button to add an entirely new contact
	 * @return true if contact added to address book successfully, false otherwise
	 */
	private boolean addContact(org.eclipse.swt.widgets.List emailList, String emailToEdit) {
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		AddOrEditContactDialog addContactDialog = new AddOrEditContactDialog(shell, emailToEdit);
		addContactDialog.open();

		String newContactEmail = addContactDialog.getEmail();
		if(newContactEmail==null) return false;

		// Successfully added contact, so add it to
		// pref page list in correct alphabetical order
		int indexToInsert = getAlphabeticalIndex(newContactEmail, emailList);
		emailList.add(newContactEmail, indexToInsert);
		return true;
	}

	private int getAlphabeticalIndex(String emailToInsert, 
			org.eclipse.swt.widgets.List emailList) {
		int index = 0;
		while(index<emailList.getItemCount()) {
			if(emailToInsert.compareToIgnoreCase(emailList.getItem(index)) <= 0)
				return index;
			index++;
		}
		return index;
	}
	
	private void removeContact(org.eclipse.swt.widgets.List emailList, String selectedContact) {
		ContactUtils.removeContact(selectedContact); // remove from store
		emailList.remove(selectedContact); // remove from pref page list
	}

	private Control createSmtpServerTabContents(TabFolder tabFolder) {

		Composite fullTabArea = new Composite(tabFolder, SWT.NONE);
		GridLayout fullTabAreaLayout = new GridLayout(2, false);
		fullTabArea.setLayout(fullTabAreaLayout);

		Composite container = new Composite(fullTabArea, SWT.NONE);
		GridLayout containerLayout = new GridLayout();
		containerLayout.numColumns = 2;
		container.setLayout(containerLayout);

		Label serverLabel = new Label(container, SWT.LEFT);
		serverLabel.setText("Server: ");

		smtpServerText = new Text(container, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
		GridData serverData = new GridData(GridData.FILL_HORIZONTAL);
		serverData.minimumWidth = 200;
		smtpServerText.setLayoutData(serverData);

		Label portLabel = new Label(container, SWT.LEFT);
		portLabel.setText("Port: ");

		smtpPortText = new Text(container, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
		GridData portTextData = new GridData(GridData.FILL_HORIZONTAL);
		smtpPortText.setLayoutData(portTextData);

		Label smtpAccountEmailLabel = new Label(container, SWT.LEFT);
		smtpAccountEmailLabel.setText("Username: ");

		smtpAccountEmailText = new Text(container, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
		GridData smtpAccountEmailData = new GridData(GridData.FILL_HORIZONTAL);
		smtpAccountEmailData.minimumWidth = 200;
		smtpAccountEmailText.setLayoutData(smtpAccountEmailData);

		// Password
		Label passwordLabel = new Label(container, SWT.LEFT);
		passwordLabel.setText("Password: ");

		smtpAccountPasswordText = new Text(container, SWT.SINGLE | SWT.READ_ONLY | SWT.PASSWORD | SWT.BORDER);
		GridData smtpAccountPasswordData = new GridData(GridData.FILL_HORIZONTAL);
		smtpAccountPasswordData.minimumWidth = 200;
		smtpAccountPasswordText.setLayoutData(smtpAccountPasswordData);

		// Set the text in the Server, Port, and Account fields based on stored preferences
		smtpServerText.setText(AccountSettings.getStoredSMTPServer());
		smtpPortText.setText(AccountSettings.getStoredSMTPPort());
		smtpAccountEmailText.setText(AccountSettings.getStoredSMTPUsername());
		smtpAccountPasswordText.setText(AccountSettings.getStoredSMTPPassword());

		Button changeSMTPButton = new Button(container, SWT.PUSH);
		changeSMTPButton.setText("Edit SMTP Settings");
		GridData changeSMTPData = new GridData();
		changeSMTPData.horizontalSpan = 2;
		changeSMTPButton.setLayoutData(changeSMTPData);
		changeSMTPButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				changeSMTP();
			}
		});

		return fullTabArea;
	}

	private void changeSMTP() {

		// Prompt the user to enter the new SMTP settings
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		ChangeSMTPSettingsDialog changeSMTPDialog = new ChangeSMTPSettingsDialog(shell);
		changeSMTPDialog.open();

		String server = changeSMTPDialog.getServer();
		String port = changeSMTPDialog.getPort();
		if(server==null || port==null) return; // changing settings unsuccessful and/or cancelled

		String email = changeSMTPDialog.getUsername();
		String pw = changeSMTPDialog.getPassword();

		// Store the new settings
		AccountSettings.setStoredSMTPServerSettings(server, port, 
				email, pw);

		// Display the new settings on the pref page
		smtpServerText.setText(server);
		smtpPortText.setText(port);
		smtpAccountEmailText.setText(email);
		smtpAccountPasswordText.setText(pw);
	}

}
