/**
 * 
 */
package com.architexa.diagrams.strata.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.model.ArtifactRel;
import com.architexa.diagrams.strata.StrataPlugin;
import com.architexa.diagrams.strata.cache.PartitionerSupport;
import com.architexa.diagrams.strata.model.Layer;
import com.architexa.diagrams.strata.model.StrataRootDoc;
import com.architexa.diagrams.strata.model.policy.LayersDPolicy;
import com.architexa.diagrams.strata.parts.StrataArtFragEditPart;
import com.architexa.diagrams.strata.ui.FocusAction;
import com.architexa.org.eclipse.gef.commands.Command;

public final class FocusCommand extends Command {

	public static final Logger logger = StrataPlugin.getLogger(FocusCommand.class);
	
	private final StrataRootDoc rootDoc;

	private final List<ArtifactFragment> strataAFToRemoveList = new ArrayList<ArtifactFragment>();
	private final List<ArtifactFragment> parentAFList  = new ArrayList<ArtifactFragment>();
	private List<ArtifactRel> removedRels = new ArrayList<ArtifactRel>();
	
	private List<StrataArtFragEditPart> allObjectEPs;
	private List<ArtifactFragment> allParentAFList  = new ArrayList<ArtifactFragment>();

	//store layer information so we can undo correctly
	private Map<ArtifactFragment, List<Layer>> parentAFToLayersMap = new HashMap<ArtifactFragment, List<Layer>>();

	public FocusCommand(List<StrataArtFragEditPart> allObjectEPs, List<StrataArtFragEditPart> selectedEPs, StrataRootDoc rootDoc) {
		super(FocusAction.FocusAction_CommandName);
		this.allObjectEPs = allObjectEPs;
		
		this.rootDoc = rootDoc;
		for (StrataArtFragEditPart safep : allObjectEPs) {
			parentAFList.add( safep.getParentTAFEP().getArtFrag()); 
			strataAFToRemoveList.add(safep.getArtFrag());
		}
		allParentAFList.addAll(parentAFList);
		for (StrataArtFragEditPart safep : selectedEPs) {
			allParentAFList.add(safep.getParentTAFEP().getArtFrag()); 
		}
	}

	@Override
	public void execute() { 
		for (int i=0; i<allObjectEPs.size(); i++) {
			ArtifactFragment parentAF = parentAFList.get(i);
			ArtifactFragment strataAFToRemove = strataAFToRemoveList.get(i);
			PartitionerSupport.setRemovedSingleChildren(parentAF.getArt().elementRes, strataAFToRemove);
			
			logger.info("Deleting: " + strataAFToRemove + " from: " + parentAF);
			removedRels.addAll(ModelUtils.removeAF(rootDoc, parentAF, strataAFToRemove));
		}
		for (int i=0; i<allParentAFList.size(); i++) {
			ArtifactFragment parentAF = allParentAFList.get(i);
			parentAFToLayersMap.put(parentAF, LayersDPolicy.copyLayers(rootDoc.getRepo(), parentAF));
		}	
	}

	@Override
	public void undo(){
		for (int i=allObjectEPs.size()-1; i>=0; i--) {
			ArtifactFragment parentAF = parentAFList.get(i);
			ArtifactFragment strataAFToRemove = strataAFToRemoveList.get(i);
		
			parentAF.appendShownChild(strataAFToRemove);
			PartitionerSupport.addSinglyAddedChild(parentAF.getArt().elementRes, strataAFToRemove);
		}
		rootDoc.addRelationships(removedRels);
		for (int i=allParentAFList.size()-1; i>=0; i--) {
			ArtifactFragment parentAF = allParentAFList.get(i);
			LayersDPolicy.setLayers(parentAF, parentAFToLayersMap.get(parentAF));
		}
	}

}