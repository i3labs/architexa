package com.architexa.diagrams.chrono.util;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;

/**
 * 
 * @author Elizabeth L. Murnane
 * 
 *         Most of this class is used to find a editor/menu manager for adding
 *         options for opening diagrams from tabs/subversion etc. This is now
 *         handled from within the plugin.xml and is depricated in eclipse 4.2 (Seth)
 */
public class UIUtil {

//	public static MenuManager getStdEditorMenuManager(IWorkbenchWindow window) {
//		return CompatUtils.getStdEditorMenuManager(window);
//	}

	public static boolean menuAlreadyContainsAction(MenuManager manager, String actionId) {
		for(IContributionItem item : manager.getItems()) {
			if(!(item instanceof ActionContributionItem)) continue;

			String itemId = ((ActionContributionItem)item).getAction().getId();
			if(actionId.equals(itemId)) return true;
		}
		return false;
	}

	public static boolean menuAlreadyContainsContribution(MenuManager manager, String id) {
		for(IContributionItem item : manager.getItems()) {
			if(id.equals(item.getId())) return true;
		}
		return false;
	}

//	private static DefaultStackPresentationSite lookUppresentationSite(PartStack  loc) {
//		Field[] fields = PartStack.class.getDeclaredFields();
//		for (Field f : fields) {
//			if (!f.getName().equals("presentationSite")) continue;
//			f.setAccessible(true);
//			try {
//				Object retVal = f.get(loc);
//				return (DefaultStackPresentationSite) retVal;
//			} catch (Exception e) {}
//
//		}
//		return null;
//	}
//
//	private static ISystemMenu lookUpsystemMenu(TabbedStackPresentation  loc) {
//		Field[] fields = TabbedStackPresentation.class.getDeclaredFields();
//		for (Field f : fields) {
//			if (!f.getName().equals("systemMenu")) continue;
//			f.setAccessible(true);
//			try {
//				Object retVal = f.get(loc);
//				return (ISystemMenu) retVal;
//			} catch (Exception e) {}
//
//		}
//		return null;
//	}
//
//	private static MenuManager lookUpmenuManager(StandardViewSystemMenu  loc) {
//		Field[] fields = StandardViewSystemMenu.class.getDeclaredFields();
//		for (Field f : fields) {
//			if (!f.getName().equals("menuManager")) continue;
//			f.setAccessible(true);
//			try {
//				Object retVal = f.get(loc);
//				return (MenuManager) retVal;
//			} catch (Exception e) {}
//
//		}
//		return null;
//	}

}
