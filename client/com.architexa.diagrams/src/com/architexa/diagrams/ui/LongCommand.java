package com.architexa.diagrams.ui;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;

import com.architexa.diagrams.Activator;
import com.architexa.org.eclipse.gef.EditPart;
import com.architexa.org.eclipse.gef.commands.Command;
import com.architexa.org.eclipse.gef.commands.CommandStack;

/**
 * Similar to regular Commands - however these commands can take a long time to
 * run and therefore are given a ProgressMonitor as well.<br>
 * <br>
 * LongCommand's therefore need a separate Thread/Job to be run in. There are
 * two potential places to start the job. The first approach is to start the job
 * inside the traditional execute() method - this approach has the challenge
 * that the command would officially finish before job would have, and thus the
 * user would be able to undo and programmatically we would need to add extra
 * logic to when we wait for command stack changes. The second approach is to
 * start the job before the execute method, which is what we are doing here.<br>
 * <br>
 * The execute method is therefore expected to run fast as before, and the job
 * is launched and calls the prep method with the ProgressMonitor. This approach
 * means that the controller can't just call CommandStack.execute on
 * LongCommands but need to use LongCommand.checkForSchedulingPrepAndExecute<br>
 * <br>
 * Currently LongCommands are only used in Strata - it might make sense to
 * eventually use them in Relo and/or Chrono.
 */
public abstract class LongCommand extends Command {
	static final Logger logger = Activator.getLogger(LongCommand.class);

	public LongCommand() {}
	public LongCommand(String label) {	super(label);	}
	
	public abstract void prep(IProgressMonitor monitor);
	
	public static void checkForSchedulingPrepAndExecute(EditPart ep, Command cmd) {
		checkForSchedulingPrepAndExecute(ep.getViewer().getEditDomain().getCommandStack(), cmd);
	}
	public static void checkForSchedulingPrepAndExecute(final CommandStack cmdStack, final Command cmd) {
		cmdStack.execute(cmd);
	}
}
