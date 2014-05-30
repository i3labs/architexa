package com.architexa.diagrams.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowPulldownDelegate;
import org.eclipse.ui.PlatformUI;

import com.architexa.diagrams.RSECore;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.ui.PluggableDiagramsSupport.IRSEDiagramEngine;
import com.architexa.diagrams.utils.RootEditPartUtils;
import com.architexa.rse.BuildStatus;


/**
 * 
 * @author Elizabeth L. Murnane
 * @author vineet
 *
 */
public class SwitchDiagramType implements IWorkbenchWindowPulldownDelegate {

	//public static String switchDiagramTypeSubMenuId = "seq.switchDiagramTypeSubMenu";

	public void init(IWorkbenchWindow window) {}
	public void dispose() {}
	public void run(IAction action) {}
	public void selectionChanged(IAction action, ISelection selection) {}

	public Menu getMenu(Control parent) {

		final IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		final IEditorPart activeEditor; 
		IEditorPart mpEditor = activeWorkbenchWindow.getActivePage().getActiveEditor();
		activeEditor = (IEditorPart) RootEditPartUtils.getEditorFromRSEMultiPageEditor(mpEditor);
		final List<ArtifactFragment> toAdd = new ArrayList<ArtifactFragment>();

		Set<IRSEDiagramEngine> diagEngines = PluggableDiagramsSupport.getRegisteredDiagramEngines();

		for (final IRSEDiagramEngine diagramEngine : diagEngines) {
			if (diagramEngine.getEditorClass().isInstance(activeEditor)) {
				toAdd.addAll(diagramEngine.getShownChildren((RSEEditor) activeEditor));
				break;
			}
		}

		Menu convertMenu = new Menu(parent);

		for (final IRSEDiagramEngine diagramEngine : diagEngines) {
			IAction toEngineAction = new Action("-> " + diagramEngine.diagramType(),
					diagramEngine.getImageDescriptor()) {
				@Override
				public void run() {
					BuildStatus.addUsage(diagramEngine.diagramUsageName());
					diagramEngine.openSwitchedDiagramEditor(toAdd);
				}
			};
			if (diagramEngine.diagramUsageName().equals("Strata"))
				toEngineAction.setEnabled(false);
			
			if (diagramEngine.getEditorClass().isInstance(activeEditor))
				toEngineAction.setEnabled(false);

			ActionContributionItem toEngineItem = new ActionContributionItem(toEngineAction);
			toEngineItem.fill(convertMenu, -1); 
		}

		return convertMenu;
	}

}
