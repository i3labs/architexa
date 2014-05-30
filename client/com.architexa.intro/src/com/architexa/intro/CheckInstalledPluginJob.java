package com.architexa.intro;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

import com.architexa.rse.AccountSettings;

public class CheckInstalledPluginJob extends Job {

	static final Logger logger = AtxaIntroPlugin.getLogger(CheckInstalledPluginJob.class);
	public CheckInstalledPluginJob() {
		super("Install Check");
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		List<String> allInstalledFeatures = InstalledPluginUtils.getFeatures(monitor);
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				// check for dontshow pref 
				if (AccountSettings.isInstallReminderUnChecked()) return;
				// Show the dialog always (to test)
				boolean debug = false;
				if (debug || (InstalledPluginUtils.isSubclipseInstalled() && !InstalledPluginUtils.isRSESubclipseInstalled()) 
						|| (InstalledPluginUtils.isSpringInstalled() && !InstalledPluginUtils.isRSESpringInstalled())) {
					// show dialog 
					// if (true) install
					UpgradeInstallDialog dlg;
					if (debug)
						dlg = new UpgradeInstallDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), true, true);
					else	
						dlg = new UpgradeInstallDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), InstalledPluginUtils.isSubclipseInstalled(), InstalledPluginUtils.isSpringInstalled());
					dlg.open();
				}
			}
		});
		return Status.OK_STATUS;
	}
	
}
