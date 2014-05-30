package com.architexa.diagrams.jdt.actions;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import com.architexa.diagrams.jdt.builder.ResourceQueueBuilder;
import com.architexa.diagrams.jdt.builder.VersionedRepositoryCheckAndInitializer;
import com.architexa.rse.BuildStatus;

public class UpdateRepository implements IWorkbenchWindowActionDelegate{
	public UpdateRepository() {
	}

	public void init(IWorkbenchWindow window) {
	}

	public static void init(IAction myAction) {
		myAction.setText("Update Indexes");
	}

	/**
	 * Delete and then recreate the repository
	 */
	public void run(IAction action) {
		
		
		Job reinitJob = new Job("Architexa RSE Connection") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				
				if (BuildStatus.getClientDisabled() ){//|| !AccountStatusUtils.testAccountValid()) {
					boolean enabled = new VersionedRepositoryCheckAndInitializer.SyncExecQuestionDialog().runDialog("Architexa RSE - Account Disabled", 
							"You disabled your Architexa client. \nEnable to rebuild?");
					if (enabled) {
						ResourceQueueBuilder.startBuildJob(true, false, false /*Do not reset Dep cache for updates*/);
					} else return Status.OK_STATUS;
					
				} else
					ResourceQueueBuilder.startBuildJob(true, false, false /*Do not reset Dep cache for updates*/);
				
				return Status.OK_STATUS;
			}
		};
		reinitJob.setUser(false);
		reinitJob.setPriority(Job.LONG);
		reinitJob.schedule();
	}


	public void selectionChanged(IAction action, ISelection selection) {
	}

	public void dispose() {
	}
}
