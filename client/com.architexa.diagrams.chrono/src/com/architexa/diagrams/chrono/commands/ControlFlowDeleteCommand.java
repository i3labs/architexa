package com.architexa.diagrams.chrono.commands;


import com.architexa.diagrams.chrono.controlflow.ControlFlowBlock;
import com.architexa.org.eclipse.gef.commands.Command;

public class ControlFlowDeleteCommand extends Command{

	ControlFlowBlock block;
	public ControlFlowDeleteCommand(ControlFlowBlock block){
		if(block == null) throw new IllegalArgumentException();
		this.block = block;
	}

	@Override
	public void execute(){
		block.delete();
	}

	@Override
	public void undo(){
		addToLayer();
	}

	@Override
	public void redo(){
		block.delete();
	}

	public void addToLayer(){
		if (block.getModel().getOuterConditionalModel() == null)
			block.getDiagram().addChildToConditionalLayer(block.getModel());
	}

}
