package com.architexa.diagrams.all.ui.startup;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.PlatformUI;

import com.architexa.diagrams.ui.PluggableDiagramsSupport;
import com.architexa.diagrams.ui.PluggableDiagramsSupport.IRSEDiagramEngine;
import com.architexa.diagrams.ui.RSEMenuAction;
import com.architexa.diagrams.ui.SelectableAction;

public class OpenInDiagramMenuAction extends RSEMenuAction {
	public OpenInDiagramMenuAction() {}
	
	@Override
	protected List<? extends IAction> getMenuActions() {

		List<SelectableAction> actions = new ArrayList<SelectableAction>();
		
		try {
			ISelection selection = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().getSelection();

			AllUIStartup.initDiagramEngines();
			
			Set<IRSEDiagramEngine> diagEngines = PluggableDiagramsSupport.getRegisteredDiagramEngines();
			for (IRSEDiagramEngine diagramEngine : diagEngines) {
				
				SelectableAction action = diagramEngine.getOpenActionClass().newInstance();
				action.selectionChanged(action, selection);
				actions.add(action);
			}

		} catch (Throwable t) {
			t.printStackTrace();
		}

		return actions;
	}

}
