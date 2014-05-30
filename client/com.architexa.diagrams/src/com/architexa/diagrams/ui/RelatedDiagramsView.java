package com.architexa.diagrams.ui;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;


public class RelatedDiagramsView extends ViewPart {

	private TreeViewer viewer;

	public RelatedDiagramsView() {
		super();
		addUserSelectionListener();
	}

	@Override
	public void dispose() {
		super.dispose();
		removeUserSelectionListener();
	}

	@Override
	public void createPartControl(Composite parent) {
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL) {
			@Override
			public void refresh(Object element) {
				super.refresh(element);
				expandAll(); // Always keep tree expanded so related diagrams always visible
			}
		};
		viewer.setContentProvider(new RelatedDiagramsProvider());
		viewer.setLabelProvider(new LabelProvider());
	}

	public StructuredViewer getViewer() {
		return viewer;
	}

	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	private boolean addUserSelectionListener() {
		IWorkbenchWindow[] wwin = PlatformUI.getWorkbench().getWorkbenchWindows();
		for (IWorkbenchWindow tgtWkbnchWin : wwin) {
			tgtWkbnchWin.getSelectionService().addPostSelectionListener(userSelectionListener);
		}

		return true;
	}
	private void removeUserSelectionListener() {
		IWorkbenchWindow[] wwin = PlatformUI.getWorkbench().getWorkbenchWindows();
		for (IWorkbenchWindow tgtWkbnchWin : wwin) {
			tgtWkbnchWin.getSelectionService().removePostSelectionListener(userSelectionListener);
		}
	}
	private ISelectionListener userSelectionListener = new ISelectionListener() {
		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			viewer.setInput(selection);

			// Expand tree so that user can see all the related diagrams. (Don't
			// want it to collapse every time it's updated with related diagrams).
			viewer.expandAll();
		}
	};

	public void updateContentDescription(String description) {
		setContentDescription(description);
	}

}
