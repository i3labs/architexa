package com.architexa.debug;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

import com.architexa.intro.AtxaIntroPlugin;

public class GenerateUniqueSystemId implements IWorkbenchWindowActionDelegate {

	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	public void init(IWorkbenchWindow window) {
		// TODO Auto-generated method stub
		
	}

	public void run(IAction action) {		
		MessageDialog dialog = new MessageDialog(
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), 
				"Architexa: Unique Id",
				null, 
				"System Key: " + AtxaIntroPlugin.getUniqueKey(), 
				MessageDialog.INFORMATION, 
				new String[]{"OK"}, 
				0) {
			@Override
			protected Control createMessageArea(Composite composite) {

				// create image
				Image image = getImage();
				imageLabel = new Label(composite, SWT.NULL);
				image.setBackground(imageLabel.getBackground());
				imageLabel.setImage(image);
				GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.BEGINNING)
				.applyTo(imageLabel);

				// create message with selectable system key
				Composite msgArea = new Composite(composite, SWT.NONE);
				msgArea.setLayout(new GridLayout());

				Text selectableMessageLabel = new Text(msgArea, SWT.MULTI | SWT.READ_ONLY);
				selectableMessageLabel.setText(message);

				return composite;
			}};
			dialog.open();
	}

	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub
		
	}

}
