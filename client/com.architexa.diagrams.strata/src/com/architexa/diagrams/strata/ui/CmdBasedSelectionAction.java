/**
 * Copyright (c) 2007-2008 Architexa, Inc
 */ 
package com.architexa.diagrams.strata.ui;

import java.util.List;

import org.eclipse.ui.IWorkbenchPart;

import com.architexa.org.eclipse.gef.commands.Command;
import com.architexa.org.eclipse.gef.ui.actions.SelectionAction;

public abstract class CmdBasedSelectionAction extends SelectionAction {

	public CmdBasedSelectionAction(IWorkbenchPart part) {
		super(part);
	}

	public CmdBasedSelectionAction(IWorkbenchPart part, int style) {
		super(part, style);
	}

	@Override
	protected boolean calculateEnabled() {
		Command cmd = createCommand(getSelectedObjects());
		if (cmd == null) return false;
		return cmd.canExecute();
	}

	@Override
	public void run() {
		execute(createCommand(getSelectedObjects()));
	}

    public abstract Command createCommand(List<?> objects);
    
}
