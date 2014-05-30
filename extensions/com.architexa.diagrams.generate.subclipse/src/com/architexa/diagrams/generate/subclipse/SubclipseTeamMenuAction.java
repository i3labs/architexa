package com.architexa.diagrams.generate.subclipse;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.team.core.RepositoryProvider;
import org.tigris.subversion.subclipse.core.SVNProviderPlugin;

import com.architexa.diagrams.generate.team.TeamMenuAction;
import com.architexa.diagrams.generate.team.UncommittedChangesDiagramGenerator;
import com.architexa.diagrams.jdt.JDTSelectionUtils;
import com.architexa.diagrams.ui.PluggableDiagramsSupport;
import com.architexa.diagrams.ui.PluggableDiagramsSupport.IRSEDiagramEngine;

/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public class SubclipseTeamMenuAction extends TeamMenuAction {

	@Override
	public Menu getMenu(Menu parent) {
		Menu menu = super.getMenu(parent);

		// Disable entire menu if selected element is not under SVN control
		boolean enabled = true;
		IJavaElement selectedElement = JDTSelectionUtils.getSelectedJDTElement();
		if(selectedElement==null || 
				(!(selectedElement.getResource() instanceof IFile) 
						&& !(selectedElement.getResource() instanceof IFolder) 
						&& !(selectedElement.getResource() instanceof IProject))) {
			enabled = false;
		} else {
			RepositoryProvider repoProvider = RepositoryProvider.getProvider(selectedElement.getResource().getProject());
			if(repoProvider==null) enabled = false;
			else enabled = SVNProviderPlugin.getTypeId().equals(repoProvider.getID());
		}
		menu.setEnabled(enabled);

		return menu;
	}


	@Override
	protected List<IAction> getMenuActions() {
		List<IJavaElement> selectedElements = JDTSelectionUtils.getSelectedJDTElements(false);

		List<IAction> actions = new ArrayList<IAction>();
		Set<IRSEDiagramEngine> diagEngines = PluggableDiagramsSupport.getRegisteredDiagramEngines();
		for (final IRSEDiagramEngine diagramEngine : diagEngines) {
			UncommittedChangesDiagramGenerator action = new SubclipseUncommittedChangesDiagramGenerator(selectedElements, diagramEngine);
			actions.add(action);
		}
		return actions;
	}

}
