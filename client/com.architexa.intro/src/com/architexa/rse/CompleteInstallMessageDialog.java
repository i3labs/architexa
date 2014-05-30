package com.architexa.rse;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class CompleteInstallMessageDialog extends MessageDialog{

	private Button index;
	public CompleteInstallMessageDialog(Shell parentShell, String dialogTitle, Image dialogTitleImage, String dialogMessage, int dialogImageType,String[] dialogButtonLabels, int defaultIndex) {
		super(parentShell, dialogTitle, dialogTitleImage, dialogMessage, dialogImageType, dialogButtonLabels, defaultIndex);
	}
	@Override
	protected Control createCustomArea(Composite parent) {
        Composite c = new Composite(parent, SWT.RIGHT);
        GridLayout layout = new GridLayout(1, true);
        layout.marginLeft = 40;
        c.setLayout(layout);

        index = new Button(c, SWT.RIGHT|SWT.CHECK);
        index.setText("Index Now");
        index.setSelection(true);
		return c;
    }
	@Override
	protected Control createDialogArea(Composite parent) {
	   Control composite = super.createDialogArea(parent);
	   GridLayout layout = new GridLayout();
       layout.marginHeight = 0;
       layout.marginWidth = 0;
       ((Composite) composite).setLayout(layout);
	   GridData data = new GridData(GridData.FILL_BOTH);
       data.horizontalSpan =2;
       data.widthHint = 50;
       composite.setLayoutData(data);
       return composite;
    }
	@Override
	public int getReturnCode() {
		if (isChecked()) return 1; 
		return 0;
	}
	public boolean isChecked() {
		return index.getSelection();
	}
}
