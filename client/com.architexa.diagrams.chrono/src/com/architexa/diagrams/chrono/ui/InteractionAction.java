package com.architexa.diagrams.chrono.ui;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;

import com.architexa.diagrams.chrono.animation.AnimateCallMadeCommand;
import com.architexa.diagrams.chrono.editparts.DiagramEditPart;
import com.architexa.diagrams.chrono.editparts.InstanceEditPart;
import com.architexa.diagrams.chrono.models.InstanceModel;
import com.architexa.diagrams.commands.BreakableCommand;
import com.architexa.diagrams.utils.RootEditPartUtils;


/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public class InteractionAction implements IEditorActionDelegate, IViewActionDelegate {

	private static SeqEditor editor;

	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		if(targetEditor == null) return;
		if (!(RootEditPartUtils.getEditorFromRSEMultiPageEditor(targetEditor) instanceof SeqEditor)) {
			action.setEnabled(false);
			return;
		} else
			action.setEnabled(true);
		editor = (SeqEditor) RootEditPartUtils.getEditorFromRSEMultiPageEditor(targetEditor);
	}

	public void run(IAction action) {
		if(editor==null) return;
		showInteractions();
	}

	public void selectionChanged(IAction action, ISelection selection) {}

	private void showInteractions() {

		try {
			new ProgressMonitorDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell()).run(true,false, new IRunnableWithProgress(){

				public void run(final IProgressMonitor monitor)
				throws InvocationTargetException, InterruptedException {
					monitor.beginTask("Displaying interactions...", IProgressMonitor.UNKNOWN);

					PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable(){
						public void run() {
							List<InstanceEditPart> instances = new ArrayList<InstanceEditPart>();
							DiagramEditPart diagramEP = editor.getDiagramController();
							for(Object child : diagramEP.getChildren()) {
								if (monitor.isCanceled()) return;
								if(!(child instanceof InstanceEditPart)) continue;
								InstanceEditPart instanceEP = (InstanceEditPart) child;
								((InstanceModel)instanceEP.getModel()).removeAllChildren();
								if(!instances.contains(instanceEP)) instances.add(instanceEP);
							}

							List<InstanceEditPart> instancesCopy = new ArrayList<InstanceEditPart>(instances);
							BreakableCommand addMultipleCallsCmd = new BreakableCommand("adding interactions", AnimateCallMadeCommand.class);
							for(InstanceEditPart instance1 : instances) {
								for(InstanceEditPart instance2 : instancesCopy) {
									if (monitor.isCanceled()) return;
									if(!instance1.equals(instance2))
										instance1.addMessagesTo((InstanceModel)instance2.getModel(), addMultipleCallsCmd);
								}
							}

							for(InstanceEditPart instance : instances) {
								if (monitor.isCanceled()) return;
								instance.removeIfDuplicateWithNoChildrenAndNoCorrespondingInstance();
							}

							diagramEP.execute(addMultipleCallsCmd);
						}

					});
					monitor.done();
				}
			});
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void init(IViewPart view) {
		// TODO Auto-generated method stub
		
	}
}
