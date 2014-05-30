package com.architexa.diagrams.ui;


import org.apache.log4j.Logger;

import com.architexa.diagrams.Activator;
import com.architexa.org.eclipse.gef.commands.Command;
import com.architexa.org.eclipse.gef.commands.CommandStack;
import com.architexa.rse.BuildStatus;

public class RSECommandStack extends CommandStack {
	private static final Logger logger = Activator.getLogger(RSECommandStack.class);

	private final String editorName;

	public RSECommandStack(String editorName) {
		this.editorName = editorName;
		BuildStatus.addDiagramType(this.toString(), editorName);
	}
	
	@Override
	public void execute(Command cmd) {
		BuildStatus.updateDiagramActionMap(this.toString(), cmd.getLabel());
		BuildStatus.addUsage(editorName + " > " + cmd.getLabel());
		try {
			super.execute(cmd);
		} catch (Throwable t) {
			logger.error("Unexpected Exception", t);
		}
	}
	
	@Override
	public void undo() {
		Command cmd = getUndoCommand();
		if (cmd != null){
			BuildStatus.addUsage(editorName + "\\Undo > " + cmd.getLabel());
			BuildStatus.updateDiagramActionMap(this.toString(), "\\Undo > " + cmd.getLabel());
		}

		super.undo();
	}

}
