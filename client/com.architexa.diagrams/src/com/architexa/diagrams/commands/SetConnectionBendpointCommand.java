
package com.architexa.diagrams.commands;

import java.util.ArrayList;
import java.util.List;


import com.architexa.diagrams.model.ArtifactRel;
import com.architexa.diagrams.parts.ConnectionBendpointDPolicy;
import com.architexa.org.eclipse.draw2d.Bendpoint;

/*
 * Command to set bendpoints for a connection from the routing constraint
 * Create, Move and Delete all modify the routing constraint and call this command 
 */
public class SetConnectionBendpointCommand extends BendpointCommand {

	List<Bendpoint> old_constraints;
	List<Bendpoint> new_constraints;
	boolean isConnModified;
	boolean oldConnModified;
	
	public SetConnectionBendpointCommand(List<Bendpoint> routingConstraint,
			ArtifactRel model) {
		this.old_constraints = new ArrayList<Bendpoint>(ConnectionBendpointDPolicy.getBendpoints(model));
		if (routingConstraint == null)
			throw new IllegalArgumentException("Null constraints provided.");
		new_constraints = routingConstraint;
		setRel(model);
		isConnModified = true;
//		oldConnModified = true;
	}

	public SetConnectionBendpointCommand(List<Bendpoint> routingConstraint,
			ArtifactRel artifactRel, boolean isModified) {
		this(routingConstraint, artifactRel);
//		oldConnModified = isModified;
		this.isConnModified = isModified;
	}

	@Override
	public void execute() {
		createNeededBP(getRel(), new_constraints);
	}

	private void createNeededBP(ArtifactRel rel, List<Bendpoint> constraint) {
		oldConnModified = ConnectionBendpointDPolicy.isModified(rel);
		ConnectionBendpointDPolicy.setModified(rel, isConnModified);
		ConnectionBendpointDPolicy.clearBendPoints(rel, true);
		for (int i = 0; i < constraint.size(); i++) {
			Bendpoint bp = constraint.get(i);
			if (i == constraint.size() - 1)
				ConnectionBendpointDPolicy.addBendpoint(rel, bp, i, true);
			else
				ConnectionBendpointDPolicy.addBendpoint(rel, bp, i, false);
		}
	}

	@Override
	public void undo() {
		isConnModified = oldConnModified;
		createNeededBP(getRel(), old_constraints);
	}

	@Override
	public void redo() {
		isConnModified = oldConnModified;
		createNeededBP(getRel(), new_constraints);
	}
}
