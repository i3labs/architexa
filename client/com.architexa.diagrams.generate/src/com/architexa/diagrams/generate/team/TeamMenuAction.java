package com.architexa.diagrams.generate.team;

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
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import com.architexa.diagrams.generate.team.cvs.CVSUncommittedChangesDiagramGenerator;
import com.architexa.diagrams.jdt.JDTSelectionUtils;
import com.architexa.diagrams.ui.PluggableDiagramsSupport;
import com.architexa.diagrams.ui.PluggableDiagramsSupport.IRSEDiagramEngine;
import com.architexa.diagrams.ui.RSEMenuAction;

/**
 * 
 * @author Elizabeth L. Murnane
 *
 */


public class TeamMenuAction extends RSEMenuAction {

	public static UncommittedChangesDiagramGenerator teamSyncGeneratorType = new CVSUncommittedChangesDiagramGenerator();
	 
	@Override
	public Menu getMenu(Menu parent) {
		Menu menu = super.getMenu(parent);

		// Disable entire menu if selected element is not under CVS control
		boolean enabled = true;
		IJavaElement selectedElement = JDTSelectionUtils.getSelectedJDTElement();
		if(selectedElement==null || 
				(!(selectedElement.getResource() instanceof IFile) && !(selectedElement.getResource() instanceof IFolder) && !(selectedElement.getResource() instanceof IProject))) {
			enabled = false;
		} else {
			RepositoryProvider repoProvider = RepositoryProvider.getProvider(selectedElement.getResource().getProject());
			if(repoProvider==null) enabled = false;
			else enabled = CVSProviderPlugin.getTypeId().equals(repoProvider.getID());
		}
		menu.setEnabled(enabled);

		return menu;
	}

	@Override
	protected List<IAction> getMenuActions() {
		return getTeamActions();
	}
	
	public static List<IAction> getTeamActions() {
		List<IJavaElement> selectedElement = JDTSelectionUtils.getSelectedJDTElements(false);

		List<IAction> actions = new ArrayList<IAction>();
		Set<IRSEDiagramEngine> diagEngines = PluggableDiagramsSupport.getRegisteredDiagramEngines();
		for (final IRSEDiagramEngine diagramEngine : diagEngines) {
			UncommittedChangesDiagramGenerator action;
			try {
				if (isSVNView())
					action = teamSyncGeneratorType.getClass().newInstance().init(selectedElement, diagramEngine);
				else
					action = new CVSUncommittedChangesDiagramGenerator(selectedElement, diagramEngine);
				actions.add(action);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			}

		}
		return actions;
	}
	
	
	private static boolean isSVNView() {
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IViewReference[] views = page.getViewReferences();
		for (IViewReference view : views) {
			if (view.getContentDescription().contains("SVN")) return true;
		}
		return false;
	}
}
