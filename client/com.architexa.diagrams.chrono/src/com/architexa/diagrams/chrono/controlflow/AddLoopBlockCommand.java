package com.architexa.diagrams.chrono.controlflow;

import com.architexa.diagrams.chrono.models.DiagramModel;
import com.architexa.org.eclipse.gef.commands.Command;

public class AddLoopBlockCommand extends Command {
	private DiagramModel diagramModel;
	private ControlFlowModel loopModel;

	public AddLoopBlockCommand(DiagramModel diagramModel, ControlFlowModel loopModel) {
		super("Add If Block");
		this.diagramModel = diagramModel;
		this.loopModel = loopModel;
	}

	@Override
	public void execute() {
		if (loopModel.getOuterConditionalModel()==null)
			diagramModel.addChildToConditionalLayer(loopModel);
	}
	
	
	@Override
	public void undo() {
		if (loopModel.getOuterConditionalModel() == null)
			diagramModel.removeChildFromConditionalLayer(loopModel);
//		else // need to remove from the model (was added to model during statement handler)
//			loopModel.getOuterConditionalModel().removeInnerConditionalModel(loopModel);
	}
}
