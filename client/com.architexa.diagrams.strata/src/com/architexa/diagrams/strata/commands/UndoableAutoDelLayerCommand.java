/**
 * 
 */
package com.architexa.diagrams.strata.commands;

import java.util.List;

import org.apache.log4j.Logger;

import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.strata.StrataPlugin;
import com.architexa.diagrams.strata.model.Layer;
import com.architexa.diagrams.strata.model.policy.LayersDPolicy;
import com.architexa.diagrams.strata.parts.StrataRootEditPart;
import com.architexa.org.eclipse.gef.commands.Command;

public final class UndoableAutoDelLayerCommand extends Command {
	public static final Logger logger = StrataPlugin.getLogger(BreakCommand.class);
	private ArtifactFragment parent;
	private List<Layer> orgLayers;
	private StrataRootEditPart rc;

	public UndoableAutoDelLayerCommand(StrataRootEditPart rc, List<Layer> orgLayers, ArtifactFragment parentAF) {
		super("Delete Layer");
		this.rc = rc;
		this.orgLayers = orgLayers;
		this.parent = parentAF;
	}
	
	@Override
	public void execute() {
		rc.autoRemoveLayers();
	}

	@Override
	public void undo(){
		LayersDPolicy.setLayers(parent, orgLayers);
	}
}