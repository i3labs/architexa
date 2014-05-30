package com.architexa.rse;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class BuildStartWizardPage extends WizardPage{

	public static final String PAGE_NAME = "Build Start";
	protected BuildStartWizardPage() {
		super(PAGE_NAME, "Setting Index Preference", null);
		// TODO Auto-generated constructor stub
	}

	public void createControl(Composite parent) {
		Composite topLevel = new Composite(parent, SWT.NONE);
	    topLevel.setLayout(new GridLayout());

	    Label textLabel = new Label(topLevel, SWT.LEFT);
	    textLabel.setText("Your account has been validated.\n\n" +
	    		"This wizard will help you setup your Architexa Index preferences. " + 
				"\nArchitexa indexes your code to be able to show diagrams quickly." +
				"\n\nTo access these settings later go to:\nEclipse Preferences->Architexa->Architexa Build." +
				"\n\nClick 'Next' now to setup your preferences.");
	   
//	    checkTable = new Table(topLevel, SWT.CHECK);
//	    checkTable.setBackground(topLevel.getBackground());
//	    item = new TableItem(checkTable, SWT.NONE);
//	    item.setText("Index the selected projects now.");
//	    item.setChecked(true);
//	    
	    setControl(topLevel);
	    setPageComplete(true);
	}

}
