/**
 * 
 */
package com.architexa.diagrams.strata.commands;

import java.util.List;

import org.apache.log4j.Logger;

import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.model.ArtifactRel;
import com.architexa.diagrams.strata.StrataPlugin;
import com.architexa.diagrams.strata.cache.PartitionerSupport;
import com.architexa.diagrams.strata.model.Layer;
import com.architexa.diagrams.strata.model.StrataRootDoc;
import com.architexa.diagrams.strata.model.policy.LayersDPolicy;
import com.architexa.org.eclipse.gef.commands.Command;

public final class DeleteCommand extends Command {
	public static final Logger logger = StrataPlugin.getLogger(BreakCommand.class);
	
	private final ArtifactFragment strataAFToRemove;
	private final StrataRootDoc rootDoc;
	private final ArtifactFragment parentAF;
	private List<ArtifactRel> removedRels = null;
	
	//store layer information so we can undo correctly
	private int orgLayerIndex;
	//private int orgPosIndex;
	//private Layer orgLayer;
	//private List<Layer> layers;

	private List<Layer> orgLayersCopy;

	public DeleteCommand(String label, ArtifactFragment strataAFToRemove,
			StrataRootDoc rootDoc, ArtifactFragment parentAF) {
		super(label);
		this.strataAFToRemove = strataAFToRemove;
		this.rootDoc = rootDoc;
		this.parentAF = parentAF;
	}

	@Override
	public void execute() { 
		//layers = LayersDPolicy.getLayers(parentAF);
		// null check to prevent throwing errors if the model gets in a broken state when deleting multiple things 
		if (parentAF.getParentArt() == null) return;
		
		orgLayersCopy = LayersDPolicy.getLayersCopy(parentAF, parentAF.getRootArt().getRepo());
		PartitionerSupport.setRemovedSingleChildren(parentAF.getArt().elementRes, strataAFToRemove);
		
		logger.info("Deleting: " + strataAFToRemove + " from: " + parentAF);
		removedRels = ModelUtils.removeAF(rootDoc, parentAF, strataAFToRemove);
	}

	@Override
	public void undo(){
		// do not execute if we dont se AD in the layer, since this means the layer was already deleted
		if (orgLayerIndex == -1) return;
		
		parentAF.appendShownChild(strataAFToRemove);
		LayersDPolicy.setLayers(parentAF, orgLayersCopy);
		rootDoc.addRelationships(removedRels);
	}
}