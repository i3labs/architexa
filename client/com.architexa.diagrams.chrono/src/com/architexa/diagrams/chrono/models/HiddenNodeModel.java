package com.architexa.diagrams.chrono.models;

import java.util.ArrayList;
import java.util.List;

import com.architexa.diagrams.chrono.controlflow.CollapseExpandButton;
import com.architexa.diagrams.chrono.controlflow.ControlFlowBlock;
import com.architexa.diagrams.chrono.figures.HiddenFigure;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.model.DerivedArtifact;


/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public class HiddenNodeModel extends DerivedArtifact {

	private List<ControlFlowBlock> conditionalBlocksContainedIn;

	private boolean visible = true;
	private HiddenFigure figure;
	private String tooltip = "";
	private HiddenNodeModel partner=null;

	public HiddenNodeModel(boolean visible, NodeModel parent, String tooltip, List<ControlFlowBlock> conditionalBlocks) {
		super(parent.getArt());
		this.visible = visible;
		if(tooltip!=null) this.tooltip = tooltip;
		conditionalBlocksContainedIn = new ArrayList<ControlFlowBlock>(conditionalBlocks);
	}

	public HiddenNodeModel(boolean visible) {
		super(null);
		this.visible = visible;
	}

	public void setPartner(HiddenNodeModel partner){
		if(partner!=null){
			this.partner=partner;
		}
	}

	public HiddenNodeModel getPartner(){
		return partner;
	}

	public boolean getVisible() {
		return visible;
	}

	public void setFigure(HiddenFigure figure) {
		this.figure = figure;
	}

	public HiddenFigure getFigure() {
		return figure;
	}

	public void setTooltip(String tooltip) {
		this.tooltip = tooltip;
	}

	public String getTooltip() {
		return tooltip;
	}

	public void addChild(NodeModel child) {
		super.appendShownChild(child);
	}


	public List<ArtifactFragment> getChildren() {
		return super.getShownChildren();
	}

	public void addConditionalBlock(ControlFlowBlock block) {
		if(!conditionalBlocksContainedIn.contains(block))
			conditionalBlocksContainedIn.add(block);
	}

	public void removeConditionalBlock(ControlFlowBlock block) {
		conditionalBlocksContainedIn.remove(block);
	}

	public List<ControlFlowBlock> getConditionalBlocksContainedIn() {
		return conditionalBlocksContainedIn;
	}

	public List<MemberModel> getControlFlowMethodsHiding() {
		List<MemberModel> containedMethods = new ArrayList<MemberModel>();
		for (ControlFlowBlock block : conditionalBlocksContainedIn) {
			for (CollapseExpandButton button : block.getCollapseExpandButtons()) {
				for (MemberModel member : button.getMemberToHiddenMap().keySet()) {
					if (!containedMethods.contains(member)
							&& this.equals(button.getMemberToHiddenMap().get(member))) {
						containedMethods.add(member);
					}
				}
			}
		}
		return containedMethods;
	}

}
