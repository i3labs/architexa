package com.architexa.diagrams.relo.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchPart;

import com.architexa.org.eclipse.gef.EditPart;
import com.architexa.org.eclipse.gef.GraphicalViewer;
import com.architexa.org.eclipse.gef.ui.actions.SelectAllAction;

/** 
 * An action which selects all edit parts in the active workbench part.
 */
public class SelectAllUIAction
	extends SelectAllAction
{

private IWorkbenchPart part;

public SelectAllUIAction(IWorkbenchPart part) {
	super(part);
	this.part = part;
}


@SuppressWarnings("unchecked")
private List<EditPart> recursiveGetChildren(List<EditPart> children) {
	List<EditPart> retVal = new ArrayList<EditPart>(children);
	for (EditPart child : children) {
		retVal.addAll(recursiveGetChildren(child.getChildren()));
	}
	return retVal;
}

@SuppressWarnings("unchecked")
@Override
public void run() {
	GraphicalViewer viewer = (GraphicalViewer)part.getAdapter(GraphicalViewer.class);
	List<EditPart> allChildren = recursiveGetChildren(viewer.getContents().getChildren());
	
	if (viewer != null)
		viewer.setSelection(new StructuredSelection(allChildren));

}


}
