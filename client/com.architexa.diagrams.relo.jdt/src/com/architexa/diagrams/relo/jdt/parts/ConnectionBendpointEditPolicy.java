package com.architexa.diagrams.relo.jdt.parts;

import java.util.ArrayList;
import java.util.List;


import com.architexa.diagrams.commands.SetConnectionBendpointCommand;
import com.architexa.diagrams.model.ArtifactRel;
import com.architexa.diagrams.model.ConnectionBendpoint;
import com.architexa.diagrams.parts.ConnectionBendpointDPolicy;
import com.architexa.org.eclipse.draw2d.Bendpoint;
import com.architexa.org.eclipse.draw2d.Connection;
import com.architexa.org.eclipse.draw2d.geometry.Point;
import com.architexa.org.eclipse.gef.Request;
import com.architexa.org.eclipse.gef.commands.Command;
import com.architexa.org.eclipse.gef.editpolicies.BendpointEditPolicy;
import com.architexa.org.eclipse.gef.requests.BendpointRequest;

public class ConnectionBendpointEditPolicy extends BendpointEditPolicy{

	@Override
	protected Command getCreateBendpointCommand(BendpointRequest request) {
		List<Bendpoint> constraint = (List<Bendpoint>)getConnection().getRoutingConstraint();
		int i = request.getIndex();
		
		if (getHost() instanceof InheritanceRelationPart) {
			if (constraint == null || constraint.isEmpty()) {
				return null;
			}
		}
		if (getHost() instanceof MethodCallRelationPart) {
			if (constraint == null || constraint.size() < i) return null;
		}
		
		SetConnectionBendpointCommand com = new SetConnectionBendpointCommand((List<Bendpoint>) getConnection().getRoutingConstraint(), (ArtifactRel)request.getSource().getModel());
		Point p = request.getLocation();
		Connection conn = getConnection();
		
		conn.translateToRelative(p);
		
		com.setLocation(p);
		return com;
	}
	
	@Override
	protected Command getDeleteBendpointCommand(BendpointRequest request) {
		return null;
//		BendpointCommand com = new DeleteBendpointCommand();
//		Point p = request.getLocation();
//		com.setLocation(p);
//		com.setRel((ArtifactRel)request.getSource().getModel());
//		com.setIndex(request.getIndex());
//		return com;
	}

	@Override
	protected Command getMoveBendpointCommand(BendpointRequest request) {
		SetConnectionBendpointCommand com = new SetConnectionBendpointCommand((List<Bendpoint>) getConnection().getRoutingConstraint(), (ArtifactRel)request.getSource().getModel());
		Point p = request.getLocation();
		Connection conn = getConnection();
		conn.translateToRelative(p);
		com.setLocation(p);
		return com;
	}
	
	
	@Override
	public void eraseSourceFeedback(Request request) {
		super.eraseSourceFeedback(request);
		firstMoveCreate = false;
		savedNextPoint = null;
		savedXPartnerLoc = null;
		savedYPartnerLoc = null;
		isDeleting = false;
	}
	
	boolean firstMoveCreate = false;
	Point savedNextPoint;
	Point savedXPartnerLoc;
	Point savedYPartnerLoc;

	@Override
	protected void showCreateBendpointFeedback(BendpointRequest request) {
		if (getHost() instanceof InheritanceRelationPart)
			showInheritaceBPCreateFeedback(request);
		if (getHost() instanceof MethodCallRelationPart)
			showMethodCallBPCreateFeedback(request);
	}
	
	private void showMethodCallBPCreateFeedback(BendpointRequest request) {
		Point p = new Point(request.getLocation());
		getConnection().translateToRelative(p);

		List<Bendpoint> constraint = (List<Bendpoint>) getConnection().getRoutingConstraint();
		int index = request.getIndex();
		Connection conn = getConnection();
		// Creating for the first time
		if (constraint == null || constraint.size() == 0){
			constraint = new ArrayList<Bendpoint>();
			Point ref1 = getConnection().getSourceAnchor().getReferencePoint();
			Point ref2 = getConnection().getTargetAnchor().getReferencePoint();
			conn.translateToRelative(ref1);
			conn.translateToRelative(ref2);
			// Straight line
			if (ref1.x == ref2.x) return; 
			ConnectionBendpoint bp1 = new ConnectionBendpoint(new Point(p.x, ref1.y));
			ConnectionBendpoint bp2 = new ConnectionBendpoint(new Point(p.x, ref2.y));
			constraint.add(bp1);
			constraint.add(bp2);
			getConnection().setRoutingConstraint(constraint);
			return;
		}
		
		if (constraint.size() <= index || 
				!ConnectionBendpointDPolicy.isModified((ArtifactRel) getHost().getModel())) return;
		
		Point prevPoint, nextPoint;
		if (index == 0) { // First point after src
			prevPoint = getConnection().getSourceAnchor().getReferencePoint();
		} else {
			prevPoint = getPoint(constraint.get(index - 1));
		}
		if (savedNextPoint == null) {
			if (constraint.size() <= index) // point just before target
				nextPoint = getConnection().getTargetAnchor().getReferencePoint();
			else	
				nextPoint = getPoint(constraint.get(index));
			savedNextPoint = nextPoint;
		} else
			nextPoint = savedNextPoint;
		
		if (!firstMoveCreate) {
			saveOriginalConstraint();
			constraint.remove(index);

			if (prevPoint.y == nextPoint.y) {
				if (constraint.size() <= index) {
					constraint.add(new ConnectionBendpoint(new Point(p.x, nextPoint.y)));
					constraint.add(new ConnectionBendpoint(new Point(p.x, p.y)));
					constraint.add(new ConnectionBendpoint(new Point(nextPoint.x, p.y)));
				} else {// insert in between bends
					constraint.add(index, new ConnectionBendpoint(new Point(p.x, nextPoint.y)));
					constraint.add(index + 1, new ConnectionBendpoint(new Point(p.x, p.y)));
					constraint.add(index + 2, new ConnectionBendpoint(new Point(nextPoint.x, p.y)));
				}
			} else {
				if (constraint.size() <= index) {
					constraint.add(new ConnectionBendpoint(new Point(nextPoint.x, p.y)));
					constraint.add(new ConnectionBendpoint(new Point(p.x, p.y)));
					constraint.add(new ConnectionBendpoint(new Point(p.x, nextPoint.y)));
				} else {
					constraint.add(index, new ConnectionBendpoint(new Point(nextPoint.x, p.y)));
					constraint.add(index + 1, new ConnectionBendpoint(new Point(p.x, p.y)));
					constraint.add(index + 2, new ConnectionBendpoint(new Point(p.x, nextPoint.y)));
				}
			}
			firstMoveCreate = true;
		} else {
			if (prevPoint.y == nextPoint.y) {
				constraint.set(index, new ConnectionBendpoint(new Point(p.x, nextPoint.y)));
				if (constraint.size() == index + 1)
					constraint.add(new ConnectionBendpoint(new Point(p.x, p.y)));
				else
					constraint.set(index + 1, new ConnectionBendpoint(new Point(p.x, p.y)));
				if (constraint.size() > index + 2) // Error handle for first time the bend is created
					constraint.set(index + 2, new ConnectionBendpoint(new Point(nextPoint.x, p.y)));
			} else {
				constraint.set(index, new ConnectionBendpoint(new Point(nextPoint.x, p.y)));
				if (constraint.size() == index + 1)
					constraint.add(new ConnectionBendpoint(new Point(p.x, p.y)));
				else
					constraint.set(index + 1, new ConnectionBendpoint(new Point(p.x, p.y)));
				if (constraint.size() > index + 2) // Error handle for first time the bend is created
					constraint.set(index + 2, new ConnectionBendpoint(new Point(p.x, nextPoint.y)));
			}
		}

		setConstraints(constraint);
	}

	private void showInheritaceBPCreateFeedback(BendpointRequest request) {
		if (isDeleting) return;
		Point p = new Point(request.getLocation());
		getConnection().translateToRelative(p);

		List<Bendpoint> constraint = (List<Bendpoint>) getConnection().getRoutingConstraint();
		int index = request.getIndex();
		
		if (constraint == null || constraint.size() <= index) return;
		Point prevPoint, nextPoint;

		if (index == 0) // First point after src
			prevPoint = getConnection().getSourceAnchor().getReferencePoint();
		else
			prevPoint = getPoint(constraint.get(index - 1));

		if (savedNextPoint == null) {
			if (constraint.size() <= index) // point just before target
				nextPoint = getConnection().getTargetAnchor().getReferencePoint();
			else	
				nextPoint = getPoint(constraint.get(index));
			savedNextPoint = nextPoint;
		} else
			nextPoint = savedNextPoint;
		
		if (!firstMoveCreate) {
			saveOriginalConstraint();
			constraint.remove(index);

			if (prevPoint.y == nextPoint.y) {
				if (constraint.size() <= index) {
					constraint.add(new ConnectionBendpoint(new Point(p.x, nextPoint.y)));
					constraint.add(new ConnectionBendpoint(new Point(p.x, p.y)));
					constraint.add(new ConnectionBendpoint(new Point(nextPoint.x, p.y)));
				} else {
					constraint.add(index, new ConnectionBendpoint(new Point(p.x, nextPoint.y)));
					constraint.add(index + 1, new ConnectionBendpoint(new Point(p.x, p.y)));
					constraint.add(index + 2, new ConnectionBendpoint(new Point(nextPoint.x, p.y)));
				}
			} else {
				if (constraint.size() <= index) {
					constraint.add(new ConnectionBendpoint(new Point(nextPoint.x, p.y)));
					constraint.add(new ConnectionBendpoint(new Point(p.x, p.y)));
					constraint.add(new ConnectionBendpoint(new Point(p.x, nextPoint.y)));
				} else {
					constraint.add(index, new ConnectionBendpoint(new Point(nextPoint.x, p.y)));
					constraint.add(index + 1, new ConnectionBendpoint(new Point(p.x, p.y)));
					constraint.add(index + 2, new ConnectionBendpoint(new Point(p.x, nextPoint.y)));
				}
			}
			firstMoveCreate = true;
		} else {
			if (prevPoint.y == nextPoint.y) {
				constraint.set(index, new ConnectionBendpoint(new Point(p.x, nextPoint.y)));
				constraint.set(index + 1, new ConnectionBendpoint(new Point(p.x, p.y)));
				constraint.set(index + 2, new ConnectionBendpoint(new Point(nextPoint.x, p.y)));
			} else {
				constraint.set(index, new ConnectionBendpoint(new Point(nextPoint.x, p.y)));
				constraint.set(index + 1, new ConnectionBendpoint(new Point(p.x, p.y)));
				constraint.set(index + 2, new ConnectionBendpoint(new Point(p.x, nextPoint.y)));
			}
		}
		setConstraints(constraint);
	}

	private Point getPoint(Object point) {
		return new Point(((ConnectionBendpoint) point).getLocation().x, ((ConnectionBendpoint) point).getLocation().y);
	}
	
	@Override
	protected void showMoveBendpointFeedback(BendpointRequest request) {
		if (getHost() instanceof InheritanceRelationPart)
			showInheritaceMoveBPFeedback(request);
		if (getHost() instanceof MethodCallRelationPart)
			showMethodCallMoveBPFeedback(request);
		
	}
	
	private void showMethodCallMoveBPFeedback(BendpointRequest request) {
		Point p = new Point(request.getLocation());
		getConnection().translateToRelative(p);
		List constraint = (List) getConnection().getRoutingConstraint();
		int index = request.getIndex();
		if (index >= constraint.size()) return;
		Point oldMovedLoc = ((Bendpoint)constraint.get(index)).getLocation();
		int xIndex = -1, yIndex = -1;
		// Find X-Y partners 
		if (index == 0) {  // first bp moved
			Bendpoint firstBP = (Bendpoint) constraint.get(index);
			if (p.x == firstBP.getLocation().x) return;
			xIndex = index + 1;
		} else if (index == constraint.size() - 1) {
			Bendpoint firstBP = (Bendpoint) constraint.get(index);
			if (p.x == firstBP.getLocation().x) return;
			xIndex = index - 1;
		} else {
			Bendpoint bp = (Bendpoint) constraint.get(index - 1);
			if (oldMovedLoc.x == bp.getLocation().x) {
				xIndex = index - 1;
				yIndex = index + 1;
			} else {
				xIndex = index + 1;
				yIndex = index - 1;
			}
		}
		
		constraint.set(index, new ConnectionBendpoint(new Point(p)));
		// Move X-Partner in Y dim and Y-partner in X dim
		if (yIndex !=  -1 && constraint.size() > yIndex) {
			if (savedYPartnerLoc == null)
				savedYPartnerLoc = ((Bendpoint)constraint.get(yIndex)).getLocation();
			constraint.set(yIndex, new ConnectionBendpoint(new Point(savedYPartnerLoc.x, p.y)));
		}
		
		if (xIndex != -1 && constraint.size() > xIndex) {
			if (savedXPartnerLoc == null)
				savedXPartnerLoc = ((Bendpoint)constraint.get(xIndex)).getLocation();
			constraint.set(xIndex, new ConnectionBendpoint(new Point(p.x, savedXPartnerLoc.y)));
		}
		
		setConstraints(constraint);
	}

	private void showInheritaceMoveBPFeedback(BendpointRequest request) {
		if (isDeleting) return;
		Point p = new Point(request.getLocation());
		getConnection().translateToRelative(p);
		List<Bendpoint> constraint = (List<Bendpoint>) getConnection().getRoutingConstraint();
		int index = request.getIndex();
		if (index >= constraint.size()) return;
		Point oldMovedLoc = ((Bendpoint)constraint.get(index)).getLocation();
		int xIndex = -1, yIndex = -1;
		// Find X-Y partners 
		if (index == 0) {  // first bp moved
			Bendpoint firstBP = constraint.get(index);
			if (p.y == firstBP.getLocation().y) return;
			yIndex = index + 1;
		} else if (index == constraint.size() - 1) {
			Bendpoint firstBP = constraint.get(index);
			if (p.y == firstBP.getLocation().y) return;
			yIndex = index - 1;
		} else {
			Bendpoint bp = (Bendpoint) constraint.get(index - 1);
			if (oldMovedLoc.x == bp.getLocation().x) {
				xIndex = index - 1;
				yIndex = index + 1;
			} else {
				xIndex = index + 1;
				yIndex = index - 1;
			}
		}
		
		constraint.set(index, new ConnectionBendpoint(new Point(p)));
		// Move X-Partner in Y dim and Y-partner in X dim
		if (xIndex != -1 && constraint.size() > xIndex) {
			if (savedXPartnerLoc == null)
				savedXPartnerLoc = ((Bendpoint)constraint.get(xIndex)).getLocation();
			constraint.set(xIndex, new ConnectionBendpoint(new Point(p.x, savedXPartnerLoc.y)));
		}
		
		if (yIndex !=  -1 && constraint.size() > yIndex) {
			if (savedYPartnerLoc == null)
				savedYPartnerLoc = ((Bendpoint)constraint.get(yIndex)).getLocation();
			constraint.set(yIndex, new ConnectionBendpoint(new Point(savedYPartnerLoc.x, p.y)));
		}
		setConstraints(constraint);
	}

	private void setConstraints(List<Bendpoint> constraint) {
		constraint = clearRedundantBP(constraint);
		getConnection().setRoutingConstraint(constraint);		
	}


	boolean isDeleting = false;
	private List<Bendpoint> clearRedundantBP(List<Bendpoint> bendPts) {
		Point startPoint = getConnection().getSourceAnchor().getReferencePoint();
		Point endPoint = getConnection().getTargetAnchor().getReferencePoint();
		
		getConnection().translateToRelative(startPoint);
		getConnection().translateToRelative(endPoint);
		
		List<Bendpoint> tempBPList = new ArrayList<Bendpoint>(bendPts);
		tempBPList.add(0, new ConnectionBendpoint(startPoint));
		tempBPList.add(new ConnectionBendpoint(endPoint));
		List<Bendpoint> returnBendpoints = new ArrayList<Bendpoint>(tempBPList);
		for (int i = tempBPList.size() - 1; i > 1 ; i--) {
			ConnectionBendpoint prev = (ConnectionBendpoint) tempBPList.get(i);
			ConnectionBendpoint next = (ConnectionBendpoint) tempBPList.get(i - 2);
			if (prev.getLocation().x == next.getLocation().x || prev.getLocation().y == next.getLocation().y) {
				returnBendpoints.remove(i - 1);
				isDeleting = true;
			}
		}
		returnBendpoints.remove(0);
		returnBendpoints.remove(returnBendpoints.size() - 1);
		return returnBendpoints;
	}
}
