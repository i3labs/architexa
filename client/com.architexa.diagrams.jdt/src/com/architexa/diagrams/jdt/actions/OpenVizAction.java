package com.architexa.diagrams.jdt.actions;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.architexa.diagrams.jdt.Activator;
import com.architexa.diagrams.jdt.JDTSelectionUtils;
import com.architexa.diagrams.jdt.builder.ResourceQueue;
import com.architexa.diagrams.jdt.builder.ResourceQueueBuilder;
import com.architexa.diagrams.ui.AFSelectionUtils;
import com.architexa.diagrams.ui.SelectableAction;
import com.architexa.diagrams.ui.WaitableUIJob;
import com.architexa.diagrams.utils.BuildPreferenceUtils;

public abstract class OpenVizAction extends SelectableAction {

	private static final Logger logger = Activator.getLogger(OpenVizAction.class);
	
	private static final int SKIP = 0;
	private static final int WAIT_BCKGD = 1;
	private static final int CANCEL = 2;

	public abstract void openViz(IWorkbenchWindow activeWorkbenchWindow, List<?> selList);	
	public abstract void openViz(IWorkbenchWindow activeWorkbenchWindow,
			List<?> toAddToDiagram, Map<IDocument, IDocument> lToRDocMap, Map<IDocument, String> lToPathMap, StringBuffer docBuff);

	public List<?> getSelection() {
		// we really want the AFSelectionUtils to happen first, but we are doing
		// it second to give priority to JDT (and we don't know the unexpected
		// sideaffects otherwise)
		List<?> sel = JDTSelectionUtils.getSelectedJDTElements(true);
		if (sel.isEmpty())
			sel = AFSelectionUtils.getSelection();
		return sel;
	}

	@Override
	public void run(IAction action) {
		try {
			IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			doChecksAndOpenViz(activeWorkbenchWindow, getSelection());
		} catch (CoreException e) {
			logger.error("Unexpected Exception", e);
		}
	}
	
	private void doChecksAndOpenViz(final IWorkbenchWindow activeWorkbenchWindow, final List<?> selList) throws CoreException {
		if (!BuildPreferenceUtils.selectionInBuild(selList))
			return;

		IProject curProj = getProj(selList);

		// this check should have been done in the 'selectionInBuild' - the only
		// reason that this would be null is if we are getting ArtFrags directly
		//if (curProj == null) {				
		//	logger.error("No current project");
		//	return;
		//}

		WaitableUIJob job = new WaitableUIJob("Waiting to Launch Visualization: Architexa is Indexing") {
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				openViz(activeWorkbenchWindow, selList);
				return Status.OK_STATUS;
			}
		};
		job.setUser(true);

		// Using ResourceHandler's theProjs list to determine what still needs to be built.
		boolean curProjNeedsToBeBuilt = false;
		if (curProj != null)
			curProjNeedsToBeBuilt = ResourceQueue.getProjects().contains(curProj);

		boolean jobInProgress = ResourceQueueBuilder.jobInProgress();

		if (jobInProgress && curProjNeedsToBeBuilt) {
			// Selected component has not been built yet, so prompt user
			// whether to open it after building is complete or cancel opening
			MessageDialog waitToOpenViz = null;
			boolean isFullBuild = !ResourceQueueBuilder.resourceBuilder.isSystem() && ResourceQueueBuilder.resourceBuilder.isUser();
			if (isFullBuild) {
				waitToOpenViz = new MessageDialog(
						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
						"Build in Progress...",
						null,
						"Selected component not built yet. Open component once build is complete?",
						MessageDialog.WARNING, new String[] { "OK", "Cancel" }, 0);
			} else {
				waitToOpenViz = new MessageDialog(
						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
						"Build In Progress...",
						null,
						"Waiting for Indexing to complete... \nPress Skip to open potentially outdated component.",
						MessageDialog.WARNING, new String[] { "Skip", "Wait in Background", "Cancel" }, 1);
			}

			// Close the message window if build completes before any selection from the user
			final MessageDialog vizDialog = waitToOpenViz;
			if (!isFullBuild) {
				JobChangeAdapter builderJobListener = new JobChangeAdapter() {
					@Override
					public void done(IJobChangeEvent event) {
						Display.getDefault().asyncExec(new Runnable() {
							public void run() {
								vizDialog.close();
							}
						});
					}
				};
				// adding listener to the builder
				if (ResourceQueueBuilder.resourceBuilder != null)
					ResourceQueueBuilder.resourceBuilder.addJobChangeListener(builderJobListener);
			}

			waitToOpenViz.open();
			int returnCode = waitToOpenViz.getReturnCode();

			if ((isFullBuild && returnCode == MessageDialog.CANCEL)
					|| returnCode == CANCEL) {
				logger.info("Cancelled opening viz instead of waiting for build");
				return;
			} else if ((isFullBuild && returnCode == MessageDialog.OK)
					|| returnCode == WAIT_BCKGD) {
				// Possible that if build was terminated or if error
				// occurred during build that build job might still be alive but not
				// actually ever going to run. So scheduling it here to make
				// sure we don't wait indefinitely for a build that will never run.
				if (ResourceQueueBuilder.resourceBuilder != null) {
					// make bkground incremental build job visible to user
					((ResourceQueueBuilder) ResourceQueueBuilder.resourceBuilder).restartBuildInProgress();
					
					// open diagram after but do not schedule the 'wait' dialog
					JobChangeAdapter builderJobListener = new JobChangeAdapter() {
						@Override
						public void done(IJobChangeEvent event) {
							Display.getDefault().asyncExec(new Runnable() {
								public void run() {
									openViz(activeWorkbenchWindow, selList);
								}
							});
						}
					};
					// adding listener to the builder
					if (ResourceQueueBuilder.resourceBuilder != null)
						ResourceQueueBuilder.resourceBuilder.addJobChangeListener(builderJobListener);
					return;
				}
				// Wait for the build job to actually start, i.e. wait for 
				// 2 x Policy.MIN_BUILD_DELAY
				job.schedule(200);
				job.waitForJobWithFamily(ResourceQueueBuilder.AtxaBuildFamily);
			} else if (returnCode == SKIP) {
				job.schedule();
			}
		} else {
			job.schedule();
		}
	}

	/**
	 * Gets the associated project, from any IResource's in a List 
	 */
	private static IProject getProj(final List<?> selList) {
		IProject curProj = null;
		for (Object selElement : selList) {
			if (selElement instanceof IJavaElement) {
				curProj = ((IJavaElement)selElement).getJavaProject().getProject();
			} else if (selElement instanceof IResource) {
				curProj = ((IResource)selElement).getProject();
			} else if (selElement instanceof IAdaptable) {
				IJavaElement selIJE = (IJavaElement) ((IAdaptable)selElement).getAdapter(IJavaElement.class);
				IResource selRes = null;
				if (selIJE == null) selRes = (IResource) ((IAdaptable)selElement).getAdapter(IResource.class);

				if (selIJE != null) curProj = selIJE.getJavaProject().getProject();
				else if (selRes != null) curProj = selRes.getProject();
			}
			if (curProj != null) break;
		}
		return curProj;
	}
	

}
