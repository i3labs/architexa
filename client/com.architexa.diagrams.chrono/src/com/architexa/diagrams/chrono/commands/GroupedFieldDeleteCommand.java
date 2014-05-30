package com.architexa.diagrams.chrono.commands;


import com.architexa.diagrams.chrono.models.GroupedFieldModel;
import com.architexa.diagrams.chrono.models.NodeModel;
import com.architexa.org.eclipse.gef.commands.Command;

public class GroupedFieldDeleteCommand extends Command{
	private NodeModel groupedParent;
	private GroupedFieldModel groupedFieldModel;
	private int index = -1;

	public GroupedFieldDeleteCommand(NodeModel parent, GroupedFieldModel fieldModel){
		this.groupedParent = parent;
		this.groupedFieldModel = fieldModel;
	}

	@Override
	public boolean canExecute(){
		return groupedParent!=null && groupedFieldModel!=null;
	}

	@Override
	public void execute(){
		if(groupedParent.getChildren().contains(groupedFieldModel)){
			index = groupedParent.getChildren().indexOf(groupedFieldModel);
			groupedParent.removeChild(groupedFieldModel);
		}
	}

	@Override
	public void undo(){
		groupedParent.addChild(groupedFieldModel, index);
	}

	@Override
	public void redo(){
		if(groupedParent.getChildren().contains(groupedFieldModel))
			groupedParent.removeChild(groupedFieldModel);
	}
}
