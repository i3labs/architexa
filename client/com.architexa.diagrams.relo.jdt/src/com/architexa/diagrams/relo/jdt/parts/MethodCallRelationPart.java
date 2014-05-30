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

import org.apache.log4j.Logger;

import com.architexa.diagrams.commands.SetConnectionBendpointCommand;
import com.architexa.diagrams.jdt.RJCore;
import com.architexa.diagrams.jdt.model.CodeUnit;
import com.architexa.diagrams.jdt.utils.JDTUISupport;
import com.architexa.diagrams.model.Artifact;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.model.ArtifactRel;
import com.architexa.diagrams.model.ConnectionBendpoint;
import com.architexa.diagrams.parts.ConnectionBendpointDPolicy;
import com.architexa.diagrams.relo.jdt.ReloJDTPlugin;
import com.architexa.diagrams.relo.parts.AbstractReloEditPart;
import com.architexa.diagrams.relo.parts.SideCenteredAnchor;
import com.architexa.org.eclipse.draw2d.AbsoluteBendpoint;
import com.architexa.org.eclipse.draw2d.Bendpoint;
import com.architexa.org.eclipse.draw2d.BendpointConnectionRouter;
import com.architexa.org.eclipse.draw2d.Connection;
import com.architexa.org.eclipse.draw2d.ConnectionAnchor;
import com.architexa.org.eclipse.draw2d.GraphAnimation;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.Label;
import com.architexa.org.eclipse.draw2d.PolygonDecoration;
import com.architexa.org.eclipse.draw2d.PolylineConnection;
import com.architexa.org.eclipse.draw2d.PositionConstants;
import com.architexa.org.eclipse.draw2d.Triangle2;
import com.architexa.org.eclipse.draw2d.geometry.Point;
import com.architexa.org.eclipse.gef.EditPolicy;
import com.architexa.org.eclipse.gef.Request;
import com.architexa.org.eclipse.gef.RequestConstants;
import com.architexa.org.eclipse.gef.commands.CompoundCommand;
import com.architexa.org.eclipse.gef.requests.ReconnectRequest;
import com.architexa.store.ReloRdfRepository;



/**
 */
public class MethodCallRelationPart extends SimpleCallRelationPart {
    static final Logger logger = ReloJDTPlugin.getLogger(MethodCallRelationPart.class);

    class MethodCallConnectionRouter extends BendpointConnectionRouter {
    	@Override
        public void route(Connection conn) {
			GraphAnimation.recordInitialState(conn);
			if (!GraphAnimation.playbackState(conn)) {
				List<Bendpoint> bendPts = (List<Bendpoint>) getConnectionFigure().getRoutingConstraint();
				if (bendPts != null && !bendPts.isEmpty()) {
					bendPts = alignFirstAndLastBends(conn, getArtifactRel());
					setConstraint(conn, bendPts);
				}
				super.route(conn);
			}
		}
		 
		public Point getStart(Connection conn) {
			return getStartPoint(conn);
		}

		public Point getEnd(Connection conn) {
			return getEndPoint(conn);
		}

		private List<Bendpoint> alignFirstAndLastBends(Connection conn,
				ArtifactRel artifactRel) {
			List<Bendpoint> bendPts = (List<Bendpoint>) conn.getRoutingConstraint();
			if (bendPts.size() < 2)
				return bendPts;
			Point startPoint = getStartPoint(conn);
			conn.translateToRelative(startPoint);
			Point endPoint = getEndPoint(conn);
			conn.translateToRelative(endPoint);
			Point firstBP = ((Bendpoint) bendPts.get(0)).getLocation();
			Point lastBP = ((Bendpoint) bendPts.get(bendPts.size() - 1)).getLocation();
			if (firstBP.y != startPoint.y)
				bendPts.set(0, new ConnectionBendpoint(new Point(firstBP.x, startPoint.y)));
			if (lastBP.y != endPoint.y)
				bendPts.set(bendPts.size() - 1, new ConnectionBendpoint(new Point(lastBP.x,	endPoint.y)));
			return bendPts;
		}
    }
    
	@Override
	public void performRequest(Request req) {
		if (req.getType().equals(RequestConstants.REQ_OPEN)) {
			// Open the caller in a java editor
			// when connection is double clicked
			ArtifactFragment src = getArtifactRel().getSrc();
			ReloRdfRepository repo = getBrowseModel().getRepo();
			if(src instanceof CodeUnit) {
				JDTUISupport.openInEditor((CodeUnit)src,repo);
				return;
			}
		}
		super.performRequest(req);
	}

	@Override
    protected ConnectionAnchor getTargetConnectionAnchor(AbstractReloEditPart tgtEP) {
        if (selfCaller)
            return new SideCenteredAnchor(tgtEP.getLabelFigure(), SideCenteredAnchor.right);
        else
            return super.getTargetConnectionAnchor(tgtEP);
	}

    /* (non-Javadoc)
     * @see com.architexa.diagrams.relo.parts.ArtifactNavAidsEditPolicy.RelationFigure#getArrow()
     */
    @Override
    public IFigure getArrow() {
        return getArrow(PositionConstants.EAST, new Triangle2());
    }
    
    private boolean selfCaller = false;

    class SelfCallsConnectionRouter extends BendpointConnectionRouter {
        private int distFromTgt;
        public SelfCallsConnectionRouter (int distFromTgt) {
            this.distFromTgt = distFromTgt;
        }
        @Override
        public void route(Connection conn) {
            GraphAnimation.recordInitialState(conn);
            if (!GraphAnimation.playbackState(conn)) {
                List<Object> bendpoints = new ArrayList<Object> (2);
                
                Point startPoint = getStartPoint(conn);
                conn.translateToRelative(startPoint);

                Point endPoint = getEndPoint(conn);
                conn.translateToRelative(endPoint);
                
                int vert = Math.max(startPoint.x, endPoint.x) + distFromTgt;

                bendpoints.add(new AbsoluteBendpoint(vert,startPoint.y));
                bendpoints.add(new AbsoluteBendpoint(vert,endPoint.y));
                
                setConstraint(conn, bendpoints);
                super.route(conn);
            }
        }
    }

    @Override
    protected IFigure createFigure() {
        ArtifactRel rel = this.getArtifactRel();
        ReloRdfRepository repo = getBrowseModel().getRepo();
        if (isSelfCaller(rel, repo)) {
            selfCaller = true;
        } else
            selfCaller = false;
        
        if (selfCaller) {
            PolylineConnection conn = new PolylineConnection();
            conn.setConnectionRouter(new SelfCallsConnectionRouter(15));
            conn.setToolTip(new Label(" " + getRelationLabel() + " "));
            conn.setTargetDecoration(new PolygonDecoration());
            return conn;
        } else {
        	Connection conn = (Connection) super.createFigure();
        	conn.setConnectionRouter(new MethodCallConnectionRouter());
            return conn;
        }
    }

    @Override
	public void addBendPointRemoveCmd(CompoundCommand cmd, Point newPoint, boolean isSrc) {
		Connection conn = getConnectionFigure();
		if (!(conn.getConnectionRouter() instanceof MethodCallConnectionRouter)) return;
		Point startPoint = ((MethodCallConnectionRouter)conn.getConnectionRouter()).getStart(conn);
    	conn.translateToRelative(startPoint);
    	Point endPoint = ((MethodCallConnectionRouter)conn.getConnectionRouter()).getEnd(conn);
    	conn.translateToRelative(endPoint);
    	if (!isSrc)
    		endPoint.translate(newPoint); 
    	else
    		startPoint.translate(newPoint);
    	
    	if (Math.abs(startPoint.y - endPoint.y) < 15 )
    		cmd.add(new SetConnectionBendpointCommand(new ArrayList<Bendpoint>(), (ArtifactRel) getModel(), false));
	}
    
	private boolean isSelfCaller(ArtifactRel rel, ReloRdfRepository repo) {
		try {
			Artifact srcParent = getParentArt(rel.getSrc(), repo);
			Artifact destParent = getParentArt(rel.getDest(), repo);
			
			// When adding connections between user created classes check for Artifact Fragments too as
			// the parent resource could be the same default created resurce.
			ArtifactFragment srcParentAF = rel.getSrc().getParentArt();
			ArtifactFragment destParentAF = rel.getDest().getParentArt();
			if(srcParent.equals(destParent) && srcParentAF.equals(destParentAF)) return true; // src and dst declared in same class

			// Consider calls to/from inner classes as self calls
			Artifact srcTopLevel = getTopLevelArt(rel.getSrc().getArt(), repo);
			Artifact destTopLevel = getTopLevelArt(rel.getDest().getArt(), repo);
			if(srcTopLevel!=null) return srcTopLevel.equals(destTopLevel);
		} catch (Throwable t) {
			logger.error("Unexpected Error", t);
		}
		return false;
	}

	private Artifact getParentArt(ArtifactFragment af, ReloRdfRepository repo) {
		Artifact parent = af.getArt().queryParentArtifact(repo);
		if(parent!=null) return parent;

		// If parent Artifact information not in repo,
		// use parent ArtifactFragment info if available
		if(af.getParentArt()!=null) return af.getParentArt().getArt();
		return null;
	}

	private Artifact getTopLevelArt(Artifact art, ReloRdfRepository repo) {
		Artifact parent = art.queryParentArtifact(repo);
		Artifact topLevelClass = null;
		while(parent!=null && RJCore.classType.equals(parent.queryType(repo))) {
			topLevelClass = parent;
			parent = parent.queryParentArtifact(repo);
		}
		return topLevelClass;
	}

	
	@Override
	protected void createEditPolicies() {
		super.createEditPolicies();
		installEditPolicy(EditPolicy.CONNECTION_BENDPOINTS_ROLE, new ConnectionBendpointEditPolicy());
        ArtifactRel.ensureInstalledPolicy((ArtifactRel)getModel(), ConnectionBendpointDPolicy.DefaultKey, ConnectionBendpointDPolicy.class);
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