package com.architexa.diagrams.jdt.compat;

import org.apache.log4j.Logger;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.ui.actions.SelectionConverter;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;

import com.architexa.diagrams.jdt.Activator;

public class SCUtils {
    private static final Logger logger = Activator.getLogger(SCUtils.class);

	public static final IJavaElement SelectionConverter_getInput(JavaEditor currentEditor) {
		// implements: SelectionConverter.getInput(currentEditor);

		// challenge is that in Eclipse 3.2 and 3.3 an IJavaElement is returned
		// while in Eclipse 3.4 and above an ITypeRoot is returned

		try {
			return (IJavaElement) SelectionConverter.class.getMethod("getInput", JavaEditor.class).invoke(null, currentEditor);
		} catch (Throwable e) {
			logger.error("Unexpected Exception", e);
			return null;
		}
	}
}
