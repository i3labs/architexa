package com.architexa.debug;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import com.architexa.collab.ui.DiagramIndexer;

public class RebuildLuceneIndex implements IWorkbenchWindowActionDelegate {

	public void run(IAction action) {
		// Clear the lucene index
		DiagramIndexer.createNewIndex();
	}

	public void dispose() {}
	public void init(IWorkbenchWindow window) {}
	public void selectionChanged(IAction action, ISelection selection) {}

}
