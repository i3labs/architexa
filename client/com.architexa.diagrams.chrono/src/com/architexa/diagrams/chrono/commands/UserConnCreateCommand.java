package com.architexa.diagrams.chrono.commands;


import com.architexa.diagrams.chrono.models.MethodBoxModel;
import com.architexa.org.eclipse.gef.commands.Command;

public class UserConnCreateCommand extends Command {

	MethodBoxModel sourceModel;
	MethodBoxModel targetModel;

	public UserConnCreateCommand(MethodBoxModel sourceModel) {
		if (sourceModel == null)
			throw new IllegalArgumentException();
		this.sourceModel = sourceModel;
	}

	public void setTargetModel(MethodBoxModel targetModel) {
		this.targetModel = targetModel;
	}

}
