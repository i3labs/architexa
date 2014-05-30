/* 
 * Copyright (c) 2004-2005 Massachusetts Institute of Technology. This code was
 * developed as part of the Haystack (http://haystack.lcs.mit.edu/) research 
 * project at MIT. Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation files (the 
 * "Software"), to deal in the Software without restriction, including without 
 * limitation the rights to use, copy, modify, merge, publish, distribute, 
 * sublicense, and/or sell copies of the Software, and to permit persons to whom
 * the Software is furnished to do so, subject to the following conditions: 
 * 
 * The above copyright notice and this permission notice shall be included in 
 * all copies or substantial portions of the Software. 
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER  
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, 
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE. 
 */
package com.architexa.diagrams.relo.jdt.parts;

import java.util.ArrayList;
import java.util.List;

import jiggle.Edge;
import jiggle.Graph;
import jiggle.PointedEdge;
import jiggle.Vertex;

import org.eclipse.swt.graphics.Color;

import com.architexa.diagrams.commands.SetConnectionBendpointCommand;
import com.architexa.diagrams.model.ArtifactRel;
import com.architexa.diagrams.model.ConnectionBendpoint;
import com.architexa.diagrams.parts.ConnectionBendpointDPolicy;
import com.architexa.diagrams.relo.parts.AbstractReloEditPart;
import com.architexa.diagrams.relo.parts.ReloArtifactRelEditPart;
import com.architexa.diagrams.relo.parts.SideCenteredAnchor;
import com.architexa.org.eclipse.draw2d.Bendpoint;
import com.architexa.org.eclipse.draw2d.BendpointConnectionRouter;
import com.architexa.org.eclipse.draw2d.Connection;
import com.architexa.org.eclipse.draw2d.ConnectionAnchor;
import com.architexa.org.eclipse.draw2d.GraphAnimation;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.Label;
import com.architexa.org.eclipse.draw2d.PolygonDecoration;
import com.architexa.org.eclipse.draw2d.PositionConstants;
import com.architexa.org.eclipse.draw2d.RoundedPolylineConnection;
import com.architexa.org.eclipse.draw2d.Triangle2;
import com.architexa.org.eclipse.draw2d.geometry.Point;
import com.architexa.org.eclipse.gef.EditPolicy;
import com.architexa.org.eclipse.gef.Request;
import com.architexa.org.eclipse.gef.commands.CompoundCommand;
import com.architexa.org.eclipse.gef.requests.ReconnectRequest;



/**
 */
public class InheritanceRelationPart extends ReloArtifactRelEditPart {
    
    class InheritanceConnectionRouter extends BendpointConnectionRouter {
        private int distFromTgt;
        public InheritanceConnectionRouter (int distFromTgt) {
            this.distFromTgt = distFromTgt;
        }

        @Override
        public void route(Connection conn) {
        	GraphAnimation.recordInitialState(conn);
			if (!GraphAnimation.playbackState(conn)) {
	        	List<Bendpoint> bendPts = (List<Bendpoint>) getConnectionFigure().getRoutingConstraint();
	        	// If empty add first and last bps
	        	if ((bendPts == null 
	        			|| bendPts.isEmpty() 
	        			|| bendPts.size() <= 2) 
	        			&& !ConnectionBendpointDPolicy.isModified(getArtifactRel())) {
	        		createInitialBendpoints(conn);
	        		bendPts = ConnectionBendpointDPolicy.getBendpoints(getArtifactRel());
	        	} else { // add code to show feedback
	        		bendPts = alignFirstAndLastBends(conn, getArtifactRel());
	        	}
	        	setConstraint(conn, bendPts);
	        	super.route(conn);
			}
		}

        public void createInitialBendpoints(Connection conn) {
        	Point startPoint = getStartPoint(conn);
        	conn.translateToRelative(startPoint);
        	Point endPoint = getEndPoint(conn);
        	conn.translateToRelative(endPoint);
        	ConnectionBendpointDPolicy.clearBendPoints(getArtifactRel(), false);
    		ConnectionBendpointDPolicy.addBendpoint(getArtifactRel(), new ConnectionBendpoint(new Point(startPoint.x,endPoint.y+distFromTgt)), 0, false);
    		ConnectionBendpointDPolicy.addBendpoint(getArtifactRel(), new ConnectionBendpoint(new Point(endPoint.x,endPoint.y+distFromTgt)), 1, false);
		}
        
        public Point getStart(Connection conn) {
        	return getStartPoint(conn);
        }
        
        public Point getEnd(Connection conn) {
        	return getEndPoint(conn);
        }
        
        private List<Bendpoint> alignFirstAndLastBends(Connection conn, ArtifactRel artifactRel) {
        	List bendPts = (List) conn.getRoutingConstraint();
        	if (bendPts.size() < 2) return bendPts;
    		Point startPoint = getStartPoint(conn);
        	conn.translateToRelative(startPoint);
        	Point endPoint = getEndPoint(conn);
        	conn.translateToRelative(endPoint);
        	Point firstBP = ((Bendpoint)bendPts.get(0)).getLocation();
        	Point lastBP = ((Bendpoint)bendPts.get(bendPts.size() - 1)).getLocation();
        	if (firstBP.x != startPoint.x)
        		bendPts.set(0, new ConnectionBendpoint(new Point(startPoint.x, firstBP.y)));
        	if (lastBP.x != endPoint.x)
        		bendPts.set(bendPts.size() -1, new ConnectionBendpoint(new Point(endPoint.x, lastBP.y)));
        	return bendPts;
    	}

    }

	@Override
    protected Edge getEdgeForGraph(Graph graph, Vertex source, Vertex target) {
        //return new PointedEdge(graph, target, source, /*vert*/true, /*asc*/true);
        return new PointedEdge(graph, source, target, /*vert*/true, /*asc*/false);
    }

	@Override
	protected void createEditPolicies() {
		super.createEditPolicies();
		installEditPolicy(EditPolicy.CONNECTION_BENDPOINTS_ROLE, new ConnectionBendpointEditPolicy());
        ArtifactRel.ensureInstalledPolicy((ArtifactRel)getModel(), ConnectionBendpointDPolicy.DefaultKey, ConnectionBendpointDPolicy.class);
	}
	
	@Override
	public void addBendPointRemoveCmd(CompoundCommand cmd, Point newPoint, boolean isSrc) {
		Connection conn = getConnectionFigure();
		Point startPoint = ((InheritanceConnectionRouter)conn.getConnectionRouter()).getStart(conn);
    	conn.translateToRelative(startPoint);
    	Point endPoint = ((InheritanceConnectionRouter)conn.getConnectionRouter()).getEnd(conn);
    	conn.translateToRelative(endPoint);
    	if (!isSrc)
    		endPoint.translate(newPoint); 
    	else
    		startPoint.translate(newPoint);

    	if (Math.abs(endPoint.x - startPoint.x) < 15 ) {
    		cmd.add(new SetConnectionBendpointCommand(new ArrayList<Bendpoint>(), getArtifactRel(), false));
    	} else {
    		checkForInitialBendpoints(conn);
    	}
	}
	
	private void checkForInitialBendpoints(Connection conn) {
		if (conn.getRoutingConstraint() == null || ((List<Bendpoint>)conn.getRoutingConstraint()).isEmpty()) {
			((InheritanceConnectionRouter)conn.getConnectionRouter()).createInitialBendpoints(conn);
		}
	}
	
	@Override
    protected IFigure createFigure() {
		//System.err.println("== == InheritanceRelationPart.createFigure called");

		//PolylineConnection conn = new PolylineConnection();
		RoundedPolylineConnection conn = new RoundedPolylineConnection();
		//conn.setConnectionRouter(new ManhattanConnectionRouter());
		conn.setConnectionRouter(new InheritanceConnectionRouter(15));
		
		//conn.setConnectionRouter(new ManhattanConnectionRouter() {
		//	public void route(Connection conn) {
		//		GraphAnimation.recordInitialState(conn);
		//		if (!GraphAnimation.playbackState(conn))
		//			super.route(conn);
		//	}
		//});

		PolygonDecoration dec = new PolygonDecoration();
		dec.setScale(7, 5);
		dec.setBackgroundColor(new Color(null, 255, 255, 255));
		conn.setTargetDecoration(dec);
		conn.setToolTip(new Label(" " + getRelationLabel() + " "));
		return conn;
	}

	@Override
    protected ConnectionAnchor getSourceConnectionAnchor(AbstractReloEditPart srcEP) {
        return new SideCenteredAnchor(srcEP.getFigure(), SideCenteredAnchor.top);
	}
	@Override
    protected ConnectionAnchor getTargetConnectionAnchor(AbstractReloEditPart tgtEP) {
        return new SideCenteredAnchor(tgtEP.getFigure(), SideCenteredAnchor.bottom);
	}


    /* (non-Javadoc)
     * @see com.architexa.diagrams.relo.parts.ArtifactNavAidsEditPolicy.RelationFigure#getArrow()
     */
    @Override
    public IFigure getArrow() {
        Triangle2 arrowHead = new Triangle2();
        arrowHead.setFill(false);
        return getArrow(PositionConstants.NORTH, arrowHead);
    }

    @Override
	public void showSourceFeedback(Request request) {
    	 if (request.getType().equals(REQ_CONNECTION_START) || request.getType().equals(REQ_CONNECTION_END) 
   	    		|| request.getType().equals(REQ_RECONNECT_SOURCE) || request.getType().equals(REQ_RECONNECT_TARGET)) {
    	    	if (((ReconnectRequest) request).getTarget() instanceof PackageEditPart) return;
    	    	else super.showSourceFeedback(request);
    	 } else if (request.getType().equals(REQ_MOVE_BENDPOINT) || request.getType().equals(REQ_CREATE_BENDPOINT)) {
			 super.showSourceFeedback(request);
		 }
    }
    @Override
    public void showTargetFeedback(Request request) {
    	  if (request.getType().equals(REQ_CONNECTION_START) || request.getType().equals(REQ_CONNECTION_END) 
  	    		|| request.getType().equals(REQ_RECONNECT_SOURCE) || request.getType().equals(REQ_RECONNECT_TARGET)) {
	    	if (((ReconnectRequest) request).getTarget() instanceof PackageEditPart) return;
	    	else super.showTargetFeedback(request);
    	  } else if (request.getType().equals(REQ_MOVE_BENDPOINT) || request.getType().equals(REQ_CREATE_BENDPOINT)) {
 			 super.showTargetFeedback(request);
 		 }
    }
    
}