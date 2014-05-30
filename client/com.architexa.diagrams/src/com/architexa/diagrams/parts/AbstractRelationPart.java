package com.architexa.diagrams.parts;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;


import com.architexa.diagrams.commands.HideRelCommand;
import com.architexa.diagrams.model.ArtifactRel;
import com.architexa.org.eclipse.draw2d.BendpointConnectionRouter;
import com.architexa.org.eclipse.draw2d.ColorConstants;
import com.architexa.org.eclipse.draw2d.Connection;
import com.architexa.org.eclipse.draw2d.Figure;
import com.architexa.org.eclipse.draw2d.GraphAnimation;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.PolygonDecoration;
import com.architexa.org.eclipse.draw2d.Polyline;
import com.architexa.org.eclipse.draw2d.PolylineConnection;
import com.architexa.org.eclipse.draw2d.PositionConstants;
import com.architexa.org.eclipse.draw2d.Shape;
import com.architexa.org.eclipse.draw2d.Triangle2;
import com.architexa.org.eclipse.draw2d.geometry.Point;
import com.architexa.org.eclipse.draw2d.geometry.Rectangle;
import com.architexa.org.eclipse.gef.EditPart;
import com.architexa.org.eclipse.gef.EditPolicy;
import com.architexa.org.eclipse.gef.Request;
import com.architexa.org.eclipse.gef.RequestConstants;
import com.architexa.org.eclipse.gef.commands.Command;
import com.architexa.org.eclipse.gef.editparts.AbstractConnectionEditPart;
import com.architexa.org.eclipse.gef.editparts.AbstractEditPart;
import com.architexa.org.eclipse.gef.editpolicies.ConnectionEndpointEditPolicy;
import com.architexa.org.eclipse.gef.requests.BendpointRequest;
import com.architexa.org.eclipse.gef.requests.GroupRequest;

public class AbstractRelationPart extends AbstractConnectionEditPart implements PropertyChangeListener{

	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.EditPart#getCommand(org.eclipse.gef.Request)
	 */
	@Override
    public Command getCommand(Request request) {
		if (request.getType().equals(RequestConstants.REQ_DELETE)) {
		    if (request instanceof GroupRequest && ((GroupRequest) request).getEditParts().contains(getParent())) return null;
		    
		    AbstractEditPart firstSelection = (AbstractEditPart) ((GroupRequest) request).getEditParts().get(0);
		    return new HideRelCommand((ArtifactRel) firstSelection.getModel());
	    }
		return super.getCommand(request);
	}
	
	
	@Override
	public void activate() {
		super.activate();
		if (isActive()) {
			((ArtifactRel)getModel()).addPropertyChangeListener(this);
			return;
		}
		super.activate();
		((ArtifactRel)getModel()).addPropertyChangeListener(this);
	}
	
	/**
	 * @see com.architexa.org.eclipse.gef.editparts.AbstractEditPart#createEditPolicies()
	 */
	@Override
    protected void createEditPolicies() {
        installEditPolicy(EditPolicy.CONNECTION_ENDPOINTS_ROLE, new ConnectionEndpointEditPolicy());
    }
	
	public static Figure getArrow(int direction, Triangle2 arrowHead) {
        Figure relationFig = new Figure();
        
    	arrowHead.setBackgroundColor(ColorConstants.listForeground);
    	arrowHead.setPreferredSize(10,10);
    	arrowHead.setDirection(direction);
    	Rectangle relBounds = new Rectangle(0,0,10,10);
    	switch (direction) {
        case PositionConstants.EAST:
            relBounds.translate(10, 0);
            break;
        case PositionConstants.WEST:
            break;
        case PositionConstants.NORTH:
            break;
        case PositionConstants.SOUTH:
            relBounds.translate(0, 10);
            break;
        };
        arrowHead.setBounds(relBounds);
    	relationFig.add(arrowHead);

        Polyline shaft = new Polyline() {
    	    @Override
            public void primTranslate(int dx, int dy) {
    	    	bounds.x += dx;
    	    	bounds.y += dy;
    	    	getPoints().translate(dx, dy);
    	    }
        };
        shaft.setOutline(true);
        shaft.setForegroundColor(ColorConstants.listForeground);
        arrowHead.validate();
    	shaft.addPoint(arrowHead.getBase());
    	switch (direction) {
        case PositionConstants.EAST:
            shaft.addPoint(new Point(0, 5).translate(-1, -1));
            break;
        case PositionConstants.WEST:
            shaft.addPoint(new Point(20, 5).translate(-1, -1));
            break;
        case PositionConstants.NORTH:
            shaft.addPoint(new Point(5, 20).translate(-1, -1));
            break;
        case PositionConstants.SOUTH:
            shaft.addPoint(new Point(5, 0).translate(-1, -1));
            break;
        };
    	relationFig.add(shaft);

    	
    	if ( (direction & PositionConstants.EAST_WEST) != 0) {
            relationFig.setPreferredSize(20, 10);
    	} else {
            relationFig.setPreferredSize(10, 20);
    	}
        return relationFig;
    }

    public IFigure getArrow() {
        Triangle2 arrowHead = new Triangle2();
        arrowHead.setFill(false);
        arrowHead.setClosed(false);
        return getArrow(PositionConstants.EAST, arrowHead);
    }
	
	
	/**
	 * @see com.architexa.org.eclipse.gef.editparts.AbstractConnectionEditPart#createFigure()
	 */
	@Override
    protected IFigure createFigure() {
		//System.err.println("== == InheritanceRelationPart.createFigure called");
		PolylineConnection conn = (PolylineConnection) super.createFigure();
		conn.setConnectionRouter(new BendpointConnectionRouter() {
			@Override
            public void route(Connection conn) {
				GraphAnimation.recordInitialState(conn);
				if (!GraphAnimation.playbackState(conn))
					super.route(conn);
			}
		});

		conn.setTargetDecoration(new PolygonDecoration());

        return conn;
	}

	/**
	 * @see com.architexa.org.eclipse.gef.EditPart#setSelected(int)
	 */
	@Override
    public void setSelected(int value) {
		super.setSelected(value);

		if (value != EditPart.SELECTED_NONE) {
			((Shape) getFigure()).setLineWidth(2);
		} else {
			((Shape) getFigure()).setLineWidth(1);
		}

	}

	public void propertyChange(PropertyChangeEvent evt) {
		String prop = evt.getPropertyName();
		if (ConnectionBendpointDPolicy.DefaultKey.equals(prop))	
			refreshBendpoints();
		
		refresh();
	}
	
	/**
	 * Updates the bendpoints, based on the model.
	 */
	protected void refreshBendpoints() {}
	public void showCreateBendpointFeedback(BendpointRequest request) {}
	public void restoreVariablesOnErase() {}
}
