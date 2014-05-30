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

public class OpenSharingTasks extends CompoundContributionItem {
	private static final String[][] tasks = {
		{"Share Via Email",
			"Email diagrams to specific team members to share insights",
			"http://www.architexa.com/user-guide/Sharing/Share_via_Email"}};
	
	private List<ActionContributionItem> getActions() {
		List<ActionContributionItem> actions = new ArrayList<ActionContributionItem>();
		
		for (final String[] task : tasks) {
			try {
			IAction newAction = new Action(task[0]) {
				@Override
				public void run() {
					BuildStatus.addUsage("OpenSharingTasks");
					//MessageDialog dialog = UIUtils.promptDialogWithLinkEmbedded(MessageDialog.INFORMATION, task[0], task[1], task[2], true);
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
					//dialog.open();
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
