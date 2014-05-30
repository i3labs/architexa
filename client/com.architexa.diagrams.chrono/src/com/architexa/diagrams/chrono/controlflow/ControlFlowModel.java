package com.architexa.diagrams.chrono.controlflow;

import java.util.ArrayList;
import java.util.List;

import com.architexa.diagrams.chrono.models.DiagramModel;
import com.architexa.diagrams.chrono.models.MemberModel;
import com.architexa.diagrams.chrono.util.MethodUtil;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.model.DerivedArtifact;

/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public abstract class ControlFlowModel extends DerivedArtifact {

	public static String PROPERTY_DELETE = "delete";

	private DiagramModel diagram;
	
	private String conditionalLabel;

	private List<ArtifactFragment> innerConditionalModels = new ArrayList<ArtifactFragment>();
	
	private ControlFlowModel outerConditionalModel = null;
	
	
	
	public ControlFlowModel(DiagramModel diagram, String conditionalLabel) {
		super(diagram.getArt());
		this.setConditionalLabel(conditionalLabel);
		this.setDiagram(diagram);
	}

	public void delete() {
		getDiagram().removeChildFromConditionalLayer(this);
	}

	public abstract List<MemberModel> getStatements();
	
	public ArtifactFragment getTopMostModel() {
		if(getStatements().size()==0) return null;

		MemberModel firstStmt =  getStatements().get(0);
		if(firstStmt.getFigure()!= null && firstStmt.getFigure().getParent()!=null) return firstStmt;

//		CollapseExpandButton button = getCorrespondingCollapseExpandButton(firstStmt);
//		if(button != null) return button.getMemberToHiddenMap().get(firstStmt);
		return null;
	}

	public ArtifactFragment getBottomMostModel() {
		if(getStatements().size()==0) return null;

		MemberModel lastStmt =  getStatements().get(getStatements().size()-1);
		if(lastStmt.getFigure()!= null && lastStmt.getFigure().getParent()!=null) return lastStmt;

//		CollapseExpandButton button = getCorrespondingCollapseExpandButton(lastStmt);
//		if(button != null) return button.getMemberToHiddenMap().get(lastStmt);
		return null;
	}

	public ArtifactFragment getRightMostModel() {
		MemberModel rightMostMethod = getRightMostMethodInBlock();
		if(rightMostMethod!=null) return rightMostMethod;

		if(getStatements().size()>0) {
			MemberModel firstStmt =  getStatements().get(0);
//			CollapseExpandButton button = getCorrespondingCollapseExpandButton(firstStmt);
//			if(button != null) return button.getHiddenChildList().get(0);
		}

//		if(getCollapseExpandButtons().size()>0 && !getCollapseExpandButtons().get(0).getHiddenChildList().isEmpty()) {
//			return getCollapseExpandButtons().get(0).getHiddenChildList().get(0);
//		}

		return null;
	}

	protected MemberModel getRightMostMethodInBlock() {
		MemberModel rightMost = null;
		int rightMostIndex = -1;
		for(MemberModel stmt : getStatements()) {
			MemberModel currentRightMost = MethodUtil.getRightMostResultingCall(stmt, getDiagram());
			if(currentRightMost.getFigure()==null || currentRightMost.getFigure().getParent()==null) continue;
			int index = getDiagram().getChildren().indexOf(currentRightMost.getInstanceModel());
			if(index > rightMostIndex) {
				rightMostIndex = index;
				rightMost = currentRightMost;
			}
		}
		return rightMost;
	}


	public DiagramModel getDiagram() {
		return diagram;
	}

	public void setDiagram(DiagramModel diagram) {
		this.diagram = diagram;
	}

	public String getConditionalLabel() {
		return conditionalLabel;
	}

	public void setConditionalLabel(String conditionalLabel) {
		this.conditionalLabel = conditionalLabel;
	}
	
	
	
	
	
	public void setOuterConditionalModel(ControlFlowModel cfModel) {
		outerConditionalModel = cfModel;
	}
	public ControlFlowModel getOuterConditionalModel() {
		return outerConditionalModel;
	}
	
	public List<ArtifactFragment> getInnerConditionalModels() {
		return innerConditionalModels;
	}

	public void addInnerConditionalModel(ControlFlowModel innerConditionalModel) {
		innerConditionalModels.add(innerConditionalModel);
	}
	
	public void removeInnerConditionalModel(ControlFlowModel cfm) {
		innerConditionalModels.remove(cfm);
	}
}
