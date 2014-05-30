package com.architexa.diagrams.all.ui.startup.welcome;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;



public class OpenWelcomePage implements IWorkbenchWindowActionDelegate {
	private IWorkbenchWindow window;
	public void run(IAction action) {
		openEditor(new CreateAtxaWelcomePageInput("atxaStart"), "com.architexa.diagrams.all.ui.startup.welcome.CreateAtxaWelcomePage"); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	protected void openEditor(String inputName, String editorId) {
		openEditor(new CreateAtxaWelcomePageInput(inputName), editorId);
	}
	protected void openEditor(IEditorInput input, String editorId) {
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		try {
			page.openEditor(input, editorId);
		} catch (PartInitException e) {
			System.out.println(e);
		}
	}
	
	protected IWorkbenchWindow getWindow() {
		return window;
	}
	/**
	 * @see IWorkbenchWindowActionDelegate#selectionChanged
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}
	/**
	 * @see IWorkbenchWindowActionDelegate#dispose
	 */
	public void dispose() {
	}
	/**
	 * @see IWorkbenchWindowActionDelegate#init
	 */
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}
}
