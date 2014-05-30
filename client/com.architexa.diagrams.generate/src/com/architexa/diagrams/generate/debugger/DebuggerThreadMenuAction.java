package com.architexa.diagrams.generate.debugger;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;

import com.architexa.diagrams.generate.GeneratePlugin;
import com.architexa.diagrams.ui.RSEMenuAction;

public class DebuggerThreadMenuAction extends RSEMenuAction {

	@Override
	protected List<? extends IAction> getMenuActions() {

		DebuggerDiagramGenerator libraryCodeHiddenAction = new DebuggerDiagramGenerator();
		libraryCodeHiddenAction.setText("Library Code Hidden");
		ImageDescriptor hiddenImg = GeneratePlugin.getImageDescriptor("icons/jar_hidden.png");
		libraryCodeHiddenAction.setImageDescriptor(hiddenImg);
		libraryCodeHiddenAction.setId("generate.OpenFilteredTraceInChronoViewerAction");

		DebuggerDiagramGenerator libraryCodeGroupedAction = new DebuggerDiagramGenerator();
		libraryCodeGroupedAction.setText("Library Code Visible and Grouped");
		ImageDescriptor groupedImage = GeneratePlugin.getImageDescriptor("icons/jar_grouped.png");
		libraryCodeGroupedAction.setImageDescriptor(groupedImage);
		libraryCodeGroupedAction.setId("generate.OpenGroupedTraceInChronoViewerAction");

		DebuggerDiagramGenerator libraryCodeVisibleAction = new DebuggerDiagramGenerator();
		libraryCodeVisibleAction.setText("Library Code Visible and Ungrouped");
		ImageDescriptor visibleImage = GeneratePlugin.getImageDescriptor("icons/jar_visible.png");
		libraryCodeVisibleAction.setImageDescriptor(visibleImage);
		libraryCodeVisibleAction.setId("generate.OpenTraceInChronoViewerAction");

		List<IAction> actions = new ArrayList<IAction>();
		actions.add(libraryCodeHiddenAction);
		actions.add(libraryCodeGroupedAction);
		actions.add(libraryCodeVisibleAction);
		return actions;
	}

}
