package com.architexa.diagrams.chrono.commands;

import com.architexa.diagrams.chrono.animation.AnimateOverrideCommand;
import com.architexa.diagrams.chrono.animation.AnimateOverrideCommand.AnimateOverrideConnectionCommand;
import com.architexa.diagrams.chrono.editparts.DiagramEditPart;
import com.architexa.diagrams.chrono.models.ConnectionModel;
import com.architexa.diagrams.chrono.models.InstanceModel;
import com.architexa.diagrams.chrono.models.MethodBoxModel;
import com.architexa.diagrams.commands.BreakableCommand;

public class UserOverridesCreateCommand extends UserConnCreateCommand {

	DiagramEditPart diagramEP;
	AnimateOverrideCommand overrideCommand;

	public UserOverridesCreateCommand(MethodBoxModel sourceModel, DiagramEditPart diagramEP) {
		super(sourceModel);
		this.diagramEP = diagramEP;
	}

	@Override
	public void execute() {

		overrideCommand = new AnimateOverrideCommand();
		BreakableCommand addMethodAndOverridesConnCmd = new BreakableCommand("adding overrides relationship", overrideCommand);

		InstanceModel declaringClassModel = targetModel.getInstanceModel();

		overrideCommand.setInstance(declaringClassModel);
		MethodBoxModel overrider = sourceModel;
		MethodBoxModel overridden = targetModel;
		overrideCommand.setOverrider(overrider);
		overrideCommand.setOverridden(overridden);

		AnimateOverrideConnectionCommand connAnimationCmd = overrideCommand.createAnimateOverrideConnectionCommand();
		addMethodAndOverridesConnCmd.addBreakPlace(connAnimationCmd);

		ConnectionCreateCommand overridesConnCmd = new ConnectionCreateCommand(overrider, overridden, "overrides", ConnectionModel.OVERRIDES);							
		addMethodAndOverridesConnCmd.add(overridesConnCmd);

		diagramEP.execute(addMethodAndOverridesConnCmd);

		overridesConnCmd.setVisible(true);
		overridesConnCmd.getConnection().setVisible(true);
	}

	@Override
	public void redo() {
		if (overrideCommand == null) return;
		overrideCommand.redo();
	}

	@Override
	public void undo() {
		if (overrideCommand == null || !overrideCommand.canUndo()) return;
		overrideCommand.undo();
	}

}
