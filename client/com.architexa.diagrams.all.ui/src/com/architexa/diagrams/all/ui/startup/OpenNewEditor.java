package com.architexa.diagrams.all.ui.startup;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowPulldownDelegate;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.CompoundContributionItem;

import com.architexa.diagrams.RSECore;
import com.architexa.diagrams.chrono.sequence.ChronoDiagramEngine;
import com.architexa.diagrams.editors.RSEMultiPageEditor;
import com.architexa.diagrams.relo.jdt.ReloDiagramEngine;
import com.architexa.diagrams.strata.StrataDiagramEngine;
import com.architexa.diagrams.ui.PluggableDiagramsSupport;
import com.architexa.diagrams.ui.PluggableDiagramsSupport.IRSEDiagramEngine;
import com.architexa.rse.BuildStatus;

/**
 * 
 * @author Elizabeth L. Murnane
 * @author vineet
 *
 */
public class OpenNewEditor extends CompoundContributionItem implements IWorkbenchWindowPulldownDelegate {
	public Menu getMenu(Control parent) {
		Menu menu = new Menu(parent);
		for (ActionContributionItem menuAction : new ArrayList<ActionContributionItem >(getActions())) {
			menuAction.fill(menu, -1);
		}
		return menu;
	}


	private void openEditor(IEditorInput editorInput, String editorId) {
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		try {
			page.openEditor(editorInput, editorId);
		} catch (PartInitException e) {
			e.printStackTrace();
		}
	}
	
	private List<ActionContributionItem> getActions() {
		List<ActionContributionItem> actions = new ArrayList<ActionContributionItem>();
		
		AllUIStartup.initDiagramEngines();
		
		List<IRSEDiagramEngine> diagEngines = new ArrayList<IRSEDiagramEngine>(PluggableDiagramsSupport.getRegisteredDiagramEngines());
		for (final IRSEDiagramEngine diagramEngine : new ArrayList<IRSEDiagramEngine >(diagEngines)) {
			try {
			IAction newAction = new Action("New " + diagramEngine.diagramType(), diagramEngine.getNewEditorImageDescriptor()) {
				@Override
				public void run() {
					BuildStatus.addUsage(diagramEngine.diagramUsageName());

//					openEditor(diagramEngine.newEditorInput(), diagramEngine.editorId());
					openEditor(diagramEngine.newEditorInput(), RSEMultiPageEditor.editorId);
				}
			};
			if (newAction==null) continue;
			ActionContributionItem newActionItem = new ActionContributionItem(newAction);
			actions.add(newActionItem);
			} catch (Exception e) {
				continue;
			}
		}
		return actions;
	}


	@Override
	protected IContributionItem[] getContributionItems() {
		
		int i = 0;
		ArrayList<ActionContributionItem> actions = new ArrayList<ActionContributionItem >(getActions());
		IContributionItem[] list = new IContributionItem[actions.size()];
		for (ActionContributionItem obj : actions) {
			IContributionItem item = ((IContributionItem) obj);
			if (item == null) continue;
			list[i] = item;
			i++;
		}
		return list;
	}

	@Override
	public void dispose() {}
	public void init(IWorkbenchWindow window) {}
	public void run(IAction action) {}
	public void selectionChanged(IAction action, ISelection selection) {}

}
