package com.architexa.diagrams.ui.menus;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;

import com.architexa.collab.CollabPlugin;
import com.architexa.collab.UIUtils;
import com.architexa.rse.BuildStatus;

public class OpenDocumentingTasks extends CompoundContributionItem {
	private static final String[][] tasks = {
		{"Document A Code Concept",
			"Every codebase consists of groups of concept and ideas. To document effectively you must provide a cumulative picture of these concepts.",
			"http://www.architexa.com/user-guide/Tasks/Document_a_code_concept"},
		{"Document While You Work",
			"In order to make documenting simpler and effortless, Architexa provides an easy way for you to create a diagram while exploring the code.",
			"http://www.architexa.com/user-guide/Tasks/Document_while_you_work_by_creating_a_diagram_in_the_background"},
		{"Quickly Explain Concepts To Coworkers",
			"rchitexa provide a great means for explaining features, code concepts, or architecture to co-workers, managers, or business folk especially when they can not be in the same room with you.",
			"http://www.architexa.com/user-guide/Tasks/Quickly_explain_concepts_to_coworkers/managers"},
		{"Keep Documentation Up To Date",
			"Documentation can be difficult to maintain effectively, especially on a large codebase. Use Architexa's server to organize and maintain your documentation.",
			"http://www.architexa.com/user-guide/Tasks/Keep_documentation_up_to_date"}};
	
	private List<ActionContributionItem> getActions() {
		List<ActionContributionItem> actions = new ArrayList<ActionContributionItem>();
		
		for (final String[] task : tasks) {
			try {
			IAction newAction = new Action(task[0]) {
				@Override
				public void run() {
					BuildStatus.addUsage("OpenDocumentingTasks");
					//MessageDialog dialog = UIUtils.promptDialogWithLinkEmbedded(MessageDialog.INFORMATION, task[0], task[1], task[2], true);
					//dialog.open();
					
					IWorkbenchBrowserSupport browserSupport = CollabPlugin.getDefault().getWorkbench().getBrowserSupport();
				    IWebBrowser browser;
					try {
						browser = browserSupport.createBrowser(IWorkbenchBrowserSupport.LOCATION_BAR, null,task[0], task[0]);
						URL url2 = new URL(task[2]);
						browser.openURL(url2);
					} catch (PartInitException e) {
						e.printStackTrace();
					} catch (MalformedURLException e) {
						e.printStackTrace();
					}
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
}
