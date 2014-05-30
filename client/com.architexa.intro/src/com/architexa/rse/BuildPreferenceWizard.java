package com.architexa.rse;

import org.eclipse.jface.wizard.Wizard;

public class BuildPreferenceWizard extends Wizard{

	@Override
	public boolean performFinish() {
		try {
			// Set selection
			BuildSelectionWizardPage selectionPage = (BuildSelectionWizardPage) getPage(BuildSelectionWizardPage.PAGE_NAME);
			selectionPage.setPreferenceFilters();
			
			//set schedule
			BuildScheduleWizardPage schedulePage = (BuildScheduleWizardPage) getPage(BuildScheduleWizardPage.PAGE_NAME);
			schedulePage.setBuildSchedule();
			
			// run build now
			if (schedulePage.isBuildNowChecked()) {
				BuildStatus.runRunnables();
				// Build jobs waiting to run after the account 
				// validity test aren't run here because they're 
				// run by AccountStatus after that test.
			}
		} catch (Throwable t) { // Make sure even if there is an error we close the dialog
			return true;
		}
			
		return true;
	}

	@Override
	public boolean performCancel() { 
 		return true;
	}
	
	@Override
	public void addPages() {
		addPage(new BuildStartWizardPage());
		addPage(new BuildSelectionWizardPage());
		addPage(new BuildScheduleWizardPage());
	}
	
	
}
