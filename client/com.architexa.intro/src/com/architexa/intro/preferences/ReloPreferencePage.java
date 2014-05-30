package com.architexa.intro.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.architexa.intro.AtxaIntroPlugin;

/**
 * @author vineet
 *
 */
public class ReloPreferencePage	extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public ReloPreferencePage() {
		super(GRID);
		setPreferenceStore(AtxaIntroPlugin.getDefault().getPreferenceStore());
	}
	
	@Override
	public void createFieldEditors() {
		setDescription("Preferences for Architexa RSE integration");
		Label lbl = new Label(getFieldEditorParent(), SWT.LEFT | SWT.WRAP);
	    GridData lblData = new GridData();
	    lblData.horizontalSpan = 2;
	    lbl.setLayoutData(lblData);
	    lbl.setText("Global Settings");

		// TODO: add support for default detail level preference
//		addField(new BooleanFieldEditor(
//                PreferenceConstants.LabelItemsWithContextKey, 
//                PreferenceConstants.LabelItemsWithContextPrompt, 
//                getFieldEditorParent()){
//        	@Override
//			protected void doStore() {
//        		super.doStore();
//        	}
//        });
		
		
		 String curJavaVer = "your current version: " + System.getProperty("java.version");
	        addField(new BooleanFieldEditor(
	                PreferenceConstants.Java5CheckKey, 
	                PreferenceConstants.Java5CheckPrompt + " (" + curJavaVer + ")", 
	                getFieldEditorParent()));
	        
	        
	        // Do not add preference for showing update notification
	        // Update notification disabled due to lack of support of eclipse p2 update functionality
	        // TODO: add support for p2 updates
//	        addField(new BooleanFieldEditor(
//	                PreferenceConstants.ShowWhenUpdatesAreAvailableKey, 
//	                PreferenceConstants.ShowWhenUpdatesAreAvailablePrompt, 
//	                getFieldEditorParent()){
//	        	@Override
//				protected void doStore() {
//	        		super.doStore();
//	        		
//	        		boolean show = AtxaIntroPlugin.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.ShowWhenUpdatesAreAvailableKey);
//	        		IFeature installedAtxaFeature;
//					try {
//						installedAtxaFeature = CheckForUpdateJob.getInstalledArchitexaFeature();
//					} catch (CoreException e) {
//						UpdateToolbar.showNotification(false, false); 
//	        			return;
//					}
//	        		if(installedAtxaFeature==null) {
//	        			// do not care if we are extended feature or not since we are not showing the button here
//	        			UpdateToolbar.showNotification(false, false); 
//	        			return;
//	        		}
//	        		final boolean isExtendedVersion = installedAtxaFeature.getUpdateSiteEntry().getURL().getPath().equals(UpdateAction.atxaExtendedUpdateSite);
//	        		
//	        		UpdateToolbar.showNotification(show, isExtendedVersion);
//	        		if (!AtxaIntroPlugin.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.ShowWhenUpdatesAreAvailableKey))
//	        			UpdateToolbar.showNotification(false);
//	        	}
//	        });
	        
	        addField(new BooleanFieldEditor(
	                PreferenceConstants.BUILD_OFF_REMINDER_KEY, 
	                PreferenceConstants.BUILD_OFF_REMINDER_CONTEXT, 
	                getFieldEditorParent()){
	        	@Override
				protected void doStore() {
	        		super.doStore();
	        	}
	        });
	        
	        addField(new BooleanFieldEditor(
	                PreferenceConstants.SHOW_DLG_WHEN_OPENING_NON_JAVA_IN_STRATA, 
	                PreferenceConstants.SHOW_DLG_WHEN_OPENING_NON_JAVA_IN_STRATA_LABEL, 
	                getFieldEditorParent()){
	        	@Override
				protected void doStore() {
	        		super.doStore();
	        	}
	        });
		
	        
			//addField(new BooleanFieldEditor(
			//        PreferenceConstants.StretchStrataKey, 
			//        "Stretch Layered Diagram contents to fit", 
			//        getFieldEditorParent()));
	        addField(new BooleanFieldEditor(
	                PreferenceConstants.StrataSizeToContentsKey, 
	                "Layered Diagram size to contents", 
	                getFieldEditorParent()));
	        
	        
//			createNoteComposite(getFont(), getFieldEditorParent(), "More Options Available:", "For more preferences regarding Account, " +
//					"\nArchitexa Build, Collaboration, and Library Code; " +
//					"\nexpand the Architexa preference page subtree");
	        
	        
//        String curJavaVer = "your current version: " + System.getProperty("java.version");
//        addField(new BooleanFieldEditor(
//                PreferenceConstants.Java5CheckKey, 
//                PreferenceConstants.Java5CheckPrompt + " (" + curJavaVer + ")", 
//                getFieldEditorParent()));
//        
//		//addField(new BooleanFieldEditor(
//		//        PreferenceConstants.BuilderCheckConnectionAtStartupKey, 
//		//        PreferenceConstants.BuilderCheckConnectionAtStartupPrompt, 
//		//        getFieldEditorParent()));
//        
//        addField(new BooleanFieldEditor(
//                PreferenceConstants.ShowWhenUpdatesAreAvailableKey, 
//                PreferenceConstants.ShowWhenUpdatesAreAvailablePrompt, 
//                getFieldEditorParent()){
//        	@Override
//			protected void doStore() {
//        		super.doStore();
//        		
//        		boolean show = AtxaIntroPlugin.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.ShowWhenUpdatesAreAvailableKey);
//        		UpdateToolbar.showNotification(show);;
////        		if (!AtxaIntroPlugin.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.ShowWhenUpdatesAreAvailableKey))
////        			UpdateToolbar.showNotification(false);
//        	}
//        });
//        
//        
//        
//        addField(new BooleanFieldEditor(
//                PreferenceConstants.BUILD_OFF_REMINDER_KEY, 
//                PreferenceConstants.BUILD_OFF_REMINDER_CONTEXT, 
//                getFieldEditorParent()){
//        	@Override
//			protected void doStore() {
//        		super.doStore();
//        		
//        	}
//        });
        
	}

	/*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
	public void init(IWorkbench workbench) {
	}
	
}