package com.architexa.diagrams.chrono.editpolicies;


import com.architexa.diagrams.chrono.figures.ConnectionFigure;
import com.architexa.org.eclipse.draw2d.PolylineConnection;
import com.architexa.org.eclipse.gef.GraphicalEditPart;
import com.architexa.org.eclipse.gef.editpolicies.ConnectionEndpointEditPolicy;


/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public class SeqConnectionEndpointEditPolicy extends ConnectionEndpointEditPolicy {

	@Override
	// Thickens the line of a selected connection
	protected void addSelectionHandles() {
		super.addSelectionHandles();
		((PolylineConnection)((GraphicalEditPart)getHost()).getFigure()).setLineWidth(2);
		if(((GraphicalEditPart)getHost()).getFigure() instanceof ConnectionFigure) 
			((ConnectionFigure)((GraphicalEditPart)getHost()).getFigure()).showFullLabelText();
	}

	@Override
	// Thins the line of a de-selected connection
	protected void removeSelectionHandles() {
		super.removeSelectionHandles();
		((PolylineConnection)((GraphicalEditPart)getHost()).getFigure()).setLineWidth(1);
		if(((GraphicalEditPart)getHost()).getFigure() instanceof ConnectionFigure) 
			((ConnectionFigure)((GraphicalEditPart)getHost()).getFigure()).showAbbreviatedLabelText();
	}
}
