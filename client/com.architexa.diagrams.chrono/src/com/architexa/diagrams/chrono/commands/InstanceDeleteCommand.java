package com.architexa.diagrams.chrono.commands;

import java.util.List;


import com.architexa.diagrams.chrono.models.DiagramModel;
import com.architexa.diagrams.chrono.models.InstanceModel;
import com.architexa.diagrams.chrono.util.SeqRelUtils;
import com.architexa.diagrams.model.ArtifactRel;
import com.architexa.org.eclipse.gef.commands.Command;

public class InstanceDeleteCommand extends Command{

	DiagramModel diagram;
	InstanceModel instance;
	int index;
	private List<ArtifactRel> removedRels;

	public InstanceDeleteCommand(DiagramModel diagram, InstanceModel instance, int index){
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
			if (diagram.removeChild(instance)) 
				removedRels = SeqRelUtils.removeChildAnnotatedRels(instance);
		}
	}
	
	@Override
	public void undo(){
		if(!diagram.getChildren().contains(instance)){
			if(index>diagram.getChildren().size()) index = -1;
			diagram.addChild(instance, index);
			SeqRelUtils.addRels(removedRels);
		}
	}
}
