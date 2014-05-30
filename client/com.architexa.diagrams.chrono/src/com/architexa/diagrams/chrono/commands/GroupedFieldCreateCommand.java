package com.architexa.diagrams.chrono.commands;


import com.architexa.diagrams.chrono.models.GroupedFieldModel;
import com.architexa.diagrams.chrono.models.NodeModel;
import com.architexa.org.eclipse.gef.commands.Command;

public class GroupedFieldCreateCommand extends Command{

	private NodeModel parent;
	private GroupedFieldModel fieldModel;
	private int index = -1;
	public GroupedFieldCreateCommand(NodeModel parent, GroupedFieldModel fieldModel){
		this.parent = parent;
		this.fieldModel = fieldModel;
	}

	@Override
	public boolean canExecute(){
		return parent!=null && fieldModel!=null;
	}

	@Override
	public void execute(){
		parent.addChild(fieldModel, index);
	}

	@Override
	public void undo(){
		if(parent.getChildren().contains(fieldModel))
			parent.removeChild(fieldModel);
	}

	@Override
	public void redo(){
		parent.addChild(fieldModel, index);
	}
}
