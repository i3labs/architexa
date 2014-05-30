package com.architexa.diagrams.generate.team;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;

import com.architexa.diagrams.generate.GeneratePlugin;
import com.architexa.diagrams.ui.PluggableDiagramsSupport.IRSEDiagramEngine;


/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public class RevisionViewGenerateAction extends Action {

	private static final Logger logger = GeneratePlugin.getLogger(RevisionViewGenerateAction.class);
	
	public static String actionId = "seq.RevisionViewGenerateAction";

//	private ChangeSet selectedChangeSet;
	private IRSEDiagramEngine diagramEngine;
	private List selectedChangeSetList;

	private UncommittedChangesDiagramGenerator generator = null;


	public RevisionViewGenerateAction(UncommittedChangesDiagramGenerator generator, IRSEDiagramEngine diagramEngine) {
		setId(actionId);

		setText(diagramEngine.diagramType()); 
		setImageDescriptor(diagramEngine.getImageDescriptor());

		this.generator = generator;
		this.diagramEngine = diagramEngine;
	}

//	public void setSelection(ChangeSet logEntry) {
//		this.selectedChangeSet = logEntry;
//	}
	
	public void setSelection(List logEntry) {
		this.selectedChangeSetList = logEntry;
	}

	public void clearSelection() {
//		this.selectedChangeSet = null;
		this.selectedChangeSetList.clear();
	}

	public boolean hasSelection() {
		return selectedChangeSetList.size() > 0;
//		return selectedChangeSet != null;
	}

	@Override
	public void run() {

		if (selectedChangeSetList == null || selectedChangeSetList.size() == 0) {
			logger.error("Selection Error");
			return;
		}	
		generator.createDiagramOfChanges(selectedChangeSetList, null, diagramEngine);
	}

	public boolean shouldBeVisible() {
		return true;
	}

	public String getDiagramEngineId() {
		if(diagramEngine==null) return null;
		return diagramEngine.editorId();
	}

	public void update() {}
	public void dispose() {}

}
