package com.architexa.diagrams.chrono.commands;

import com.architexa.diagrams.chrono.models.DiagramModel;
import com.architexa.diagrams.chrono.models.UserCreatedInstanceModel;

public class UserInstanceCreateCommand extends SeqCommand{

	DiagramModel diagram;
	UserCreatedInstanceModel userInstanceModel;
	int index;
	
	public UserInstanceCreateCommand(DiagramModel diagram, UserCreatedInstanceModel model,int index){
		if (diagram == null || model == null) 
			throw new IllegalArgumentException();
		this.diagram = diagram;
		this.userInstanceModel = model;
		this.index=index;
	}
	
	@Override
	public void execute(){
		if (! diagram.getChildren().contains(userInstanceModel)){
			if (index > diagram.getChildren().size()) index = -1;
			diagram.addChild(userInstanceModel,index);
		}
	}
	
	@Override
	public void undo(){
		if (diagram.getChildren().contains(userInstanceModel))
			diagram.removeChild(userInstanceModel);
	}
	
	@Override
	public void redo(){
		execute();
	}
}
