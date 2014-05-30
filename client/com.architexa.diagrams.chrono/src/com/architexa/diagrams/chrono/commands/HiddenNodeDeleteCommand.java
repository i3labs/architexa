package com.architexa.diagrams.chrono.commands;


import com.architexa.diagrams.chrono.models.HiddenNodeModel;
import com.architexa.diagrams.chrono.models.NodeModel;
import com.architexa.org.eclipse.gef.commands.Command;

public class HiddenNodeDeleteCommand extends Command{
	HiddenNodeModel hiddenModel;
	NodeModel parent;
	boolean visible;
	String toolTip;
	int index;

	public HiddenNodeDeleteCommand(HiddenNodeModel hiddenModel, NodeModel parent, boolean visible, String toolTip){
		if(hiddenModel == null || parent == null) throw new IllegalArgumentException();

		this.hiddenModel = hiddenModel;
		this.parent = parent;
		this.visible = visible;
		this.toolTip = toolTip;
	}

	@Override
	public void execute(){
		removeChild();
	}

	@Override
	public void undo(){
		if(!parent.getChildren().contains(hiddenModel)){
			if(parent.getChildren().size() > index){
				parent.addChild(hiddenModel,index);
				return;
			}
			parent.addChild(hiddenModel, -1);
		}
	}

	@Override
	public void redo(){
		removeChild();
	}

	private void removeChild(){
		if(parent.getChildren().contains(hiddenModel)){
			index = parent.getChildren().indexOf(hiddenModel);
			parent.removeChild(hiddenModel);
		}
	}
}
