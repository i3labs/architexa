package com.architexa.diagrams.chrono.controlflow;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.architexa.diagrams.chrono.models.DiagramModel;
import com.architexa.diagrams.chrono.models.MemberModel;

/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public class IfBlockModel extends ControlFlowModel {


	public IfBlockModel(DiagramModel diagram, String conditionLabel) {
		super(diagram, conditionLabel);
	}


	private List<MemberModel> thenStmts = new ArrayList<MemberModel>();
	private Map<String,List<MemberModel>> elseIfStmts = new LinkedHashMap<String, List<MemberModel>>();
	private List<MemberModel> elseStmts = new ArrayList<MemberModel>();

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

	
	public List<MemberModel> getThenStmts() {
		return thenStmts;
	}
	public List<MemberModel> getElseStmts() {
		return elseStmts;
	}
	
	public void setThenStmts(List<MemberModel> thenStmts) {
		if(thenStmts==null) return;
//		for(MemberModel mbm : this.thenStmts)
//			mbm.removeConditionalBlock(this);
//		for(MemberModel mbm : thenStmts) 
//			mbm.addConditionalBlock(this);
		this.thenStmts = thenStmts;

//		origThenStmts = new ArrayList<MemberModel>(thenStmts);
		if(thenStmts.size()>0) {
//			CollapseExpandButton button = new CollapseExpandButton(SHOWING, getConditionLabel(), thenStmts, diagram);
//			addCollapseExpandButton(button);
//			addFigureListener(button);
//			getConditionLabel().addFigureListener(button);

//			this.ifButton = button;
//			mapButtonsToContainedStmts(thenStmts, button);
		} else {
//			getConditionLabel().setVisible(false);
		}
	};
	
	public void addElseIfStmts(String condition, List<MemberModel> stmts) {
		if(stmts==null || stmts.size()==0) return;

//		for(MemberModel mbm : stmts) 
//			mbm.addConditionalBlock(this);
		elseIfStmts.put(condition, stmts);
//		origElseIfStmts.put(condition, new ArrayList<MemberModel>(stmts));

//		Label elseIfLabel = new Label("     [ else if " + condition + " ]");
//		elseIfLabel.setBorder(new MarginBorder(labelMargin));

		// If all of the previous then and else if statements in the conditional block
		// this represents are not invocations, this section will be the first one to
		// be displayed in the diagram and therefore the first one to have a button
//		CollapseExpandButton button = new CollapseExpandButton(SHOWING, elseIfLabel, stmts, diagram);
//		addCollapseExpandButton(button);
//		addFigureListener(button);
//		elseIfLabel.addFigureListener(button);

//		ElseBlock elseIfBlock = new ElseBlock(elseIfLabel, button, stmts,condition);
//		elseBlocks.add(elseIfBlock);

//		mapButtonsToContainedStmts(stmts, button);
	}
	
	
	public void setElseStmts(List<MemberModel> elseStmts) {
		if(elseStmts==null) return;
//		for(MemberModel mbm : this.elseStmts) mbm.removeConditionalBlock(this);
//		for(MemberModel mbm : elseStmts) mbm.addConditionalBlock(this);
		this.elseStmts = elseStmts;
//		origElseStmts = new ArrayList<MemberModel>(elseStmts);

//		if(elseStmts.size()>0) {
//
//			Label elseLabel = new Label("     [ else ]");
//			elseLabel.setBorder(new MarginBorder(labelMargin));
//
//			// If all of the previous then and else if statements in the conditional block
//			// this represents are not invocations, this section will be the first one to
//			// be displayed in the diagram and therefore the first one to have a button
//			CollapseExpandButton button = new CollapseExpandButton(SHOWING, elseLabel, elseStmts, diagram);
//			addCollapseExpandButton(button);
//			addFigureListener(button);
//			elseLabel.addFigureListener(button);
//
//			ElseBlock elseBlock = new ElseBlock(elseLabel, button, elseStmts);
//			elseBlocks.add(elseBlock);
//			mapButtonsToContainedStmts(elseStmts, button);
//		}
	}


	public List<MemberModel> getIfStmts() {
		List<MemberModel> allelseIfStmts = new ArrayList<MemberModel>();
		for(List<MemberModel> stmtsForAnElseIf : elseIfStmts.values()) {
			allelseIfStmts.addAll(stmtsForAnElseIf);
		}
		return allelseIfStmts;
	}
}
