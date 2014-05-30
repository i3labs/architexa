package com.architexa.diagrams.relo.commands.old;

import org.openrdf.model.URI;

import com.architexa.diagrams.model.Artifact;
import com.architexa.diagrams.model.ArtifactRel;
import com.architexa.diagrams.model.DirectedRel;
import com.architexa.diagrams.relo.parts.ArtifactEditPart;
import com.architexa.diagrams.relo.parts.ReloController;
import com.architexa.org.eclipse.gef.commands.Command;

public class FindOrAddNodesAndRelCmd extends Command {

	ReloController rc;
	Artifact srcArt;
	Artifact dstArt;
	URI rel;

	ArtifactEditPart addedChildAEP;
	ArtifactEditPart addedCalledAEP;
	ArtifactRel addedRel;

	// Replaced by AddNodeAndRelCmd ??
	
	public FindOrAddNodesAndRelCmd(ReloController rc, Artifact srcArt, URI rel, Artifact dstArt) {
		this.rc = rc;
		this.srcArt = srcArt;
		this.dstArt = dstArt;
		this.rel = rel;		
	}

	@Override
	public void execute() {
		findOrAdd();
	}

	/**
	 * Deletes whatever this command added to the diagram.
	 * (Does nothing if the source, destination, and
	 * connection were all already in the diagram).
	 */
	@Override
	public void undo() {
		if(addedRel!=null) 
			rc.hideRel(addedRel);
		if(addedChildAEP!=null) 
			rc.getRootArtifact().removeVisibleArt(addedChildAEP.getArtFrag());
		if(addedCalledAEP!=null)
			rc.getRootArtifact().removeVisibleArt(addedCalledAEP.getArtFrag());
	}

	@Override
	public void redo() {
		findOrAdd();
	}

	// Adds the source to the diagram if it is not already present, adds the destination
	// to the diagram if it is not already present, and adds a connection from the
	// source to the destination, unless the connection already exists between them. 
	private void findOrAdd() {
		ArtifactEditPart foundSrcEP = rc.findArtifactEditPart(srcArt);
		if(foundSrcEP==null) {
			foundSrcEP = rc.createOrFindArtifactEditPart(srcArt);
			addedChildAEP = foundSrcEP;
		}
		if(rc.findArtifactEditPart(dstArt)==null) 
			addedCalledAEP = rc.createOrFindArtifactEditPart(dstArt);

		if(rc.canAddRel(foundSrcEP.getArtFrag(), DirectedRel.getFwd(rel), dstArt))
			addedRel = rc.addRel(srcArt, rel, dstArt);
	}

}
