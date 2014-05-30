package com.architexa.diagrams.chrono.commands;


import com.architexa.diagrams.chrono.models.HiddenNodeModel;
import com.architexa.diagrams.chrono.models.NodeModel;
import com.architexa.org.eclipse.gef.commands.Command;

public class HiddenNodeCreateCommand extends Command{

	HiddenNodeModel hiddenModel;
	NodeModel parent;
	int index;

	public HiddenNodeCreateCommand(HiddenNodeModel hiddenModel, NodeModel parent, int index){
		if(hiddenModel == null || parent == null) throw new IllegalArgumentException();

		this.hiddenModel = hiddenModel;
		this.parent = parent;
		this.index = index;
	}

	@Override
	public void execute(){
		addChild();
	}

	@Override
	public void undo(){
		if(parent.getChildren().contains(hiddenModel))
			parent.removeChild(hiddenModel);
	}

	@Override
	public void redo(){
		addChild();
	}

	private void addChild(){
		if(!parent.getChildren().contains(hiddenModel)){
			if(parent.getChildren().size() > index){
				parent.addChild(hiddenModel,index);
				return;
			}

			parent.addChild(hiddenModel, -1);
		}

	}
}
