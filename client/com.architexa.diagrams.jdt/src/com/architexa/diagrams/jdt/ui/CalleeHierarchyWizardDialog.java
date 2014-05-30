package com.architexa.diagrams.jdt.ui;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * The wizard dialog that guides a user through the incremental 
 * addition of the calls in a method's callee hierarchy (all the
 * calls the method makes, all the calls those methods make, etc)
 *
 */
public class CalleeHierarchyWizardDialog extends MessageDialog {

	private boolean addAllChkBx = false;

	public CalleeHierarchyWizardDialog(Shell parentShell, String dialogTitle, Image dialogTitleImage, String dialogMessage, int dialogImageType, String[] dialogButtonLabels, int defaultIndex) {
		super(parentShell, dialogTitle, dialogTitleImage, dialogMessage, dialogImageType, dialogButtonLabels, defaultIndex);
	}

	@Override
	public int getReturnCode() {
		if (addAllChkBx) return 3;
		return super.getReturnCode();
	}

	@Override
	protected Control createButtonBar(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(1).equalWidth(true).applyTo(composite);
		GridDataFactory.fillDefaults().indent(45, 0).align(SWT.BEGINNING, SWT.CENTER).span(2, 1).applyTo(composite);

		composite.setFont(parent.getFont());

		Button checkbox = new Button(composite, SWT.CHECK);
		checkbox.setText("Do not prompt again and add all remaining items.");
		checkbox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				addAllChkBx = !addAllChkBx;
				getButton(1).setEnabled(!getButton(1).getEnabled());
				getButton(2).setEnabled(!getButton(2).getEnabled());
			}
		});

		return super.createButtonBar(parent);
	}
}