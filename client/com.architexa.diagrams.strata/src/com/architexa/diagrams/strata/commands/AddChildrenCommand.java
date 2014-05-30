/**
 * 
 */
package com.architexa.diagrams.strata.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;

import com.architexa.diagrams.Activator;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.strata.cache.PartitionerSupport;
import com.architexa.diagrams.strata.model.DependencyRelation;
import com.architexa.diagrams.strata.model.Layer;
import com.architexa.diagrams.strata.model.StrataRootDoc;
import com.architexa.diagrams.strata.model.policy.ClosedContainerDPolicy;
import com.architexa.diagrams.strata.model.policy.LayersDPolicy;
import com.architexa.diagrams.ui.LongCommand;

public final class AddChildrenCommand extends LongCommand {
    static final Logger logger = Activator.getLogger(AddChildrenCommand.class);

    private final List<ArtifactFragment> childrenAF;
    private final List<Layer> childrenLayers;
    private final ArtifactFragment parentAF;
	private final ArtifactFragment oldParentAF;			// needed if we are given childrenLayers (for undo ...and doing)
	
    // store parents of children added due to reParenting so they can be undone
    private final Map<ArtifactFragment,ArtifactFragment> orgChildrenToParents = new HashMap<ArtifactFragment, ArtifactFragment>();
	
    private List<DependencyRelation> newRels = null;
	private final StrataRootDoc rootDoc;
	
	// Saved Layer information
	private int parentLayerNdx;
	private Layer parentLayer;

	public AddChildrenCommand(List<ArtifactFragment> childrenAF, ArtifactFragment parentAF, StrataRootDoc rootDoc) {
		this(childrenAF, parentAF, rootDoc, null, -1);
	}

	public AddChildrenCommand(List<ArtifactFragment> childrenAF, List<Layer> childrenLayers, ArtifactFragment parentAF, ArtifactFragment oldParentAF, StrataRootDoc rootDoc) {
		this(childrenAF, childrenLayers, parentAF, oldParentAF, rootDoc, null, -1);
	}

	public AddChildrenCommand(List<ArtifactFragment> childrenAF, ArtifactFragment parentAF, StrataRootDoc rootDoc, Layer parentLayer, int parentLayerNdx) {
		this(childrenAF, null, parentAF, null, rootDoc, parentLayer, parentLayerNdx);
	}

	public AddChildrenCommand(List<ArtifactFragment> childrenAF, List<Layer> childrenLayers, ArtifactFragment parentAF, ArtifactFragment oldParentAF, StrataRootDoc rootDoc, Layer parentLayer, int parentLayerNdx) {
		super("Add Children");
		this.childrenAF = childrenAF;
		this.childrenLayers = childrenLayers;
		this.parentAF = parentAF;
		this.oldParentAF = oldParentAF;
		this.rootDoc = rootDoc;	
		this.parentLayer = parentLayer;
		this.parentLayerNdx = parentLayerNdx;
	}

	@Override
	public void prep(IProgressMonitor monitor) {
		if (parentAF.getShownChildrenCnt() == 0 && rootDoc.getShownChildrenCnt() == 0 && childrenAF.size() <= 1) {
			newRels =  new ArrayList<DependencyRelation>();
			return;
		}
		ArrayList<ArtifactFragment> allNestedChildren = new ArrayList<ArtifactFragment>(childrenAF);
		for (ArtifactFragment child : childrenAF) {
			allNestedChildren.addAll(ClosedContainerDPolicy.getShownChildren(child));	
		}
		newRels = rootDoc.getRelationshipsToAdd(allNestedChildren, monitor);
	}

	@Override
	public void execute() { 
		if (newRels == null) {
			logger.error("Unexpected Error - not prepped");
			return;
		}
		
		// store parents of children added due to reParenting
		for (ArtifactFragment child : childrenAF) {
			orgChildrenToParents.put(child, child.getParentArt());
		}
		
		// manually place item in correct layer if given a layer
		if (parentLayer != null && childrenAF.size() == 1) {
			
			ArtifactFragment tgtAF = childrenAF.get(0);
			// moving to a different parent
			parentAF.getShownChildren().add(tgtAF);
			tgtAF.setParentArt(parentAF);	
			
			// creating a new layer
			if (parentLayer.isEmpty()) {
				List<Layer> newParentLayers = LayersDPolicy.getLayers(parentAF);
				newParentLayers.add(newParentLayers.size(), parentLayer);
			}

			// adding to layer
			if (parentLayerNdx == -1 || parentLayerNdx > parentLayer.size())
				parentLayerNdx = parentLayer.size();
			parentLayer.appendShownChild(tgtAF, parentLayerNdx);
			rootDoc.addRelationships(newRels);
			parentAF.appendShownChildren(childrenAF);
			return;
		}
		
        // bring the layering to the new parent
		if (childrenLayers != null)
			LayersDPolicy.moveLayers(oldParentAF, parentAF, childrenLayers);

		if (childrenAF.size() == 1 && !(parentAF instanceof StrataRootDoc)) 
			PartitionerSupport.addSinglyAddedChild(parentAF.getArt().elementRes, childrenAF.get(0));
		else {
			if (childrenLayers == null)
				LayersDPolicy.setAutoLayering(parentAF, true);
			else {
				LayersDPolicy.setAutoLayering(parentAF, false);
				LayersDPolicy.setLayersNeedBuilding(parentAF, false);
			}
		}
		rootDoc.addRelationships(newRels);
		parentAF.appendShownChildren(childrenAF);
	}

	@Override
	public void undo() {
		rootDoc.removeRelationships(newRels);
		for (ArtifactFragment childAF : childrenAF) {
			parentAF.removeShownChild(childAF);
			PartitionerSupport.setRemovedSingleChildren(parentAF.getArt().elementRes, childAF);
			
			// add back to original parent
			ArtifactFragment orgParent = orgChildrenToParents.get(childAF);
			if (orgParent == null ) continue;
			orgParent.appendShownChild(childAF);
			//PartitionerSupport.addSinglyAddedChild(orgParent.getArt().elementRes, childAF);
		}
		
        // bring the layering to the new parent
		if (childrenLayers != null)
			LayersDPolicy.moveLayers(parentAF, oldParentAF, childrenLayers);

	}
}