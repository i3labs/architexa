package com.architexa.diagrams.relo.commands;


import com.architexa.diagrams.relo.parts.ArtifactEditPart;
import com.architexa.diagrams.relo.parts.ModelControllerManager;
import com.architexa.org.eclipse.gef.commands.Command;
// Nested Command for CreateParentCommand
// also used for building menus
public class ReparentCommand extends Command  {
	private ArtifactEditPart childEP;
	private ArtifactEditPart thisAEP;
	private ArtifactEditPart oldParentEP;
	public ReparentCommand(ArtifactEditPart childEP,
			ArtifactEditPart thisAEP, ArtifactEditPart oldParentEP) {
		super("Reparent Node");
		this.childEP = childEP;
		this.thisAEP = thisAEP;
		this.oldParentEP = oldParentEP;
    }
	@Override
    public void execute() { 
    	ModelControllerManager.moveModelAndChildKeepingSelection(childEP, oldParentEP, thisAEP);
    }
    @Override
    public void undo() { 
    	ModelControllerManager.moveModelAndChildKeepingSelection(childEP, thisAEP, oldParentEP);
    }
};