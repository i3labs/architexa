package com.architexa.collab.ui;

import org.apache.log4j.Logger;

import com.architexa.rse.AccountSettings;

public class ContactUtils {
	static final Logger logger = Activator.getLogger(ContactUtils.class);
	public static void removeContact(String contactEmail) {
		// Removing the contact by doing the following
		// String contactString = getPreferenceStore().getString(CONTACTS).trim();
		// contactString = contactString.replace(contactEmail, "");
		// will result in problems if our stored contact string is something like
		// "az@mailinator.com;z@mailinator.com;" and the contact we want to remove is
		// "z@mailinator.com" because the replace() call will match both of the contacts. 
		// Avoiding this problem by clearing the address book and adding all contacts 
		// back except the one whose email exactly matches the one we want to remove.
		
		
		clearSrvrContacts();
		String[] storedContacts = AccountSettings.getStoredContacts();
		AccountSettings.clearAddressBook();
		for(int i=0; i<storedContacts.length; i++) {
			if(contactEmail.equalsIgnoreCase(storedContacts[i])) continue;
			addContactToAddressBook(storedContacts[i]);
		}
	}
	
	public static void addContactToAddressBook(String contactEmail) {
		AccountSettings.addContactToAddressBook(contactEmail);
	}
	
	public static void clearSrvrContacts() {}
}
