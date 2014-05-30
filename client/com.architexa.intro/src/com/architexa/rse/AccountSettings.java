package com.architexa.rse;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.jface.preference.IPreferenceStore;

import com.architexa.intro.AtxaIntroPlugin;
import com.architexa.intro.preferences.PreferenceConstants;

public class AccountSettings {

    static final Logger logger = AtxaIntroPlugin.getLogger(AccountSettings.class);
	// experimental flag
	public static final boolean EXPERIMENTAL_MODE = false;

	//The identifiers for the preferences	

	private static final String STORED_EMAIL_PREFERENCE = "storedEmail";
	private static final String STORED_PASSWORD_PREFERENCE = "storedPassword";
	private static final String STORED_EMAIL_CONFIG = "configEmail";
	private static final String STORED_PASSWORD_CONFIG = "configPassword";
	private static final String STORED_GENERATED_PASSWORD = "storedGeneratedPassword";
	private static final String STORED_TEMPORARY_VALIDATION_CONFIG = "configTempValidation";
	private static final String UNIQUE_SYSTEM_KEY = "systemKey";

	private static final String STORED_SMTP_SERVER_PREFERENCE = "storedSMTPServer";
	private static final String STORED_SMTP_PORT_PREFERENCE = "storedSMTPPort";
	private static final String STORED_SMTP_USERNAME_PREFERENCE = "storedSMTPUsername";
	private static final String STORED_SMTP_PASSWORD_PREFERENCE = "storedSMTPPassword";

	private static final String CONTACTS = "contacts";
	private static final String EXTRA_REMOTE_HOSTS = "extraRemoteHosts";
	
	private static final String COLLAB_SYNC = "collabSync";
	private static final String USAGE_SYNC = "usageSync";
	private static final String UPLOAD_DIFF = "uploadDiff";
	private static final String INSTALL_REMINDER = "subclipseReminder";
	
	//The default values for the preferences

	private static final String DEFAULT_STORED_EMAIL = "";
	private static final String DEFAULT_STORED_PASSWORD = "";

	private static final String DEFAULT_STORED_SMTP_SERVER = "smtp.gmail.com";
	private static final String DEFAULT_STORED_SMTP_PORT = "465";
	private static final String DEFAULT_STORED_SMTP_USERNAME = "web@architexa.com";
	private static final String DEFAULT_STORED_SMTP_PASSWORD = "architexaBew";

	private static final String DEFAULT_CONTACTS = "";
	private static final String DEFAULT_EXTRA_REMOTE_HOSTS = "";
	
	private static final boolean DEFAULT_COLLAB_SYNC = true;
	private static final boolean DEFAULT_USAGE_SYNC = true;
	private static final boolean DEFAULT_UPLOAD_DIFF = false;
	private static final boolean DEFAULT_INSTALL_REMINDER = false;
	
	private static IPreferenceStore getPreferenceStore() {
		return AtxaIntroPlugin.getDefault().getPreferenceStore();
	}

	public static void initializeDefaultPreferences() {
		IPreferenceStore store = getPreferenceStore();
		store.setDefault(STORED_EMAIL_PREFERENCE, DEFAULT_STORED_EMAIL);
		store.setDefault(STORED_PASSWORD_PREFERENCE, DEFAULT_STORED_PASSWORD);

		store.setDefault(STORED_SMTP_SERVER_PREFERENCE, DEFAULT_STORED_SMTP_SERVER);
		store.setDefault(STORED_SMTP_PORT_PREFERENCE, DEFAULT_STORED_SMTP_PORT);
		store.setDefault(STORED_SMTP_USERNAME_PREFERENCE, DEFAULT_STORED_SMTP_USERNAME);
		store.setDefault(STORED_SMTP_PASSWORD_PREFERENCE, DEFAULT_STORED_SMTP_PASSWORD);

		store.setDefault(CONTACTS, DEFAULT_CONTACTS);
		store.setDefault(EXTRA_REMOTE_HOSTS, DEFAULT_EXTRA_REMOTE_HOSTS);
		
		store.setDefault(COLLAB_SYNC, DEFAULT_COLLAB_SYNC);
		store.setDefault(USAGE_SYNC, DEFAULT_USAGE_SYNC);
		store.setDefault(UPLOAD_DIFF, DEFAULT_UPLOAD_DIFF);
		store.setDefault(INSTALL_REMINDER, DEFAULT_INSTALL_REMINDER);

		getConfigStore().setDefault(STORED_TEMPORARY_VALIDATION_CONFIG, false);
	}

	public static void setStoredAccount(String email, String password) {
		getPreferenceStore().setValue(STORED_EMAIL_PREFERENCE, email);
		getPreferenceStore().setValue(STORED_PASSWORD_PREFERENCE, password);
		getConfigStore().setValue(STORED_EMAIL_CONFIG, email);
		getConfigStore().setValue(STORED_PASSWORD_CONFIG, password);
		try {
			getConfigStore().save();
		} catch (IOException e) {
			logger.error("Error while saving Account Configuration", e);
		}
	}
	
	public static void setStoredEmail(String email) {
		getPreferenceStore().setValue(STORED_EMAIL_PREFERENCE, email);
		getConfigStore().setValue(STORED_EMAIL_CONFIG, email);
		try {
			getConfigStore().save();
		} catch (IOException e) {
			logger.error("Error while saving Account Configuration", e);
		}
	}

	public static String getStoredAccountEmail() {
		return getPreferenceStore().getString(STORED_EMAIL_PREFERENCE);
	}

	public static String getStoredAccountPassword() {
		return getPreferenceStore().getString(STORED_PASSWORD_PREFERENCE);
	}
	
	public static String getConfigAccountEmail() {
		return getConfigStore().getString(STORED_EMAIL_CONFIG);
	}

	public static String getConfigAccountPassword() {
		return getConfigStore().getString(STORED_PASSWORD_CONFIG);
	}
	
	public static String getConfigAccountGeneratedPassword() {
		return getConfigStore().getString(STORED_GENERATED_PASSWORD);
	}
	
	public static void setConfigAccountGeneratedPassword(String password) {
		try {		
			getConfigStore().setValue(STORED_GENERATED_PASSWORD, password);
			getConfigStore().save();
		} catch (IOException e) {
			logger.error("Could not save offline password. " + e.getMessage());
		}
	}
	
	public static String getConfigSystemKey() {
		return getConfigStore().getString(UNIQUE_SYSTEM_KEY);
	}
	
	public static void setConfigSystemKey(String key) {
		try {		
			getConfigStore().setValue(UNIQUE_SYSTEM_KEY, key);
			getConfigStore().save();
		} catch (IOException e) {
			logger.error("Could not save system key. " + e.getMessage());
		}
	}
	
	private static IPersistentPreferenceStore getConfigStore() {
		return AtxaIntroPlugin.getDefault().getConfigStore();
	}

	public static void restoreDefaultStoredAccount() {
		getPreferenceStore().setValue(STORED_EMAIL_PREFERENCE, getDefaultStoredAccountEmail());
		getPreferenceStore().setValue(STORED_PASSWORD_PREFERENCE, getDefaultStoredAccountPassword());
		clearAddressBook();
	}

	public static String getDefaultStoredAccountEmail() {
		return getPreferenceStore().getDefaultString(STORED_EMAIL_PREFERENCE);
	}

	public static String getDefaultStoredAccountPassword() {
		return getPreferenceStore().getDefaultString(STORED_PASSWORD_PREFERENCE);
	}

	public static void clearAddressBook() {
		getPreferenceStore().setValue(CONTACTS, DEFAULT_CONTACTS);
	}

	public static void setStoredSMTPServerSettings(String server, String port, 
			String username, String pw) {
		getPreferenceStore().setValue(STORED_SMTP_SERVER_PREFERENCE, server);
		getPreferenceStore().setValue(STORED_SMTP_PORT_PREFERENCE, port);
		getPreferenceStore().setValue(STORED_SMTP_USERNAME_PREFERENCE, username);
		getPreferenceStore().setValue(STORED_SMTP_PASSWORD_PREFERENCE, pw);
	}

	public static String getStoredSMTPServer() {
		return getPreferenceStore().getString(STORED_SMTP_SERVER_PREFERENCE);
	}

	public static String getStoredSMTPPort() {
		return getPreferenceStore().getString(STORED_SMTP_PORT_PREFERENCE);
	}

	public static String getStoredSMTPUsername() {
		return getPreferenceStore().getString(STORED_SMTP_USERNAME_PREFERENCE);
	}

	public static String getStoredSMTPPassword() {
		return getPreferenceStore().getString(STORED_SMTP_PASSWORD_PREFERENCE);
	}

	public static void restoreDefaultSMTPServerSettings() {
		getPreferenceStore().setValue(STORED_SMTP_SERVER_PREFERENCE, getDefaultStoredSMTPServer());
		getPreferenceStore().setValue(STORED_SMTP_PORT_PREFERENCE, getDefaultStoredSMTPPort());
		getPreferenceStore().setValue(STORED_SMTP_USERNAME_PREFERENCE, getDefaultStoredSMTPUsername());
		getPreferenceStore().setValue(STORED_SMTP_PASSWORD_PREFERENCE, getDefaultStoredSMTPPassword());
	}

	public static String getDefaultStoredSMTPServer() {
		return getPreferenceStore().getDefaultString(STORED_SMTP_SERVER_PREFERENCE);
	}

	public static String getDefaultStoredSMTPPort() {
		return getPreferenceStore().getDefaultString(STORED_SMTP_PORT_PREFERENCE);
	}

	public static String getDefaultStoredSMTPUsername() {
		return getPreferenceStore().getDefaultString(STORED_SMTP_USERNAME_PREFERENCE);
	}

	public static String getDefaultStoredSMTPPassword() {
		return getPreferenceStore().getDefaultString(STORED_SMTP_PASSWORD_PREFERENCE);
	}
	
	private static String[] getListOfStrings(String key) {
		return getPreferenceStore().getString(key).split(";");
	}
	private static boolean isListOfStringsEmpty(String key) {
		return "".equals(getPreferenceStore().getString(key).trim());
	}
	private static void addToListOfStrings(String key, String value) {
		List<String> alreadyStoredValues = Arrays.asList(AccountSettings.getListOfStrings(key));
		if (alreadyStoredValues.contains(value)) return;
		String valuesString = getPreferenceStore().getString(key);
		valuesString = valuesString.concat(value.trim()+";");
		getPreferenceStore().setValue(key, valuesString);
	}

	/**
	 * 
	 * @return an array containing the email addresses of the user's contacts
	 */
	public static String[] getStoredContacts() {
		return getListOfStrings(CONTACTS);
	}

	/**
	 * 
	 * @return true if the address book contains no contacts, false otherwise
	 */
	public static boolean isAddressBookEmpty() {
		return isListOfStringsEmpty(CONTACTS);
	}

	public static void addContactToAddressBook(String contactEmail) {
		addToListOfStrings(CONTACTS, contactEmail);
	}
	
	public static String[] getStoredExtraRemoteHosts() {
		return getListOfStrings(EXTRA_REMOTE_HOSTS);
	}

	public static void addRemoteHostURLForStorage(String remoteHostURL) {
		addToListOfStrings(EXTRA_REMOTE_HOSTS, remoteHostURL);
	}
	

	/**
	 * whether the client should download shared diagrams from the server 
	 */
	public static boolean isCollabSync() {
		if (!getPreferenceStore().contains(COLLAB_SYNC))
			return true;
		return getPreferenceStore().getBoolean(COLLAB_SYNC);
	}
	
	public static void setCollabSync(boolean val) {
		getPreferenceStore().setValue(COLLAB_SYNC, val);
	}
	
	public static boolean isUsageSync() {
		if (!getPreferenceStore().contains(USAGE_SYNC))
			return true;
		return getPreferenceStore().getBoolean(USAGE_SYNC);
	}
	
	public static void setUsageSync(boolean val) {
	}
	
	public static boolean getTemporaryValidation() {
		return getConfigStore().getBoolean(STORED_TEMPORARY_VALIDATION_CONFIG);
	}
	public static void setTemporaryValidation(boolean val) {
		getPreferenceStore().setValue(STORED_TEMPORARY_VALIDATION_CONFIG, val);
	}

	public static void setUploadDiff(boolean uploadDiff) {
		getPreferenceStore().setValue(UPLOAD_DIFF, uploadDiff);
	}
	
	public static boolean isUploadDiff() {
		if (!getPreferenceStore().contains(UPLOAD_DIFF))
			return false;
		return getPreferenceStore().getBoolean(UPLOAD_DIFF);
	}
	
	public static void setSubclipseReminder(boolean reminder) {
		getPreferenceStore().setValue(INSTALL_REMINDER, reminder);
	}
	
	public static boolean isInstallReminderUnChecked() {
		if (!getPreferenceStore().contains(INSTALL_REMINDER))
			return false;
		return getPreferenceStore().getBoolean(INSTALL_REMINDER);
	}

	public static String getStoredGroupPreference() {
		return getPreferenceStore().getString(PreferenceConstants.STORED_GROUP_PREFERENCE_KEY);
	}
	public static String getWorkspaceBasePathPreference() {
		return getPreferenceStore().getString(PreferenceConstants.WORKSPACE_BASE_PATH_PREFERENCE_KEY);
	}
}
