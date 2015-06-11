package com.architexa.compat;

import java.lang.reflect.Field;

import org.eclipse.jdt.internal.ui.util.StringMatcher;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.DefaultStackPresentationSite;
import org.eclipse.ui.internal.EditorAreaHelper;
import org.eclipse.ui.internal.EditorStack;
import org.eclipse.ui.internal.PartStack;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.internal.presentations.util.ISystemMenu;
import org.eclipse.ui.internal.presentations.util.StandardEditorSystemMenu;
import org.eclipse.ui.internal.presentations.util.StandardViewSystemMenu;
import org.eclipse.ui.internal.presentations.util.TabbedStackPresentation;


public class CompatUtils {
	
	public static boolean findBooleanMatchForOICSearch(StringMatcher matcher, TreeViewer treeViewer, Object element) {
		if (matcher == null || !(treeViewer instanceof TreeViewer))
			return true;

		String matchName= ((ILabelProvider) treeViewer.getLabelProvider()).getText(element);
		if (matchName != null && matcher.match(matchName))
			return true;
		
		return false;
	}
	
	public static Object findElementMatchForOICSearch(StringMatcher matcher, Object element, ILabelProvider labelProvider) {
		if (matcher == null && element != null)
			return element;

		if (element != null) {
			String label= labelProvider.getText(element);
			if (matcher.match(label))
				return element;
		}
		return null;
	}

	public static void rseSaveAction(IWorkbenchPart workbenchPart) {
		((WorkbenchPage) workbenchPart.getSite().getPage()).getEditorManager().savePart((ISaveablePart)workbenchPart, workbenchPart, false);
	}
	
	public static MenuManager getStdEditorMenuManager(IWorkbenchWindow window) {
		WorkbenchWindow workbenchWindow;
		if(window instanceof WorkbenchWindow) workbenchWindow = (WorkbenchWindow) window;
		else workbenchWindow = (WorkbenchWindow) PlatformUI.getWorkbench().getActiveWorkbenchWindow();

		EditorStack stack = ((EditorAreaHelper)(((WorkbenchPage) workbenchWindow.getActivePage()).getEditorPresentation())).getActiveWorkbook();
		DefaultStackPresentationSite presentationSite = lookUppresentationSite(stack);
		if(presentationSite==null) return null;

		// StackPresentation may not be a TabbedStackPresentation. For example,
		// if using the Extended VS Presentation plugin for Eclipse, it will
		// be a de.loskutov.eclipseskins.presentation.VSEditorStackPresentation
		if(!(presentationSite.getPresentation() instanceof TabbedStackPresentation)) return null;

		TabbedStackPresentation presentation = (TabbedStackPresentation)presentationSite.getPresentation();
		ISystemMenu systemMenu = lookUpsystemMenu(presentation);
		if(systemMenu==null) return null;

		StandardEditorSystemMenu stdEditorSystemMenu = (StandardEditorSystemMenu) systemMenu;
		MenuManager stdEditorMenuManager = lookUpmenuManager(stdEditorSystemMenu);
		return stdEditorMenuManager;
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	private static ISystemMenu lookUpsystemMenu(TabbedStackPresentation  loc) {
		Field[] fields = TabbedStackPresentation.class.getDeclaredFields();
		for (Field f : fields) {
			if (!f.getName().equals("systemMenu")) continue;
			f.setAccessible(true);
			try {
				Object retVal = f.get(loc);
				return (ISystemMenu) retVal;
			} catch (Exception e) {}

		}
		return null;
	}
	private static MenuManager lookUpmenuManager(StandardViewSystemMenu  loc) {
		Field[] fields = StandardViewSystemMenu.class.getDeclaredFields();
		for (Field f : fields) {
			if (!f.getName().equals("menuManager")) continue;
			f.setAccessible(true);
			try {
				Object retVal = f.get(loc);
				return (MenuManager) retVal;
			} catch (Exception e) {}

		}
		return null;
	}
	private static DefaultStackPresentationSite lookUppresentationSite(PartStack  loc) {
		Field[] fields = PartStack.class.getDeclaredFields();
		for (Field f : fields) {
			if (!f.getName().equals("presentationSite")) continue;
			f.setAccessible(true);
			try {
				Object retVal = f.get(loc);
				return (DefaultStackPresentationSite) retVal;
			} catch (Exception e) {}

		}
		return null;
	}

	
}
