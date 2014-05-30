package com.architexa.diagrams.generate.team;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.team.internal.ui.history.GenericHistoryView;
import org.eclipse.team.ui.history.IHistoryPage;
import org.eclipse.team.ui.history.IHistoryPageSite;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.PartSite;
import org.eclipse.ui.internal.PopupMenuExtender;

import com.architexa.diagrams.chrono.util.UIUtil;
import com.architexa.diagrams.generate.GeneratePlugin;
import com.architexa.diagrams.generate.compat.PMEUtil;
import com.architexa.diagrams.ui.PluggableDiagramsSupport;
import com.architexa.diagrams.ui.RSEMenuAction;
import com.architexa.diagrams.ui.PluggableDiagramsSupport.IRSEDiagramEngine;


/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public abstract class SyncDiagramGenerator {

	UncommittedChangesDiagramGenerator uncommittedChangesDiagramGenerator = null;
	static List<RevisionViewGenerateAction> diagramGenerateActions;

	public SyncDiagramGenerator(UncommittedChangesDiagramGenerator generator) {
		uncommittedChangesDiagramGenerator = generator;
	}

	public void initialize() {
		final IWorkbench workbench = PlatformUI.getWorkbench();
		workbench.getDisplay().asyncExec(new Runnable() {
			public void run() {
				IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
				if (window == null) return;

				window.getActivePage().addPartListener(new IPartListener() {

					public void partActivated(IWorkbenchPart part) {
						if(!(part instanceof GenericHistoryView)) return;

						final GenericHistoryView view = (GenericHistoryView) part;
						final IWorkbench workbench = PlatformUI.getWorkbench();
						workbench.getDisplay().asyncExec(new Runnable() {
							public void run() {
								createGenerateAction(view);
							}
						});
					}

					public void partBroughtToTop(IWorkbenchPart part) {}
					public void partClosed(IWorkbenchPart part) {}
					public void partDeactivated(IWorkbenchPart part) {}
					public void partOpened(IWorkbenchPart part) {}
				});
			}
		});
	}

	public void createGenerateAction(GenericHistoryView view) {
		if(view==null) return;

		// Add option to context menu
		IHistoryPageSite parentSite = view.getHistoryPage().getHistoryPageSite();
		ArrayList<?> menuExtenders = lookupmenuExtenders((PartSite)parentSite.getPart().getSite());
		if(menuExtenders==null) return;

		diagramGenerateActions = new ArrayList<RevisionViewGenerateAction>();
		Set<IRSEDiagramEngine> diagEngines = PluggableDiagramsSupport.getRegisteredDiagramEngines();
		for (final IRSEDiagramEngine diagramEngine : diagEngines) {
			RevisionViewGenerateAction diagramGenerateAction = new RevisionViewGenerateAction(uncommittedChangesDiagramGenerator, diagramEngine);
			diagramGenerateActions.add(diagramGenerateAction);
		}

		for(final Object obj : menuExtenders) {
			if(!(obj instanceof PopupMenuExtender)) continue;
			final PopupMenuExtender pme = (PopupMenuExtender) obj;
			PMEUtil.getManager(pme).addMenuListener(new IMenuListener() {
				public void menuAboutToShow(IMenuManager menuMgr) {
					if(UIUtil.menuAlreadyContainsAction(PMEUtil.getManager(pme), RevisionViewGenerateAction.actionId)) return;

					PMEUtil.getManager(pme).add(new Separator());

					ImageDescriptor rseIcon = GeneratePlugin.getImageDescriptor("icons/rse-document.png");
					RSEMenuAction openChangesAsSubMenu = new RSEMenuAction(RevisionViewGenerateAction.actionId, "Open Changes Made Since This Revision As ", rseIcon) {
						@Override
						protected List<IAction> getMenuActions() {
							return new ArrayList<IAction>(diagramGenerateActions);
						}
					};
					openChangesAsSubMenu.setMenuCreator(openChangesAsSubMenu);

					PMEUtil.getManager(pme).add(openChangesAsSubMenu);
				}
			});
		}

		listenForSelections(view.getHistoryPage(), diagramGenerateActions);
	}

	public List<RevisionViewGenerateAction> getDiagramGenerateActions() {
		return diagramGenerateActions;
	}

	protected abstract void listenForSelections(IHistoryPage historyPage, List<RevisionViewGenerateAction> diagramGenerateActions);

	private static ArrayList<?> lookupmenuExtenders(PartSite site) {
		Field[] fields = PartSite.class.getDeclaredFields();
		for (Field f : fields) {
			if (!f.getName().equals("menuExtenders")) continue;
			f.setAccessible(true);
			try {
				Object retVal = f.get(site);
				return ArrayList.class.cast(retVal);
			} catch (Exception e) {}
		}
		return null;
	}

}