package com.architexa.diagrams.ui;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;

import com.architexa.diagrams.Activator;
import com.architexa.diagrams.utils.JobUtils;
import com.architexa.org.eclipse.gef.commands.Command;
import com.architexa.org.eclipse.gef.commands.CompoundCommand;

public class LongCommandStack extends RSECommandStack {
	private static final Logger logger = Activator.getLogger(LongCommandStack.class);
	
	public interface HidePrep {}
	private Command curCmd = null;

	public LongCommandStack(String editorName) {
		super(editorName);
	}

	private void baseExec(Command cmd) {
		super.execute(cmd);
	}
	
	@Override
	public void execute(final Command cmd){
		if (cmd == null) {
			logger.error("Unexpected error", new Exception());
			return;
		}
		// If we get another longCommand that is the same as the currently
		// executing one, ignore it. The cached cmd is set to null upon completion 
		if (curCmd!= null && cmd.getClass()== curCmd.getClass()) return;
		curCmd = cmd;
		if (cmd instanceof HidePrep)
			this.execute(cmd, false);
		else
			this.execute(cmd, true);
	}
	
	public void execute(final Command cmd, final boolean showPrep){
		Job prepJob = null;
		final List<LongCommand> longCmds = getLongCommands(cmd);
		if (!longCmds.isEmpty()) {
			prepJob = new Job(cmd.getLabel()) {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					try {
						for (LongCommand longCommand : longCmds) {
							longCommand.prep(monitor);
						}
					} catch (Throwable t) {
	                    logger.error("Unexpected exception", t);
					}
					return Status.OK_STATUS;
				}
			};
			if (showPrep) {
				prepJob.setUser(true);
				prepJob.schedule(1000);				
			} else {
				// Schedule with no delay in the case of linkedTracker / not showing prep.
				// this prevents duplicate adds from being queued.
				// there might be a better solution for this (not adding items that have already been added).
				prepJob.setUser(false);
				prepJob.setSystem(true);
				prepJob.schedule(1);		
			}
			prepJob.setPriority(Job.LONG);
			}
		JobUtils.performOnCompletion(prepJob, new Runnable() {
			public void run() {
				curCmd = null;
				Display.getDefault().asyncExec(new Runnable() {
					
					public void run() {
						LongCommandStack.this.baseExec(cmd);
					}});
			}});
	}
	private static List<LongCommand> getLongCommands(Command cmd) {
		List<LongCommand> retVal = new ArrayList<LongCommand>(2);
		if (cmd instanceof LongCommand) retVal.add((LongCommand) cmd);
		if (cmd instanceof CompoundCommand) {
			for (Object containedCmd : ((CompoundCommand)cmd).getCommands()) {
				retVal.addAll(getLongCommands((Command) containedCmd));
			}
		}
		return retVal;
	}
}
