package com.architexa.diagrams.generate.tabs;

import org.eclipse.jface.action.Action;

import com.architexa.diagrams.ui.PluggableDiagramsSupport.IRSEDiagramEngine;


/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public class SystemMenuGenerateNavHistory extends Action {

	public static String actionId = "seq.SystemMenuGenerateNavHistory";

	private IRSEDiagramEngine diagramEngine;

	public SystemMenuGenerateNavHistory(IRSEDiagramEngine diagramEngine) {
		setId(actionId);
		setText(diagramEngine.diagramType());
		setImageDescriptor(diagramEngine.getImageDescriptor());
		this.diagramEngine = diagramEngine;
	}

	@Override
	public void run() {
		OpenTabsDiagramGenerator.generateDiagram(diagramEngine);
	}

	public boolean shouldBeVisible() {
		return true;
	}

	public void update() {
		setEnabled(OpenTabsDiagramGenerator.tabsAreOpen());
	}

	public void dispose() {}

}