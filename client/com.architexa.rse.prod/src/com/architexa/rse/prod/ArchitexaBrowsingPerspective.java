package com.architexa.rse.prod;

//import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
//import org.eclipse.ui.progress.IProgressConstants;

import com.architexa.inspector.InspectorView;

public class ArchitexaBrowsingPerspective implements IPerspectiveFactory {

	public ArchitexaBrowsingPerspective() {}

	public void createInitialLayout(IPageLayout layout) {
 		String editorArea = layout.getEditorArea();
		
		IFolderLayout folder = layout.createFolder("left", IPageLayout.LEFT, (float)0.25, editorArea);
		folder.addView(InspectorView.ID);
		//folder.addPlaceholder(IPageLayout.ID_RES_NAV);
		
		//IFolderLayout outputfolder = layout.createFolder("bottom", IPageLayout.BOTTOM, (float)0.75, editorArea);
		//outputfolder.addView(IPageLayout.ID_PROBLEM_VIEW);
		//outputfolder.addPlaceholder(IProgressConstants.PROGRESS_VIEW_ID);

		//IFolderLayout mainFolder = layout.createFolder("main", IPageLayout.TOP, (float)1.00, editorArea);
		//mainFolder.addView("com.architexa.diagrams.strata.ui.StrataView");
		//mainFolder.addView("com.architexa.diagrams.relo.ui.ReloView");
		//mainFolder.addView("com.architexa.diagrams.sequence.ui.SeqView");
		
//		layout.addActionSet(JavaUI.ID_ACTION_SET);
//		layout.addActionSet(JavaUI.ID_ELEMENT_CREATION_ACTION_SET);
//		layout.addActionSet(IPageLayout.ID_NAVIGATE_ACTION_SET);
		
		// views - java
		//layout.addShowViewShortcut(JavaUI.ID_PACKAGES);
		//layout.addShowViewShortcut(JavaUI.ID_TYPE_HIERARCHY);
		//layout.addShowViewShortcut(JavaUI.ID_SOURCE_VIEW);
		//layout.addShowViewShortcut(JavaUI.ID_JAVADOC_VIEW);

		// views - standard workbench
		//layout.addShowViewShortcut(IPageLayout.ID_OUTLINE);
		//layout.addShowViewShortcut(IPageLayout.ID_PROBLEM_VIEW);
		//layout.addShowViewShortcut(IPageLayout.ID_RES_NAV);
		//layout.addShowViewShortcut(IPageLayout.ID_TASK_LIST);
		//layout.addShowViewShortcut(IProgressConstants.PROGRESS_VIEW_ID);
				
		// new actions - Java project creation wizard
		//layout.addNewWizardShortcut("org.eclipse.jdt.ui.wizards.JavaProjectWizard"); //$NON-NLS-1$
		//layout.addNewWizardShortcut("org.eclipse.jdt.ui.wizards.NewPackageCreationWizard"); //$NON-NLS-1$
		//layout.addNewWizardShortcut("org.eclipse.jdt.ui.wizards.NewClassCreationWizard"); //$NON-NLS-1$
		//layout.addNewWizardShortcut("org.eclipse.jdt.ui.wizards.NewInterfaceCreationWizard"); //$NON-NLS-1$
		//layout.addNewWizardShortcut("org.eclipse.jdt.ui.wizards.NewEnumCreationWizard"); //$NON-NLS-1$
		//layout.addNewWizardShortcut("org.eclipse.jdt.ui.wizards.NewAnnotationCreationWizard"); //$NON-NLS-1$
		//layout.addNewWizardShortcut("org.eclipse.jdt.ui.wizards.NewSourceFolderCreationWizard");	 //$NON-NLS-1$
		//layout.addNewWizardShortcut("org.eclipse.jdt.ui.wizards.NewSnippetFileCreationWizard"); //$NON-NLS-1$
		//layout.addNewWizardShortcut("org.eclipse.jdt.ui.wizards.NewJavaWorkingSetWizard"); //$NON-NLS-1$
		//layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.folder");//$NON-NLS-1$
		//layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.file");//$NON-NLS-1$
		//layout.addNewWizardShortcut("org.eclipse.ui.editors.wizards.UntitledTextFileWizard");//$NON-NLS-1$
	}

}
