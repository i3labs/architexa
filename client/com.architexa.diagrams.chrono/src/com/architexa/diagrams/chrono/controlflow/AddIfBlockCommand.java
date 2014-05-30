package com.architexa.diagrams.chrono.controlflow;

import com.architexa.diagrams.chrono.models.DiagramModel;
import com.architexa.org.eclipse.gef.commands.Command;

public class AddIfBlockCommand extends Command {
	private DiagramModel diagramModel;
	private ControlFlowModel ifModel;

	public AddIfBlockCommand(DiagramModel diagramModel, ControlFlowModel ifModel) {
		super("Add If Block");
		this.diagramModel = diagramModel;
		this.ifModel = ifModel;
	}

	@Override
	public void execute() {
		if(ifModel.getStatements().size()<=0) return;
		if (ifModel.getOuterConditionalModel() == null)
			diagramModel.addChildToConditionalLayer(ifModel);
	}
	
	@Override
	public void undo() {
		if (ifModel.getOuterConditionalModel() == null)
			diagramModel.removeChildFromConditionalLayer(ifModel);
//		else // need to remove from the model (was added to model during statement handler)
//			ifModel.getOuterConditionalModel().removeInnerConditionalModel(ifModel);
	}
}
