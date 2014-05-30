package com.architexa.diagrams.chrono.commands;


import com.architexa.diagrams.chrono.models.DiagramModel;
import com.architexa.diagrams.chrono.models.UserCreatedInstanceModel;
import com.architexa.org.eclipse.gef.commands.Command;

public class UserInstanceDeleteCommand extends Command{
	DiagramModel diagram;
	UserCreatedInstanceModel instance;
	int index;

	public UserInstanceDeleteCommand(DiagramModel diagram, UserCreatedInstanceModel instance, int index){
		if(diagram==null||instance==null)
			throw new IllegalArgumentException();
		this.diagram=diagram;
		this.index=index;
		this.instance=instance;
	}

	@Override
	public boolean canExecute() {
		return diagram != null && instance != null;
	}
	@Override
	public void execute(){
		removeInstance();
	}

	private void removeInstance(){
		if(diagram !=null && diagram.getChildren().contains(instance)){
			diagram.removeChild(instance);
		}
	}

	@Override
	public void undo(){
		if(!diagram.getChildren().contains(instance)){
			if(index>diagram.getChildren().size()) index = -1;
			diagram.addChild(instance, index);
		}
	}

	@Override
	public void redo(){
		removeInstance();
	}

}
