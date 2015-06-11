package com.architexa.compat;

import org.eclipse.jdt.internal.ui.text.JavaElementPrefixPatternMatcher;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.SaveableHelper;

public class CompatUtils {
	
	public static boolean findBooleanMatchForOICSearch(JavaElementPrefixPatternMatcher matcher, TreeViewer treeViewer, Object element) {
		if (matcher == null || !(treeViewer instanceof TreeViewer))
			return true;

		String matchName= ((ILabelProvider) treeViewer.getLabelProvider()).getText(element);
		if (matchName != null && matcher.matches(matchName))
			return true;
		
		return false;
	}
	
	public static Object findElementMatchForOICSearch(JavaElementPrefixPatternMatcher matcher, Object element, ILabelProvider labelProvider) {
		if (matcher == null && element != null)
			return element;

		if (element != null) {
			String label= labelProvider.getText(element);
			if (matcher.matches(label))
				return element;
		}
		return null;
	}

	public static void rseSaveAction(IWorkbenchPart wbp) {
		IWorkbenchWindow window = wbp.getSite().getWorkbenchWindow(); 
		SaveableHelper.savePart((ISaveablePart)wbp, wbp, window, false);
	}

	
}
