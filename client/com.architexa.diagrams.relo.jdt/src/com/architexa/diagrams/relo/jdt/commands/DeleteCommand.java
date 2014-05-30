package com.architexa.diagrams.relo.jdt.commands;

import java.util.ArrayList;
import java.util.List;


import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.model.ArtifactRel;
import com.architexa.diagrams.model.RootArtifact;
import com.architexa.diagrams.relo.parts.ReloController;
import com.architexa.org.eclipse.gef.commands.Command;

public class DeleteCommand extends Command  {
	private List<ArtifactRel> connections = new ArrayList<ArtifactRel>();
	private RootArtifact rootArt;
	private ArtifactFragment cuAF;
	private ReloController rc;

	public DeleteCommand(RootArtifact rootArt, ArtifactFragment cuAF, ReloController rc) {
    	super("Delete");
    	this.rootArt = rootArt;
    	this.cuAF = cuAF; 
    	this.rc = rc; 
    }

	@Override
    public void execute() {
		findConnections(connections, cuAF);
	   	
		for (ArtifactRel rel : connections)
    		rc.hideRel(rel);    	
		rootArt.removeVisibleArt(cuAF);
    }

	@Override
    public void undo() {
    	rootArt.addVisibleArt(cuAF);
    	
    	// restore connections
    	for (ArtifactRel rel : connections)
    		rel.connect(rel.getSrc(), rel.getDest());
    	connections.clear();
    }
    
    private void findConnections(List<ArtifactRel> connections, ArtifactFragment part) {
    	// for both source and target connections
    	// check if connection already found
    	// else add to the list of rel to delete
    	List <ArtifactRel> partConnections = new ArrayList<ArtifactRel>(part.getSourceConnections());
    	partConnections.addAll(part.getTargetConnections());
    	for (ArtifactRel conn : partConnections) { 
    		 if (connections.contains(conn)) continue; 
    		 connections.add(conn);   	
    	}
    	if (part instanceof ArtifactFragment) {
    		for (ArtifactFragment child :  ((ArtifactFragment)part).getShownChildren())
    			findConnections(connections, child);
    	}
    }    
};













