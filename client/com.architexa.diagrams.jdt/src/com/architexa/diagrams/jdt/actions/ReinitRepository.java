package com.architexa.diagrams.jdt.actions;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

import com.architexa.diagrams.jdt.builder.VersionedRepositoryCheckAndInitializer;
import com.architexa.diagrams.jdt.builder.asm.AsmPackageSupport;
import com.architexa.store.ReloRdfRepository;
import com.architexa.store.StoreUtil;


public class ReinitRepository implements IWorkbenchWindowActionDelegate {

	public ReinitRepository() {
	}

	public void init(IWorkbenchWindow window) {
	}

	public static void init(IAction myAction) {
		myAction.setText("Rebuild Complete Index");
	}

	/**
	 * Delete and then recreate the repository
	 */
	public void run(IAction action) {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

		if(StoreUtil.hasDiagrams()){
			boolean confirmed = MessageDialog.openConfirm(window.getShell(),
					"Close Editors?",
					"In order to Reinitialize, diagrams will be closed.");
			if (!confirmed) return;
		}
		
		launchJob();
	}


	public static void launchJob() {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IWorkbenchPage page = window.getActivePage();

		//Go through and close the RSEEditors
		StoreUtil.killDiagrams(page);

		Job reinitJob = new Job("Architexa RSE Connection") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				ReloRdfRepository repo = StoreUtil.getDefaultStoreRepository();
				AsmPackageSupport.clearCaches();
				VersionedRepositoryCheckAndInitializer.initializeStore(repo, monitor, true);
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