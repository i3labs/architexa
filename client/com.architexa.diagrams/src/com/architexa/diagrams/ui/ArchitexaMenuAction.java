package com.architexa.diagrams.ui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;

import com.architexa.diagrams.Activator;
import com.architexa.rse.FeedbackDialog;

public class ArchitexaMenuAction extends RSEMenuAction implements IViewActionDelegate {
	static final Logger logger = Activator.getLogger(ArchitexaMenuAction.class);

	public ArchitexaMenuAction() {
	}

	public void init(IViewPart view) {
	}
	
	public static class Init implements IStartup {
		public void earlyStartup() {
			try {
				earlyStartupInternal();
			} catch (Throwable t) {
				logger.error("Unexpected Error", t);
			}
		}
		private void earlyStartupInternal() {
			FeedbackDialog.FeedbackDlgAction.init(registerAction(new FeedbackDialog.FeedbackDlgAction()));
		}
	}

	private static Set<IAction> registeredActions = new HashSet<IAction>();
	public static IAction registerAction(IAction action) {
		registeredActions.add(action);
		return action;
	}
	public static IAction registerAction(final IActionDelegate actionDel) {
		Action action = new Action() {
			@Override
			public void run() {
				actionDel.run(this);
			}
		};
		return registerAction(action);
	}

	@Override
	protected List<? extends IAction> getMenuActions() {
		try {
			ISelection selection = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().getSelection();

			for (IAction action : registeredActions) {
				if (action instanceof IActionDelegate) 
					((IActionDelegate)action).selectionChanged(action, selection);
			}
		} catch (Throwable t) {
			logger.error("Unexpected Exception", t);
		}
		
		List<IAction> registeredActionList = new ArrayList<IAction>(registeredActions);
		return registeredActionList;
	}


}
