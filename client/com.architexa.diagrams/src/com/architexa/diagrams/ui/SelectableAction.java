package com.architexa.diagrams.ui;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import com.architexa.diagrams.Activator;

/**
 * Unified interface for adding actions that work with both view and editor
 * selections. Usually works with JDTSelectionUtils for processing selections in
 * either the views or the text editor
 */
public abstract class SelectableAction extends Action implements IObjectActionDelegate, IEditorActionDelegate {
    private static final Logger logger = Activator.getLogger(SelectableAction.class);

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {}
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {}

	public abstract void selectionChanged(IAction action, ISelection selection);
	public abstract void run(IAction action);

	@Override
	public void run() {
		try {
			run(this);
		} catch (Throwable t) {
			logger.error("Unexpected Exception", t);
		}
	}

}
