package com.architexa.intro;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.progress.UIJob;

import com.architexa.intro.preferences.PreferenceConstants;

/**
 * @author vineet
 *
 */
public class Java5Check implements IStartup {
	static final Logger logger = AtxaIntroPlugin.getLogger(Java5Check.class);
	
	public void earlyStartup() {
		try {
			earlyStartupInternal();
		} catch (Throwable t) {
			logger.error("Unexpected Error", t);
		}
	}
	private void earlyStartupInternal() {
		final IPreferenceStore prefStore = AtxaIntroPlugin.getDefault().getPreferenceStore();

		if (!prefStore.getBoolean(PreferenceConstants.Java5CheckKey)) return;
		
		if (System.getProperty("java.version").compareTo("1.5.0") > 0) return;
		
		Job job = new UIJob("Java VM Checker") {
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				MessageDialogWithToggle dialog = new MessageDialogWithToggle(
						null, // ignore shell
						"Architexa RSE Java 5+ Check", 
						null, // accept the default window icon
						"The Eclipse Workbench is running using a VM lower than Java 5.\n" + 
							"Architexa RSE needs Java 5 SE or higher to run.", 
						MessageDialog.ERROR, 
						new String[] { IDialogConstants.OK_LABEL }, 0, // ok is the default button
                        PreferenceConstants.Java5CheckPrompt, 
                        prefStore.getBoolean(PreferenceConstants.Java5CheckKey)
		                ) {
					@Override
					protected void buttonPressed(int buttonId) {
						super.buttonPressed(buttonId);
						prefStore.setValue(PreferenceConstants.Java5CheckKey, getToggleState());
					}
				};
				dialog.open();
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

}
