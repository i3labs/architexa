package com.architexa.diagrams.jdt.properties;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.architexa.intro.AtxaIntroPlugin;


public class AtxaBuildPathPropertiesPage	extends TabbedFieldEditorOverlayPage implements IWorkbenchPreferencePage {

	public AtxaBuildPathPropertiesPage() {
		super(GRID);
		setPreferenceStore(AtxaIntroPlugin.getDefault().getPreferenceStore());
	}
	
	@Override
	public void createFieldEditors() {       
		if(isPropertyPage()) {
			createContents(getFieldEditorParent());
        }     
	}

	/*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
	public void init(IWorkbench workbench) {
	}
	
}