package com.architexa.diagrams.chrono.editpolicies;

import com.architexa.org.eclipse.gef.GraphicalEditPart;
import com.architexa.org.eclipse.gef.handles.MoveHandle;
import com.architexa.org.eclipse.gef.handles.MoveHandleLocator;

/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public class SeqMoveHandle extends MoveHandle {

	public SeqMoveHandle(GraphicalEditPart owner) {
		super(owner, new MoveHandleLocator(SeqSelectionEditPolicy.getReferenceFigure(owner)));
	}

}
