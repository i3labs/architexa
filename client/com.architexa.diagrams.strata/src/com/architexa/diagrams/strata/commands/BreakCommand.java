/**
 * 
 */
package com.architexa.diagrams.strata.commands;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.model.ArtifactRel;
import com.architexa.diagrams.strata.StrataPlugin;
import com.architexa.diagrams.strata.cache.PartitionerSupport;
import com.architexa.diagrams.strata.model.Layer;
import com.architexa.diagrams.strata.model.StrataRootDoc;
import com.architexa.diagrams.strata.model.policy.ClosedContainerDPolicy;
import com.architexa.diagrams.strata.model.policy.LayersDPolicy;
import com.architexa.diagrams.strata.ui.BreakAction;
import com.architexa.org.eclipse.gef.commands.Command;

public final class BreakCommand extends Command {
	public static final Logger logger = StrataPlugin.getLogger(BreakCommand.class);
	

	private final ArtifactFragment parentAF;
	private final ArtifactFragment toBreakAF;

	private final StrataRootDoc rootDoc;

	private List<ArtifactFragment> shownChildren;
	private ArrayList<Layer> shownLayers;

	private List<ArtifactRel> removedRelationships;


	public BreakCommand(ArtifactFragment parentAF, ArtifactFragment strataAFToBreak) {
		super(BreakAction.CommandName);
		this.parentAF = parentAF;
		this.toBreakAF = strataAFToBreak;
		this.rootDoc = (StrataRootDoc) parentAF.getRootArt();
	}

	private String getStr(ArtifactFragment strataAF) {
		if (strataAF == null) return "{null}";
		return strataAF + "[" + strataAF.getClass() + "]";
	}

	@Override
	public void execute() { 
	    if (parentAF == null) {
	        logger.info("Moving: " + getStr(toBreakAF) + " to top level");
	       return;
	    }
	    
        logger.info("Breaking: " + getStr(toBreakAF) + " and placing in: " + getStr(parentAF));
        List<ArtifactFragment> allShownChildren = ModelUtils.getAllNestedChildren(toBreakAF);
	     
        // now delete child
        removedRelationships = ModelUtils.removeAF(rootDoc, parentAF, toBreakAF);
        
        // bring the layering to the parent
        shownLayers = new ArrayList<Layer>(LayersDPolicy.getLayers(toBreakAF));
        LayersDPolicy.moveLayers(toBreakAF, parentAF, shownLayers);

        // bring the children to the parent
        shownChildren = new ArrayList<ArtifactFragment>(ClosedContainerDPolicy.getShownChildren(toBreakAF));
        parentAF.moveChildren(shownChildren);
        
        // Notify Layers of change so we do not have to Partition the entire parent
        PartitionerSupport.setRemovedSingleChildren(parentAF.getArt().elementRes, toBreakAF);
        
        // Add the child relationships back if they were deleted
		for (ArtifactRel depRel : removedRelationships) {
        	   if (allShownChildren.contains(depRel.getDest()) || allShownChildren.contains(depRel.getSrc())){
        		   depRel.getSrc().addSourceConnection(depRel);
        		   depRel.getDest().addTargetConnection(depRel);
        	   }
        }
	}
	

	@Override
	public void undo(){
        LayersDPolicy.moveLayers(parentAF, toBreakAF, shownLayers);
		
		ModelUtils.addAF(parentAF, toBreakAF, removedRelationships);

		toBreakAF.moveChildren(shownChildren);
		
		// update layers for the parent
		PartitionerSupport.addSinglyAddedChild(parentAF.getArt().elementRes, toBreakAF);
	}
}