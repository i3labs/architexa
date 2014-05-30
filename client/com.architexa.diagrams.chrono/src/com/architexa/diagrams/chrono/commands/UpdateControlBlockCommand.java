package com.architexa.diagrams.chrono.commands;

import java.util.ArrayList;
import java.util.List;

import com.architexa.diagrams.chrono.controlflow.CollapseExpandButton;
import com.architexa.diagrams.chrono.controlflow.ControlFlowBlock;
import com.architexa.diagrams.chrono.controlflow.IfBlock;
import com.architexa.diagrams.chrono.controlflow.IfBlock.ElseBlock;
import com.architexa.diagrams.chrono.models.MemberModel;
import com.architexa.diagrams.chrono.models.MethodBoxModel;
import com.architexa.diagrams.chrono.models.NodeModel;
import com.architexa.diagrams.chrono.sequence.StatementHandler;
import com.architexa.diagrams.chrono.util.MemberUtil;
import com.architexa.diagrams.jdt.Invocation;
import com.architexa.org.eclipse.gef.commands.CompoundCommand;

public class UpdateControlBlockCommand extends CompoundCommand {

	IfBlock ifBlock;
	MemberModel model;
	NodeModel parent;
	List<ControlFlowBlock> blockList;
	List<CollapseExpandButton> buttonList = new ArrayList<CollapseExpandButton>();

	public UpdateControlBlockCommand(IfBlock ifBlock, MemberModel model) {
		super("Update If Block");
		this.ifBlock = ifBlock;
		this.model = model;
		this.parent = model.getParent();
		blockList = new ArrayList<ControlFlowBlock>(model
				.getConditionalBlocksContainedIn());
	}

	@Override
	public boolean canExecute() {
		return true;
	}

	@Override
	public void execute() {
		update();
	}

	@Override
	public boolean canUndo() {
		return true;
	}

	@Override
	public void redo() {
		update();
	}

	private void update() {
		updateBlock();
		if (!buttonList.isEmpty())
			this.add(new CollapseCommand(buttonList));
		super.execute();
	}

	public void updateBlock() {
		if (ifBlock.getStatements().isEmpty()
				&& !MemberUtil.outerBlocksHaveChildren(ifBlock))
			return; // do not collapse if all stmts of block are removed

		if (ifBlock.getThenStmts().isEmpty()
				&& !ifBlock.getOrigThenStmts().isEmpty()) {
			addCallsAndCollapse((MethodBoxModel) parent, ifBlock.getOrigThenStmts(), 
					ifBlock.getThenStmts());
			buttonList.add(ifBlock.getButton());
		}

		if (ifBlock.getElseStmts().isEmpty()
				&& !ifBlock.getOrigElseStmts().isEmpty()) {
			addCallsAndCollapse((MethodBoxModel) parent, ifBlock.getOrigElseStmts(), 
					ifBlock.getElseStmts());
			buttonList.add(getElseButton(ifBlock.getElseStmts(), ifBlock));
		}

		for (String str : ifBlock.getOrigElseIfStmts().keySet()) {
			if (ifBlock.getElseIfStmts().get(str).isEmpty()
					&& !ifBlock.getOrigElseIfStmts().get(str).isEmpty()) {
				addCallsAndCollapse((MethodBoxModel) parent, ifBlock.getOrigElseIfStmts().get(str), 
						ifBlock.getElseIfStmts().get(str));
				buttonList.add(getElseButton(ifBlock.getElseIfStmts().get(str),	ifBlock));
			}
		}
	}

	private CollapseExpandButton getElseButton(List<MemberModel> stmts,
			IfBlock ifBlock) {
		for (ElseBlock block : ifBlock.getElseBlocks()) {
//			if (block.getStmts().equals(stmts))
//				return block.getButton();
		}
		return null;
	}

	private void addCallsAndCollapse(MethodBoxModel parent,
			List<MemberModel> origStmt, List<MemberModel> stmtsToAdd) {
		stmtsToAdd.clear();
		for (MemberModel invocation : updateOrigStmts(origStmt)) {
			Invocation invoc = new Invocation(invocation.getASTNode());
			// if invocation is a field mode then continue as the corresponding
			// method should take care of its creation.
			if (invoc.getInvocation() == null)
				continue;

			StatementHandler.createCallFromInvocation(invoc, parent
					.getInstanceModel(), parent, ifBlock.getDiagram(), this,
					stmtsToAdd);
		}
		for (MemberModel model : stmtsToAdd) {
			for (ControlFlowBlock block : updateBlockList())
				model.addConditionalBlock(block);
		}
	}

	// method to remove all stmts not present in this block as a direct child
	// i.e is child of a nested block
	private List<MemberModel> updateOrigStmts(List<MemberModel> origStmt) {
		List<MemberModel> updatedList = new ArrayList<MemberModel>(origStmt);
		for (ControlFlowBlock block : ifBlock.getInnerConditionalBlocks()) {
			for (MemberModel innerStmts : block.getOrigStatements())
				updatedList.remove(innerStmts);
		}
		return updatedList;
	}

	// method to remove all inner blocks from the list, this is for a specific
	// case when the update command was made for the outer block by some
	// statement in
	// the inner block
	private List<ControlFlowBlock> updateBlockList() {
		List<ControlFlowBlock> listBlock = new ArrayList<ControlFlowBlock>(
				blockList);
		for (ControlFlowBlock block : ifBlock.getInnerConditionalBlocks())
			listBlock.remove(block);
		return listBlock;
	}
}
