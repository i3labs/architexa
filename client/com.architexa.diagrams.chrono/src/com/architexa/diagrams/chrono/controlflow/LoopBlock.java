package com.architexa.diagrams.chrono.controlflow;

import java.util.ArrayList;
import java.util.List;


import com.architexa.diagrams.chrono.models.DiagramModel;
import com.architexa.diagrams.chrono.models.HiddenNodeModel;
import com.architexa.diagrams.chrono.models.MemberModel;
import com.architexa.diagrams.chrono.models.MethodBoxModel;
import com.architexa.diagrams.chrono.models.NodeModel;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.geometry.Point;
import com.architexa.org.eclipse.draw2d.geometry.Rectangle;

/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public class LoopBlock extends ControlFlowBlock {

	private List<MemberModel> loopStmts = new ArrayList<MemberModel>();
	private List<MemberModel> origLoopStmts ;

	public LoopBlock(String condition, DiagramModel diagram) {
		super(condition, diagram);

		setType(ControlFlowTypeLabel.LOOP, condition);
		add(getTypeLabel());
		add(getConditionLabel());
	}
	/*
	 * @Method to add method box to statement list in the correct order of the call made
	 */
	public void addLoopStmt(MethodBoxModel loopBox) {
		//find the index where the method box has to be placed in the loopStmts
		List<ArtifactFragment> Children=loopBox.getParent().getChildren();
		List<MemberModel> childList=new ArrayList<MemberModel>();

		if(loopStmts.isEmpty()){
			loopStmts.add(loopBox);
			loopBox.addConditionalBlock(this);
			return;
		}
		if(loopStmts.contains(loopBox)) return;

		//making the list of all children hidden and non hidden
		for(ArtifactFragment child:Children){
			if(child instanceof MemberModel)
				childList.add((MemberModel) child);
			else if(child instanceof HiddenNodeModel){
				for(MemberModel hidden: ((HiddenNodeModel) child).getControlFlowMethodsHiding()){
					childList.add(hidden);
				}
			}
		}
		for(MemberModel child:childList){
			boolean added=false;
			for(MemberModel stmt:getStatements()){
				if(child.equals(stmt)){
					if(childList.indexOf(child)>childList.indexOf(loopBox)){
						loopStmts.add(getStatements().indexOf(stmt), loopBox);
						added=true;
						break;
					}
					//find if the last element in statement list
					if(getStatements().indexOf(stmt)==getStatements().size()-1){
						loopStmts.add(loopBox); //add to the last index
						added=true;
					}
					break;
				}
			}
			if(added){
				break;
			}
		}
		loopBox.addConditionalBlock(this);
	}

	public void setLoopStmts(List<MemberModel> loopStmts) {
		for(MemberModel mbm : this.loopStmts) {
			mbm.removeConditionalBlock(this);
		}
		for(MemberModel mbm : loopStmts) {
			mbm.addConditionalBlock(this);
		}
		this.loopStmts = loopStmts;
		this.origLoopStmts = new ArrayList<MemberModel>(loopStmts);
	}

	@Override
	public List<MemberModel> getStatements() {
		return loopStmts;
	}

	@Override
	public List<MemberModel> getOrigStatements() {
		return origLoopStmts;
	}

	@Override
	public void statementDeleted(NodeModel deleted) {
		if(loopStmts.contains(deleted)) {
			loopStmts.remove(deleted);
		} else {
			return;
		}
		super.statementDeleted(deleted);
	}

	@Override
	public void statementAdded(MemberModel added){
		if(!loopStmts.contains(added)) //TODO add support for fields
			addInCorrectLocation(added);
		super.statementAdded(added);
	}

	@Override
	public void figureMoved(IFigure source) {

//		IFigure topLeftFig = getTopMostFigure();
//		IFigure bottomMostFig = getBottomMostFigure();
//		IFigure rightMostFig = getRightMostFigure();
//		IFigure bottomMostFig = ControlFlowEditPart.getFigureOfModel(getModel().getBottomMostModel());//fig.getTopMostFigure();
//		IFigure topLeftFig = ControlFlowEditPart.getFigureOfModel(getModel().getTopMostModel());//fig.getBottomMostFigure();
//		IFigure rightMostFig = ControlFlowEditPart.getFigureOfModel(getModel().getRightMostModel());//fig.getRightMostFigure();

		
		IFigure topLeftFig = getTopLeftFigureListeningTo();
		IFigure bottomMostFig = getBottomFigureListeningTo();
		IFigure rightMostFig = getRightFigureListeningTo();
		
		
		if(topLeftFig==null || bottomMostFig==null || rightMostFig==null) return;

		int typeLabelWidth = getTypeLabel().getSize().width;
		Point topLeft = new Point(topLeftFig.getBounds().getTopLeft().x - MARGIN - typeLabelWidth/2, topLeftFig.getBounds().getTop().y - getTypeLabel().getSize().height - getConditionLabel().getSize().height/2 + DiagramModel.TOP_MARGIN);
		Point bottomRight = new Point(rightMostFig.getBounds().getRight().x + MARGIN + typeLabelWidth/2, bottomMostFig.getBounds().getBottom().y + MARGIN + DiagramModel.TOP_MARGIN);

		removeAnyOverlap(topLeft, bottomRight);

		Rectangle boundsRect = new Rectangle(topLeft, bottomRight);
		setBounds(boundsRect);

		updateOuterBlocks(source);
		updateLoopStmts(source);
	}

	private void addInCorrectLocation(MemberModel added) {
		if (loopStmts.contains(added))	return;
		for (MemberModel model : origLoopStmts) {
			if (model.getCharStart() == added.getCharStart()
					&& model.getCharEnd() == added.getCharEnd()) {
				int index = origLoopStmts.indexOf(model);
				if (index <= loopStmts.size())
					loopStmts.add(index, added);
				else
					loopStmts.add(added);
				return;
			}
		}
	}

	private void updateLoopStmts(IFigure source) {
		if(loopStmts.isEmpty()) return;
		if (!(loopStmts.get(0).getParent() instanceof MemberModel)) return;
		MemberModel parent = (MemberModel) loopStmts.get(0).getParent();
		if(parent == null){
			HiddenNodeModel hiddenModel = getCorrespondingCollapseExpandButton(loopStmts.get(0)).getMemberToHiddenMap().get(loopStmts.get(0));
			parent = (MemberModel) hiddenModel.getParentArt();
		}
		if(parent == null || !parent.getFigure().equals(source)) return;
		for(MemberModel child : parent.getMemberChildren()){
			addInCorrectLocation(child);
		}
	}

}
