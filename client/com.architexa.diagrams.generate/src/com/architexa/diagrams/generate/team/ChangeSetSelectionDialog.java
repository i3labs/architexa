package com.architexa.diagrams.generate.team;

import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.dialogs.CheckedTreeSelectionDialog;

public class ChangeSetSelectionDialog extends CheckedTreeSelectionDialog{

	public ChangeSetSelectionDialog(Shell parent, ILabelProvider labelProvider,
			ITreeContentProvider contentProvider) {
		super(parent, labelProvider, contentProvider);
	}
	
	@Override
	protected CheckboxTreeViewer createTreeViewer(Composite parent) {
		CheckboxTreeViewer viewer = super.createTreeViewer(parent);
		viewer.getTree().addSelectionListener(new SelectionListener() {
			
			public void widgetSelected(SelectionEvent e) {
				TreeItem item = (TreeItem) e.item;
				boolean isSelected = item.getChecked();
				if (item.getParentItem() != null) {
					item.getParentItem().setChecked(isSelected);
					if (!isSelected) {
						for (TreeItem childItem : item.getParentItem().getItems()) {
							if (childItem.getChecked()) {
								item.getParentItem().setChecked(true);
								break;
							}
						}
					}
				}
				for (TreeItem childItem : item.getItems())
					childItem.setChecked(isSelected);
			}
			
			public void widgetDefaultSelected(SelectionEvent e) {
				e.getSource();
			}
		});
		
		return viewer;
	}

}
