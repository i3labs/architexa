package com.architexa.diagrams.ui.menus;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.ide.IDE;

import com.architexa.collab.UIUtils;
import com.architexa.diagrams.utils.RecentlyCreatedDiagramUtils;

public class OpenRecentlyCreatedDiagrams extends CompoundContributionItem {
	
	private List<ActionContributionItem> getActions() {
		List<ActionContributionItem> actions = new ArrayList<ActionContributionItem>();
        IWorkbench wb = PlatformUI.getWorkbench();
	    IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
 	    final IWorkbenchPage page = win.getActivePage();
	    File[] recentDiagramFiles = RecentlyCreatedDiagramUtils.getRecentItems();
	    
	    if (recentDiagramFiles==null || recentDiagramFiles.length <=0) {
	    	IAction a = new Action("No Recent Diagrams") {};
	    	a.setEnabled(false);
	    	ActionContributionItem a2 = new ActionContributionItem(a);
			actions.add(a2);
	    	return actions;
	    }
	    List<File> sortedRecentDiagrams = Arrays.asList(recentDiagramFiles);
	    Collections.sort(sortedRecentDiagrams, new Comparator<File>() {
			public int compare(File o1, File o2) {
				String fileName1 = o1.getName();
				Long time1 =  Long.parseLong(fileName1.substring(fileName1.lastIndexOf("DATE~")+5,fileName1.lastIndexOf("TYPE~")));
				String fileName2 = o2.getName();
				Long time2 =  Long.parseLong(fileName2.substring(fileName2.lastIndexOf("DATE~")+5,fileName2.lastIndexOf("TYPE~")));
				return time2.compareTo(time1);
			}
		});
	    
		for (int i=0; i<recentDiagramFiles.length; i++){
			final File file = recentDiagramFiles[i]; 
			
			ImageDescriptor image =  null;
			if (RecentlyCreatedDiagramUtils.getImageDescriptor(file) != null) {
				image  = RecentlyCreatedDiagramUtils.getImageDescriptor(file);
			}
			
			IAction a = new Action(RecentlyCreatedDiagramUtils.getDisplayName(file.getName()), image) {
	    		@Override
	    		public void run() {
	    			File fileToOpen = new File(RecentlyCreatedDiagramUtils.recentDiagramPath +file.getName());
	    			if (fileToOpen.exists() && fileToOpen.isFile()) {
	    			    IFileStore fileStore = EFS.getLocalFileSystem().getStore(fileToOpen.toURI());
	    			    try {
	    			        IDE.openEditorOnFileStore( page, fileStore );
	    			    } catch ( PartInitException e ) {
	    			    	System.err.println("Error opening editor for recent diagram");
	    					e.printStackTrace();
	    			    }
	    			} else {
	    				UIUtils.openErrorPromptDialog("Error opening Recent Diagram"," Could not find file associated with this recent diagram.");
	    			}
	    		}
			};
			ActionContributionItem newActionItem = new ActionContributionItem(a );
			actions.add(newActionItem);
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
