package com.architexa.diagrams.ui;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;

import com.architexa.diagrams.ui.PluggableDiagramsSupport.IRSEDiagramEngine;
import com.architexa.diagrams.utils.RootEditPartUtils;

public class RSELinkedTrackerDelegator extends RSEAction{

	private static RSEAction currentTracker;
	Map<String, RSEAction> idToTrackerMap = new HashMap<String, RSEAction>();
	
	@Override
	public void run(IAction action) {
		if (currentTracker != null)
			currentTracker.run(action);
	}
	
	@Override
	public void init(IViewPart view) {
		if (currentTracker != null)
			currentTracker.init(view);
	}
	
	@Override
	public void initAction() {
		if (currentTracker != null)
			currentTracker.initAction();
	}
	
	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		if (currentTracker != null)
			currentTracker.selectionChanged(action, selection);
	}
	
	@Override
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		if (targetEditor == null)
			targetEditor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		if (targetEditor == null) return;
		targetEditor = (IEditorPart) RootEditPartUtils.getEditorFromRSEMultiPageEditor(targetEditor);
		if (!(targetEditor.getEditorInput() instanceof RSEShareableDiagramEditorInput))
			return;

		String diagramType = ((RSEShareableDiagramEditorInput)targetEditor.getEditorInput()).getDiagramType();
		
		setCurrentTracker(diagramType, targetEditor);
		if (currentTracker != null)
			currentTracker.setActiveEditor(action, targetEditor);
		super.setActiveEditor(action, targetEditor);
	}

	private void setCurrentTracker(String diagramType, IEditorPart activeEditor) {
		if (idToTrackerMap.containsKey(diagramType) && idToTrackerMap.get(diagramType) != null) 
			currentTracker = idToTrackerMap.get(diagramType);
		else {
			for (IRSEDiagramEngine engine : PluggableDiagramsSupport.getRegisteredDiagramEngines()) {
				if (engine.editorId().equals(diagramType)) {
					currentTracker = engine.getTracker();
					idToTrackerMap.put(diagramType, engine.getTracker());
				}
			}
		}
	}
	
}
