package com.architexa.diagrams.strata.commands;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.model.ArtifactRel;
import com.architexa.diagrams.strata.cache.PartitionerSupport;
import com.architexa.diagrams.strata.model.CompositeLayer;
import com.architexa.diagrams.strata.model.Layer;
import com.architexa.diagrams.strata.model.StrataRootDoc;
import com.architexa.diagrams.strata.model.policy.ClosedContainerDPolicy;
import com.architexa.diagrams.strata.model.policy.LayersDPolicy;
import com.architexa.org.eclipse.gef.commands.Command;
import com.architexa.store.ReloRdfRepository;

public class MultiBreakCommand  extends Command {

	private final StrataRootDoc rootDoc;

	private List<ArtifactFragment> toBreakTargetAFs;
	private List<ArtifactFragment> brokenAFs = new ArrayList<ArtifactFragment>();
	private List<ArtifactFragment> parentAFs = new ArrayList<ArtifactFragment>();
	private Map<ArtifactFragment, List<ArtifactFragment>> brokenAFToChildrenMap = new LinkedHashMap<ArtifactFragment, List<ArtifactFragment>>();
	private Map<ArtifactFragment, List<Layer>> parentAFToOldLayers = new LinkedHashMap<ArtifactFragment, List<Layer>>();
	private Map<ArtifactFragment, List<ArtifactRel>> removedRelationshipsMap = new LinkedHashMap<ArtifactFragment, List<ArtifactRel>>();
	private ReloRdfRepository repo;
	
	public MultiBreakCommand(List<ArtifactFragment> toBreakTargetAFs, ReloRdfRepository repo, StrataRootDoc rootDoc) {
		super("Auto Break");
		this.repo = repo;
		this.toBreakTargetAFs = toBreakTargetAFs;
		this.rootDoc = rootDoc;
}
	
	@Override
	public void execute() { 
		// clear lists for redo
		parentAFs.clear();
		brokenAFs.clear();
		brokenAFToChildrenMap.clear();
		removedRelationshipsMap.clear();
		parentAFToOldLayers.clear();
		
		// copy org layers for undo
		for (ArtifactFragment strataAFToBreak : toBreakTargetAFs) {
			ArtifactFragment parentAF = strataAFToBreak.getParentArt();
			if (parentAF != null && (parentAF.getShownChildrenCnt() > 1 || parentAF instanceof StrataRootDoc)) {
				List<Layer> newLayers = LayersDPolicy.copyLayers(repo, parentAF);
				parentAFToOldLayers.put(parentAF, newLayers);
			}
			List<Layer> newLayers = LayersDPolicy.copyLayers(repo, strataAFToBreak);
			parentAFToOldLayers.put(strataAFToBreak, newLayers);
		}
		// break model and layers
		// layers need to be set to their original posistion to prevent re-layering
		for (ArtifactFragment toBreakAF : toBreakTargetAFs) {
			ArtifactFragment toBreakParentAF = toBreakAF.getParentArt();
			if (toBreakParentAF == null) continue;
			parentAFs.add(toBreakParentAF);
			brokenAFs.add(toBreakAF);
			List<ArtifactFragment> allShownChildren = ModelUtils.getAllNestedChildren(toBreakAF);
				
			// now delete child
			removedRelationshipsMap.put(toBreakAF, ModelUtils.removeAF(rootDoc, toBreakParentAF, toBreakAF));
	        
	        List<ArtifactFragment> toBreakShownChildren;
	        toBreakShownChildren = new ArrayList<ArtifactFragment>(ClosedContainerDPolicy.getShownChildren(toBreakAF));
	        brokenAFToChildrenMap.put(toBreakAF, toBreakShownChildren);
	        
	        // TODO: we should figure out and move the layering to the same place
	        if (toBreakShownChildren.size() > 1)
	        	LayersDPolicy.addLayers(toBreakParentAF, new ArrayList<Layer>(LayersDPolicy.getLayers(toBreakAF)));
        	
	        toBreakParentAF.moveChildren(toBreakShownChildren);
	    
	        // Set broken children to the same spot within the layers so we do not relayer anything
	        // must handle the case where an item with no siblings is broken and the case where an item with only one child is broken
	        if (toBreakShownChildren.size() == 1) {
	        	List<Layer> currLayers = new ArrayList<Layer>(LayersDPolicy.getLayers(toBreakParentAF));
		        ArtifactFragment afChild = toBreakShownChildren.get(0);
		        // find correct layer and get index
		        recursiveLayerReparent(currLayers, toBreakAF, toBreakParentAF, afChild);  
	            LayersDPolicy.setLayers(toBreakParentAF, new ArrayList<Layer>(currLayers));
	        } else {
	        	//we do not want to update layers here because this can be called asynchronously, so we instead schedule it to be added by the partitioner
	        	//List<Layer> currLayers = new ArrayList<Layer>(LayersDPolicy.getLayers(strataAFToBreak));
		        //LayersDPolicy.setLayers(parentAF, new ArrayList<Layer>(currLayers) );
	        	
		        // Notify Layers of change so we do not have to Partition the entire parent
		        PartitionerSupport.setRemovedSingleChildren(toBreakParentAF.getArt().elementRes, toBreakAF);

				// this way will make sure that breaking does not reLayer. We
				// may want to do this in the future but currently it causes
				// problems because of the order things break in
	        	// List<Layer> currLayers = new ArrayList<Layer>(LayersDPolicy.getLayers(strataAFToBreak));
		        //LayersDPolicy.setLayers(parentAF, new ArrayList<Layer>(currLayers) );
	        }
	        
	        // Add the child relationships back if they were deleted
	       for (ArtifactRel depRel : removedRelationshipsMap.get(toBreakAF)){
	        	   if (allShownChildren.contains(depRel.getDest()) || allShownChildren.contains(depRel.getSrc())){
	        		   depRel.getSrc().addSourceConnection(depRel);
	        		   depRel.getDest().addTargetConnection(depRel);
	        	   }
	        }

		}
	}
	
	private void recursiveLayerReparent(List<Layer> currLayers, ArtifactFragment strataAFToBreak, ArtifactFragment parentAF, ArtifactFragment afChild) {
		for (Layer layer : currLayers) {
        	if (layer instanceof CompositeLayer) {
        		 recursiveLayerReparent(((CompositeLayer) layer).getChildrenLayers(), strataAFToBreak, parentAF, afChild);
        		 continue;
        	} 
            if (!layer.contains(strataAFToBreak)) continue;
            int idx = layer.getChildren().indexOf(strataAFToBreak);
            layer.removeShownChild(strataAFToBreak);
            parentAF.removeShownChild(strataAFToBreak);
            layer.appendShownChild(afChild, idx);
            break;
          }		
	}

	@Override
	public void undo(){
		
		for (int i=brokenAFs.size()-1; i>=0; i--) {
			ArtifactFragment parentAF = parentAFs.get(i);
			ArtifactFragment brokenAF = brokenAFs.get(i);
			// add broken guy back to the correct parent
			List<ArtifactFragment> shownChildren = brokenAFToChildrenMap.get(brokenAF);	        
	        brokenAF.moveChildren(shownChildren);
	        ModelUtils.addAF(parentAF, brokenAF, removedRelationshipsMap.get(brokenAF));
		}
		for (int i=brokenAFs.size()-1; i>=0; i--) {
			ArtifactFragment parentAF = parentAFs.get(i);
			ArtifactFragment brokenAF = brokenAFs.get(i);
			if (parentAF.getShownChildrenCnt() > 1 || parentAF instanceof StrataRootDoc) {
				LayersDPolicy.setLayers(parentAF, new ArrayList<Layer>( parentAFToOldLayers.get(parentAF)));
			}
		    LayersDPolicy.setLayers(brokenAF, new ArrayList<Layer>( parentAFToOldLayers.get(brokenAF)));
		}
	}
	
}
