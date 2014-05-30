package com.architexa.diagrams.ui;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.action.Action;

import com.architexa.diagrams.model.Artifact;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.parts.BasicRootController;
import com.architexa.org.eclipse.gef.commands.Command;

/*
 * Used when actions are backed by commands. This class allows multiple actions
 * to be grouped into a single compound action
 * Logic/Hashmap for detecting duplicates in in the NavAidsEditPolicy 
 */
public abstract class MultiAddCommandAction extends Action {

	private final BasicRootController brc;

	public MultiAddCommandAction(String label, BasicRootController brc) {
		super(label);
		this.brc = brc;
	}

	public abstract Command getCommand(Map<Artifact, ArtifactFragment> addedArtToAFMap);
	
	// Method overwritten by the actions in the menu to get the artifact without having to get the AST
	public Artifact getInvokedModelArtifact() {
		return null;
	}
	
	@Override
	public void run() {
		brc.execute(getCommand(new HashMap<Artifact, ArtifactFragment>()));
	}

}
