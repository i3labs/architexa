package com.architexa.collab.ui;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;

import com.architexa.collab.ui.dialogs.ShareDiagramDialog;
import com.architexa.diagrams.editors.RSEMultiPageEditor;
import com.architexa.external.eclipse.IExportableEditorPart;
import com.architexa.rse.BuildStatus;

public class OpenShareDialog extends Action {

	public OpenShareDialog() {
		super("Share Diagram...", Activator.getImageDescriptor("icons/share-icon-16x16.png"));
	}

	@Override
	public void run() {
		BuildStatus.addUsage("Open Dialog to Share Diagram");
		openDialog();
	}

	private void openDialog() {
		// Need to do exported image customizations before dialog is created for
		// them to take effect. The opening of the dialog causes a refresh to
		// occur, refreshing the diagram contents and applying the customizations.
		// If the call to do the customizations was done after the dialog was created, 
		// refresh would not be called until the dialog closed, which would be after
		// the share action completed and the image was already uploaded or emailed.
		IEditorPart activeEditor = 
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		if (activeEditor instanceof IExportableEditorPart) 
			((IExportableEditorPart) activeEditor).addRemoveImageExportCustomizations(true);

		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		ShareDiagramDialog shareDialog = new ShareDiagramDialog(shell, (RSEMultiPageEditor) activeEditor);
		shareDialog.open();

		if (activeEditor instanceof IExportableEditorPart) 
			((IExportableEditorPart) activeEditor).addRemoveImageExportCustomizations(false);
	}
}
