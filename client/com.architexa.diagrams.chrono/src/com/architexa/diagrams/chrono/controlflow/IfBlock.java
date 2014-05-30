package com.architexa.diagrams.chrono.controlflow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.architexa.diagrams.chrono.models.DiagramModel;
import com.architexa.diagrams.chrono.models.HiddenNodeModel;
import com.architexa.diagrams.chrono.models.MemberModel;
import com.architexa.diagrams.chrono.models.NodeModel;
import com.architexa.org.eclipse.draw2d.ConnectionLocator;
import com.architexa.org.eclipse.draw2d.Figure;
import com.architexa.org.eclipse.draw2d.Graphics;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.Label;
import com.architexa.org.eclipse.draw2d.MarginBorder;
import com.architexa.org.eclipse.draw2d.Polyline;
import com.architexa.org.eclipse.draw2d.PolylineConnection;
import com.architexa.org.eclipse.draw2d.PositionConstants;
import com.architexa.org.eclipse.draw2d.geometry.Point;
import com.architexa.org.eclipse.draw2d.geometry.Rectangle;

/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public class IfBlock extends ControlFlowBlock {

	private List<MemberModel> thenStmts = new ArrayList<MemberModel>();
	private Map<String,List<MemberModel>> elseIfStmts = new LinkedHashMap<String, List<MemberModel>>();
	private List<MemberModel> elseStmts = new ArrayList<MemberModel>();

	private List<MemberModel> origThenStmts;
	private Map<String, List<MemberModel>> origElseIfStmts = new LinkedHashMap<String, List<MemberModel>>();
	private List<MemberModel> origElseStmts;
	private CollapseExpandButton ifButton;

	private List<ElseBlock> elseBlocks = new ArrayList<ElseBlock>();
	private List<Polyline> elseDivisionLines = new ArrayList<Polyline>();

	private DiagramModel diagram;

	public IfBlock(String condition, DiagramModel diagram) {
		super(condition, diagram);
		this.diagram = diagram;
		setType(ControlFlowTypeLabel.IF, condition);
		add(getTypeLabel());
		add(getConditionLabel());
	}

	public int getNumberOfStmts() {
		int num = thenStmts.size();
		num = num + elseIfStmts.values().size();
		num = num + elseStmts.size();
		return num;
	}

	public void setThenStmts(List<MemberModel> thenStmts) {
		if(thenStmts==null) return;
		for(MemberModel mbm : this.thenStmts)mbm.removeConditionalBlock(this);
		for(MemberModel mbm : thenStmts) mbm.addConditionalBlock(this);
		this.thenStmts = thenStmts;

		origThenStmts = new ArrayList<MemberModel>(thenStmts);
		if(thenStmts.size()>0) {
//			CollapseExpandButton button = new CollapseExpandButton(SHOWING, getConditionLabel(), thenStmts, diagram);
//			addCollapseExpandButton(button);
//			addFigureListener(button);
//			getConditionLabel().addFigureListener(button);

//			this.ifButton = button;
//			mapButtonsToContainedStmts(thenStmts, button);
		} else {
			getConditionLabel().setVisible(false);
		}
	}

	/*
	 * @updates the control flow block
	 */
	@Override
	public void statementAdded(MemberModel model){
		addInCorrectLocation(model);
		super.statementDeleted(model);
	}

	// find the correct index for the invocation to be put in the list
	private void addInCorrectLocation(MemberModel model) {
		if(thenStmts.contains(model) || elseStmts.contains(model)) return;
		for(String str : elseIfStmts.keySet()){
			if(elseIfStmts.get(str).contains(model)) return;
		}
		int index;
		for(MemberModel origModel : origThenStmts){
			if(origModel.getCharStart() == model.getCharStart() && origModel.getCharEnd() == model.getCharEnd()){
				index = origThenStmts.indexOf(origModel);
				if(index <= thenStmts.size())
					thenStmts.add(index,model);
				else
					thenStmts.add(model);
			}
		}

		for(MemberModel origModel : origElseStmts){
			if(origModel.getCharStart() == model.getCharStart() && origModel.getCharEnd() == model.getCharEnd()){
				index = origElseStmts.indexOf(origModel);
				if(index <= elseStmts.size())
					elseStmts.add(index,model);
				else
					elseStmts.add(model);
			}
		}

		for(String str : origElseIfStmts.keySet()){
			for(MemberModel origModel : origElseIfStmts.get(str)){
				if(origModel.getCharStart() == model.getCharStart() && origModel.getCharEnd() == model.getCharEnd()){
					index = origElseIfStmts.get(str).indexOf(origModel);
					if(index <= elseIfStmts.get(str).size())
						elseIfStmts.get(str).add(index, model);
					else
						elseIfStmts.get(str).add(model);
				}
			}
		}
	}

	public void addElseIfStmts(String condition, List<MemberModel> stmts) {
		if(stmts==null || stmts.size()==0) return;

		for(MemberModel mbm : stmts) mbm.addConditionalBlock(this);
		elseIfStmts.put(condition, stmts);
		origElseIfStmts.put(condition, new ArrayList<MemberModel>(stmts));

		Label elseIfLabel = new Label("     [ else if " + condition + " ]");
		elseIfLabel.setBorder(new MarginBorder(labelMargin));

		// If all of the previous then and else if statements in the conditional block
		// this represents are not invocations, this section will be the first one to
		// be displayed in the diagram and therefore the first one to have a button
//		CollapseExpandButton button = new CollapseExpandButton(SHOWING, elseIfLabel, stmts, diagram);
//		addCollapseExpandButton(button);
//		addFigureListener(button);
//		elseIfLabel.addFigureListener(button);

		ElseBlock elseIfBlock = new ElseBlock(elseIfLabel, null, stmts,condition);
		elseBlocks.add(elseIfBlock);

//		mapButtonsToContainedStmts(stmts, button);
	}

	public void setElseStmts(List<MemberModel> elseStmts) {
		if(elseStmts==null) return;
		for(MemberModel mbm : this.elseStmts) mbm.removeConditionalBlock(this);
		for(MemberModel mbm : elseStmts) mbm.addConditionalBlock(this);
		this.elseStmts = elseStmts;
		origElseStmts = new ArrayList<MemberModel>(elseStmts);

		if(elseStmts.size()>0) {

			Label elseLabel = new Label("     [ else ]");
			elseLabel.setBorder(new MarginBorder(labelMargin));

			// If all of the previous then and else if statements in the conditional block
			// this represents are not invocations, this section will be the first one to
			// be displayed in the diagram and therefore the first one to have a button
//			CollapseExpandButton button = new CollapseExpandButton(SHOWING, elseLabel, elseStmts, diagram);
//			addCollapseExpandButton(button);
//			addFigureListener(button);
//			elseLabel.addFigureListener(button);

			ElseBlock elseBlock = new ElseBlock(elseLabel, null, elseStmts);
			elseBlocks.add(elseBlock);
//			mapButtonsToContainedStmts(elseStmts, button);
		}
	}

	public CollapseExpandButton getButton(){
		return ifButton;
	}

	public List<MemberModel> getOrigThenStmts(){
		return origThenStmts;
	}

	public List<MemberModel> getOrigElseStmts(){
		return origElseStmts;
	}

	public Map<String, List<MemberModel>> getOrigElseIfStmts(){
		return origElseIfStmts;
	}

	public List<MemberModel> getThenStmts() {
		return thenStmts;
	}

	public List<MemberModel> getElseStmts() {
		return elseStmts;
	}

	public List<ElseBlock> getElseBlocks() {
		return elseBlocks;
	}

	public List<Polyline> getElseDivisionLines() {
		return elseDivisionLines;
	}

	public Map<String, List<MemberModel>> getElseIfStmts(){
		return elseIfStmts;
	}
	@Override
	public List<MemberModel> getStatements() {
		List<MemberModel> allStmts = new ArrayList<MemberModel>();
		allStmts.addAll(thenStmts);
		for(List<MemberModel> stmtsForAnElseIf : elseIfStmts.values()) {
			allStmts.addAll(stmtsForAnElseIf);
		}
		allStmts.addAll(elseStmts);

		return allStmts;
	}

	@Override
	public List<MemberModel> getOrigStatements() {
		List<MemberModel> allStmts = new ArrayList<MemberModel>();
		allStmts.addAll(origThenStmts);
		for (List<MemberModel> stmtsForAnElseIf : origElseIfStmts.values()) {
			allStmts.addAll(stmtsForAnElseIf);
		}
		allStmts.addAll(origElseStmts);
		return allStmts;
	}

	@Override
	public void statementDeleted(NodeModel deleted) {
		if(thenStmts.contains(deleted)) {
			thenStmts.remove(deleted);
		} else if(elseStmts.contains(deleted)) {
			elseStmts.remove(deleted);
		}
		Map<String, List<MemberModel>> map=new HashMap<String, List<MemberModel>>();
		map.putAll(elseIfStmts);
		for(String string:map.keySet()){
			if(elseIfStmts.get(string).contains(deleted)){
				elseIfStmts.get(string).remove(deleted);// we do not remove the whole string to list map now
			}
		}
		super.statementDeleted(deleted);
		addFigListnerToFirstElseIf();
	}

	private void mapButtonsToContainedStmts(List<MemberModel> stmts, CollapseExpandButton button) {
		List<ControlFlowBlock> innerBlocks = new ArrayList<ControlFlowBlock>();
		buttonToContainedInnerBlocks.put(button, innerBlocks);
		for(ControlFlowBlock innerBlock : getInnerConditionalBlocks()) {
			addInnerBlocksToMap(stmts, innerBlock, innerBlocks);
		}
	}

	private void addInnerBlocksToMap(List<MemberModel> stmts, ControlFlowBlock block, List<ControlFlowBlock> innerBlocks) {
		if(stmts.containsAll(block.getStatements())) {
			innerBlocks.add(block);
			block.setOuterIsCollapsed();
		}
		for(ControlFlowBlock innerBlock : block.getInnerConditionalBlocks()) {
			addInnerBlocksToMap(stmts, innerBlock, innerBlocks);
		}
	}

	public void clearElseBlocks(){
		elseBlocks.clear();
	}

	@Override
	public void figureMoved(IFigure source) {
		try{
//			IFigure topLeftFig = getTopMostFigure();
//			IFigure bottomMostFig = getBottomMostFigure();
//			IFigure rightMostFig = getRightMostFigure();
			
//			ControlFlowModel model = getModel();
//			IFigure bottomMostFig = ControlFlowEditPart.getFigureOfModel(model.getBottomMostModel());//fig.getTopMostFigure();
//			IFigure topLeftFig = ControlFlowEditPart.getFigureOfModel(model.getTopMostModel());//fig.getBottomMostFigure();
//			IFigure rightMostFig = ControlFlowEditPart.getFigureOfModel(model.getRightMostModel());//fig.getRightMostFigure();
			IFigure topLeftFig = getTopLeftFigureListeningTo();
			IFigure bottomMostFig = getBottomFigureListeningTo();
			IFigure rightMostFig = getRightFigureListeningTo();
			
			if(topLeftFig==null || bottomMostFig==null || rightMostFig==null) return;

			int typeLabelWidth = getTypeLabel().getSize().width;
			Point topLeft = new Point(topLeftFig.getBounds().getTopLeft().x - MARGIN - typeLabelWidth, topLeftFig.getBounds().getTop().y - getTypeLabel().getSize().height - getConditionLabel().getSize().height/2 + DiagramModel.TOP_MARGIN);
			int rightMostX = Math.max(rightMostFig.getBounds().getRight().x, getConditionLabel().getBounds().right());
			Point bottomRight = new Point(rightMostX + MARGIN + typeLabelWidth, bottomMostFig.getBounds().getBottom().y + MARGIN + DiagramModel.TOP_MARGIN);

			removeAnyOverlap(topLeft, bottomRight);

			Rectangle boundsRect = new Rectangle(topLeft, bottomRight);
			setBounds(boundsRect);

			if(elseBlocks.isEmpty()) {
				updateOuterBlocks(source);
				updateStmts(source);
				return;
			}

			IFigure lastBeforeElse;
			if(thenStmts.size()>0) {
				MemberModel lastThenStmt = thenStmts.get(thenStmts.size()-1);
				lastBeforeElse = lastThenStmt.getFigure();
				if(lastBeforeElse.getParent() == null){
//					CollapseExpandButton button = getCorrespondingCollapseExpandButton(lastThenStmt);
//					if(button == null) return;
//					lastBeforeElse = button.getMemberToHiddenMap().get(lastThenStmt).getFigure();
				}
			} else {
				IFigure topOfBlock = new Figure();
				topOfBlock.setSize(0,0);
				topOfBlock.setLocation(new Point(getBounds().getCopy().x, getBounds().getCopy().y - DiagramModel.TOP_MARGIN));
				lastBeforeElse = topOfBlock;
			}

			for(Polyline elseIfLine : elseDivisionLines) remove(elseIfLine);
			elseDivisionLines.clear();

			for(ElseBlock elseBlock : elseBlocks) {

				if(elseBlock.getStmts().size()==0) continue;

				IFigure firstInElse = null;
				MemberModel firstStmt = elseBlock.getStmts().get(0);
				int raiseDivisionLineAmt = 0;
				if(thenStmts.size()==0 && elseBlocks.indexOf(elseBlock)==0) {
					firstInElse = lastBeforeElse;
				} else {
					firstInElse = elseBlock.getStmts().get(0).getFigure();
					if(firstInElse.getParent()==null){
//						HiddenNodeModel hiddenModel = elseBlock.getButton().getMemberToHiddenMap().get(firstStmt);
//						if(hiddenModel == null) return;
//						firstInElse = hiddenModel.getFigure();
					}
					// raising the division line so that the else or else if 
					// label does not overlap a method call connection's label
					raiseDivisionLineAmt = elseBlock.getLabel().getSize().height/2;
				}
				int midPoint = (lastBeforeElse.getBounds().getBottom().y + DiagramModel.TOP_MARGIN + firstInElse.getBounds().getTop().y + DiagramModel.TOP_MARGIN)/2;
				PolylineConnection divisionLine = new PolylineConnection();
				divisionLine.setLineStyle(Graphics.LINE_DASH);

				divisionLine.setStart(new Point(getBounds().getLeft().x, midPoint-raiseDivisionLineAmt));
				divisionLine.setEnd(new Point(getBounds().getRight().x, midPoint-raiseDivisionLineAmt));

				ConnectionLocator elseLabelLocator = new ConnectionLocator(divisionLine, ConnectionLocator.SOURCE);
				elseLabelLocator.setRelativePosition(PositionConstants.SOUTH_EAST);
				divisionLine.add(elseBlock.getLabel(), elseLabelLocator);

				elseDivisionLines.add(divisionLine);
				add(divisionLine);

				MemberModel lastMethodInBlock = elseBlock.getStmts().get(elseBlock.getStmts().size()-1);
				//making sure that last method before the next else is last of this else.
				if(lastMethodInBlock.getFigure().getParent() != null) 
					lastBeforeElse = lastMethodInBlock.getFigure();
				else {
					if(getCorrespondingCollapseExpandButton(lastMethodInBlock)!=null)
						lastBeforeElse = getCorrespondingCollapseExpandButton(lastMethodInBlock).getMemberToHiddenMap().get(lastMethodInBlock).getFigure();
					else {
						elseDivisionLines.remove(divisionLine);
						remove(divisionLine);
					}
				}
			}

			updateOuterBlocks(source);
			updateStmts(source);
		}catch (Throwable e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	private void updateStmts(IFigure source) {
		// get parent declaration
		if(thenStmts.isEmpty()) return;
		if (!(thenStmts.get(0).getParent() instanceof MemberModel)) return;
		
		MemberModel parent = (MemberModel) thenStmts.get(0).getParent();
		if(parent == null){
			HiddenNodeModel hiddenModel = getCorrespondingCollapseExpandButton(thenStmts.get(0)).getMemberToHiddenMap().get(thenStmts.get(0));
			parent = (MemberModel) hiddenModel.getParentArt();
		}
		if(parent == null || !parent.getFigure().equals(source)) return;

		// for all the member children add back to the respective list
		for(MemberModel child : parent.getMemberChildren()){
			addInCorrectLocation(child);
		}
	}

	public void addFigListnerToFirstElseIf(){
		if(!elseIfStmts.isEmpty()){
			for(String str:elseIfStmts.keySet()){
				if(!elseIfStmts.get(str).isEmpty())
					elseIfStmts.get(str).get(0).getFigure().addFigureListener(this);
			}
		}

	}

	public class ElseBlock {

		private Label label;
		private CollapseExpandButton button;
		List<MemberModel> stmts;
		private String condition="";
		ElseBlock(Label label, CollapseExpandButton button, List<MemberModel> stmts) {
			this.label = label;
			this.button = button;
			this.stmts = stmts;
		}

		ElseBlock(Label label, CollapseExpandButton button, List<MemberModel> stmts, String condition) {
			this.label = label;
			this.button = button;
			this.stmts = stmts;
			this.condition=condition;
		}

		public String getCondition(){
			return condition;
		}
		public Label getLabel() {
			return label;
		}

		public CollapseExpandButton getButton() {
			return button;
		}

		public List<MemberModel> getStmts() {
			return stmts;
		}

		public boolean blockIsShowing() {
			return getButton().sectionIsShowing();
		}
	}

}
