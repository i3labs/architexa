package com.architexa.diagrams.chrono.commands;

import java.util.List;


import com.architexa.diagrams.chrono.controlflow.CollapseExpandButton;
import com.architexa.org.eclipse.gef.commands.CompoundCommand;

public class CollapseCommand extends CompoundCommand{
	List<CollapseExpandButton> buttonList;
	public CollapseCommand(List<CollapseExpandButton> collapseExpandButtonList) {
		super("Collapse Block");
		buttonList = collapseExpandButtonList;
	}

	@Override
	public void execute(){
		update();
	}

	public void collapse(CollapseExpandButton button){
		if(button == null)	return;
		CollapseExpandButtonCommand buttonCommand = new CollapseExpandButtonCommand(button);
		button.collapseOrExpand(true, buttonCommand);
		this.add(buttonCommand);
	}

	@Override
	public boolean canUndo(){
		return true;
	}

	@Override
	public void redo(){
		update();
	}

	public void update(){
		for(CollapseExpandButton button : buttonList)
			collapse(button);
		super.execute();	
	}
}
