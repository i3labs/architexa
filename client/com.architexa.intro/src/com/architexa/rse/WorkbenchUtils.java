package com.architexa.rse;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

public class WorkbenchUtils {
	
	public static IWorkbenchPage getActivePage() {
		if (PlatformUI.getWorkbench() == null) return null;
		if (PlatformUI.getWorkbench().getActiveWorkbenchWindow() == null) return null;
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
	}

	public static IWorkbenchPart getActivePart() {
		if (PlatformUI.getWorkbench() == null) return null;
		if (PlatformUI.getWorkbench().getActiveWorkbenchWindow() == null) return null;
		if (PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage() == null) return null;
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart();
	}

	public static Shell getActiveWorkbenchWindowShell() {
		if (PlatformUI.getWorkbench() == null) return null;
		if (PlatformUI.getWorkbench().getActiveWorkbenchWindow() == null) return null;
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
	}

}
