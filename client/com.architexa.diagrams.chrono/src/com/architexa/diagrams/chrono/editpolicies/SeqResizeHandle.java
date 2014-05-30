package com.architexa.diagrams.chrono.editpolicies;


import com.architexa.org.eclipse.draw2d.Cursors;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.gef.GraphicalEditPart;
import com.architexa.org.eclipse.gef.handles.RelativeHandleLocator;
import com.architexa.org.eclipse.gef.handles.ResizeHandle;

/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public class SeqResizeHandle extends ResizeHandle {

	public SeqResizeHandle(GraphicalEditPart owner, int direction) {
		super(owner, direction);

		IFigure referenceFig = SeqSelectionEditPolicy.getReferenceFigure(owner);
		setLocator(new RelativeHandleLocator(referenceFig, direction));
		setCursor(Cursors.getDirectionalCursor(direction, referenceFig.isMirrored()));
	}

}
