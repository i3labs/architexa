package com.architexa.collab.ui.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.IEditorPart;

import com.architexa.collab.ui.Activator;
import com.architexa.collab.ui.ShareByEmailAction;
import com.architexa.diagrams.ui.ImageCache;
import com.architexa.diagrams.ui.RSEEditorViewCommon.IRSEEditorViewCommon;
import com.architexa.diagrams.ui.RSEShareableDiagramEditorInput;
import com.architexa.external.eclipse.IExportableEditorPart;
import com.architexa.rse.AccountSettings;
import com.architexa.rse.BuildStatus;

public class ShareDiagramDialog extends Dialog {

	ShareDialogTab selectedTab;
	private IRSEEditorViewCommon editor;
	private boolean isEmailDefaultTab = false;
	private boolean showPrivateGroups = true;
	

	public ShareDiagramDialog(Shell parentShell, IRSEEditorViewCommon activeEditor) {
		super(parentShell);
		this.editor = activeEditor;
	}
	
	public ShareDiagramDialog(Shell parentShell, IRSEEditorViewCommon activeEditor, boolean isEmailDef, boolean showPrivate) {
		super(parentShell);
		this.editor = activeEditor;
		isEmailDefaultTab = isEmailDef;
		showPrivateGroups = showPrivate;
		
		// Need to do exported image customizations before dialog is created for
		// them to take effect. The opening of the dialog causes a refresh to
		// occur, refreshing the diagram contents and applying the customizations.
		// If the call to do the customizations was done after the dialog was created, 
		// refresh would not be called until the dialog closed, which would be after
		// the share action completed and the image was already uploaded or emailed.
		if (activeEditor instanceof IExportableEditorPart) 
			((IExportableEditorPart) activeEditor).addRemoveImageExportCustomizations(true);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Share Diagram");
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite modifiedParent;
		modifiedParent = parent;
		
		final TabFolder tabFolder = new TabFolder(modifiedParent, SWT.TOP);

		// Share by email
		TabItem shareByEmailItem = new TabItem(tabFolder, SWT.NONE);
		shareByEmailItem.setText(ShareByEmailAction.shareByEmailText);
		shareByEmailItem.setImage(ImageCache.calcImageFromDescriptor(Activator.getImageDescriptor(ShareByEmailAction.shareByEmailIconPath)));
		ShareByEmailTab shareByEmailContents = new ShareByEmailTab(tabFolder, SWT.NONE);
		shareByEmailContents.createContent();
		shareByEmailItem.setControl(shareByEmailContents);

		// Listen to which tab is selected so when 'Share' button 
		// is pressed we know whether to upload or email.
		tabFolder.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				selectedTab = (ShareDialogTab) tabFolder.getSelection()[0].getControl();
			}
		});
		selectedTab = shareByEmailContents; // sharing by e-mail

		tabFolder.pack();

		//TODO NEED TO REMOVE
		Label emptyLabel = new Label(parent, SWT.NONE);
		emptyLabel.setText(" ");
//		createShowDefaultMenuButton(parent);
		if (isEmailDefaultTab)
			tabFolder.setSelection(1);
		return tabFolder;
	}

	
//	private void createShowDefaultMenuButton(Composite parent) {
//		Button showDefMenu = new Button(parent, SWT.PUSH);
//		showDefMenu.setText("Show Options...");
//		
//		MouseListener listener = new MouseListener() {
//			public void mouseUp(MouseEvent e) {}
//			public void mouseDown(MouseEvent e) {
//				cancelPressed();
//				new DefaultSaveDialog(getParentShell(), editor).open();
//			}
//			
//			public void mouseDoubleClick(MouseEvent e) {}
//		};
//		
//		showDefMenu.addMouseListener(listener);
//	}
	
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		/*Button shareBtn =*/ createButton(parent, IDialogConstants.OK_ID, "Share", true);
		/*Button cancelBtn =*/ createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
//		GridData gd = new GridData(SWT.END, SWT.FILL, true, false);
////		gd.horizontalSpan = 2;
//		
//		shareBtn.setLayoutData(gd);
//		cancelBtn.setLayoutData(gd);
	}

	@Override
	protected void okPressed() {
		boolean readyToShare = selectedTab.requiredInfoEntered();
		if(readyToShare) {
			BuildStatus.addUsage("OnlineSave");
			editor.clearDirtyFlag();
			selectedTab.share();
			if (selectedTab.isDiagramDiff())
				AccountSettings.setUploadDiff(selectedTab.uploadDiff());
//			selectedTab.isUploadedToCommunityServer();
			setDiagramSaved(true);
			super.okPressed();
		}
	}

	private void setDiagramSaved(boolean isSavedFile) {
		if (editor instanceof IEditorPart)
			((RSEShareableDiagramEditorInput) ((IEditorPart) editor).getEditorInput()).setSavedFile(isSavedFile);
	}

	public void cancel() {
		cancelPressed();
	}

	public void ok() {
		okPressed();
	}

}
