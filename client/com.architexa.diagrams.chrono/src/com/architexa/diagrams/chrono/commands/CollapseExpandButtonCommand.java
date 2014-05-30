package com.architexa.diagrams.chrono.commands;


import com.architexa.diagrams.chrono.controlflow.CollapseExpandButton;
import com.architexa.diagrams.chrono.controlflow.ControlFlowBlock;
import com.architexa.org.eclipse.gef.commands.CompoundCommand;

public class CollapseExpandButtonCommand extends CompoundCommand{

	CollapseExpandButton button;
	public CollapseExpandButtonCommand(CollapseExpandButton collapseExpandButton) {
		super("Collapse/Expand Control Flow");
		button = collapseExpandButton;
	}

	@Override
	public void execute(){
		super.execute();
		invertType();
	}

	@Override
	public void undo(){
		invertType();
		super.undo();
	}

	@Override
	public void redo(){
		super.execute();
		invertType();
	}

	private void invertType(){
		String type = button.getType(); 
		type =	ControlFlowBlock.SHOWING.equals(type) ? ControlFlowBlock.HIDING : ControlFlowBlock.SHOWING;
		button.setType(type);
	}

}
