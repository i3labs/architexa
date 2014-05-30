package com.architexa.diagrams.ui;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;

import com.architexa.diagrams.utils.RootEditPartUtils;
import com.architexa.org.eclipse.gef.editparts.AbstractGraphicalEditPart;


// provides support for working with both ReloViews and Editors
public abstract class RSEAction implements IEditorActionDelegate, IViewActionDelegate {

	//TODO need to find a better place for this field
	public final static String linkedTrackerId = "com.architexa.diagrams.ui.linkedExploration";
	protected AbstractGraphicalEditPart rc = null;
	protected IToolBarManager tbm = null;
	protected IWorkbenchPart rseWbPart = null;

	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		targetEditor = (IEditorPart) RootEditPartUtils.getEditorFromRSEMultiPageEditor(targetEditor);
		if (targetEditor == null) return;
		targetEditor = (IEditorPart) RootEditPartUtils.getEditorFromRSEMultiPageEditor(targetEditor);
		rseWbPart = targetEditor;
	    RSEEditor rseEditor = (RSEEditor) targetEditor;
	    
        rc = rseEditor.getRootController();
        tbm  = rseEditor.getEditorSite().getActionBars().getToolBarManager();
        initAction();
	}
	
	public void init(IViewPart view) {
		rseWbPart = view;
		RSEView rseView = (RSEView) view;
		
        rc = rseView.getRootController();
        tbm  = rseView.getViewSite().getActionBars().getToolBarManager();
        initAction();
	}
	
	public void initAction() {}

	public abstract void run(IAction action);

	public void selectionChanged(IAction action, ISelection selection) {}


}
