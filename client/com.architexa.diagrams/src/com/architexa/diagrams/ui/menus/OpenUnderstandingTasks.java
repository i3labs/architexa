package com.architexa.diagrams.ui.menus;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;

import com.architexa.collab.CollabPlugin;
import com.architexa.rse.BuildStatus;

public class OpenUnderstandingTasks extends CompoundContributionItem {
	private static final String[][] tasks = {
		{"Explore From Code Overviews To Details",
				"Getting up to speed when working on relatively unfamiliar code can be hard. Architexa allows you to quickly generate helpful diagrams to start from an overview of the code and then dive into the details of it.",
				"http://www.architexa.com/user-guide/Tasks/Understand_a_new_project,_library,_or_section_of_code"},
		{"Find Cycles In Your Architecture",
				"Architectural cycles can cause a number of headaches for developers. Spotting them early can save a lot of time and effort when it comes time to refactor the codebase.",
				"http://www.architexa.com/user-guide/Tasks/Find_cycles_in_your_architecture"},
		{"Understand Search Results With A Diagram",
				"Understand the details of a specific word or phrase by searching for it and opening the results in a diagram",
				"http://www.architexa.com/user-guide/Tasks/Searching_and_opening_the_results_in_a_diagram"},
		{"Debug Easily By Using Diagrams To See Control Flow",
				"Use the Debug view to see a Thread's stack trace (the list of method calls made by the thread that leads to a breakpoint you have set), there are two ways to open it in a sequence diagram - via a menu in the Debug view toolbar and via a menu in the Thread context menu:",
				"http://www.architexa.com/user-guide/Tasks/Debug_easily_by_using_diagrams_to_see_control_flow"},
		{"Review Code Easily",
				"To facilitate code maintenance, developers need to control change and manage multiple versions of code. This is a challenge even for the most experienced developer, especially considering today's increasing iteration speed due to agile development. Architexa provides source repository integration with CVS and SVN to make code reviews faster, easier, and less painful.",
				"http://www.architexa.com/user-guide/Tasks/Have_easier_code_reviews_by_understanding_changes_with_diagrams"},
	};
	
	
	private List<ActionContributionItem> getActions() {
		List<ActionContributionItem> actions = new ArrayList<ActionContributionItem>();
		
		for (final String[] task : tasks) {
			try {
			IAction newAction = new Action(task[0]) {
				@Override
				public void run() {
					BuildStatus.addUsage("OpenUnderstandingTasks");
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
