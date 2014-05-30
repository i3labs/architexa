/**
 * 
 */
package com.architexa.diagrams.strata.commands;

import org.apache.commons.collections.Predicate;

import com.architexa.diagrams.model.DirectedRel;
import com.architexa.diagrams.strata.parts.StrataArtFragEditPart;
import com.architexa.org.eclipse.gef.commands.Command;

final public class ShowAllDirectRelationCommand extends Command {
	/**
	 * 
	 */
	private final StrataArtFragEditPart strataArtFragEditPart;
	private final Predicate filter;
	private final DirectedRel rel;

	public ShowAllDirectRelationCommand(StrataArtFragEditPart strataArtFragEditPart, Predicate filter, DirectedRel rel) {
		this.strataArtFragEditPart = strataArtFragEditPart;
		this.filter = filter;
		this.rel = rel;
	}

	@Override
	public void execute() { 
	    this.strataArtFragEditPart.showAllDirectRelation(rel, filter);
	}
}