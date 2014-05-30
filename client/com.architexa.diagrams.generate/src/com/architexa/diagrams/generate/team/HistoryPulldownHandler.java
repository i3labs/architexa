package com.architexa.diagrams.generate.team;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;

import com.architexa.diagrams.generate.GeneratePlugin;

/**
 * 
 * Handler for the pull-down in the History view that contains the commands
 * for generating a diagram from the changes made since the selected revision.
 *
 */
public class HistoryPulldownHandler extends AbstractHandler {

	private static final Logger logger = GeneratePlugin.getLogger(HistoryPulldownHandler.class);

	// Do not put override here as method is present in an interface (Possible bug in eclipse 3.3)
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			if(event.getCommand()==null) return null;
			String commandId=event.getCommand().getId();
			if(commandId==null) return null;

			List<RevisionViewGenerateAction> generateActions = 
				RevisionDiagramGenerator.diagramGenerateActions;

			// Alert user if no revision history is open in the view
			if(generateActions==null || generateActions.size()==0) {
				alertUserNoHistory();
				return null;
			}

			// Alert user if he has not selected a change set
			if(!generateActions.get(0).hasSelection()) {
				alertUserNoSelection();
				return null;
			}

			// Determine the diagram type selected from the drop down and open
			// in that diagram the changes made in the selected revision
			for (RevisionViewGenerateAction generateAction : generateActions) {
				if(commandId.equals(generateAction.getDiagramEngineId())) {
					generateAction.run();
					return null;
				}
			}
		} catch(Exception e) {
			logger.error("Unexpected exception while generating diagram from stack trace", e);
		}
		return null;
	}

	private void alertUserNoHistory() {
		new MessageDialog(
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), 
				"Open Changes Made In Change Set(s)",
				null, 
				"No revision history is open.", 
				MessageDialog.INFORMATION, 
				new String[]{"OK"}, 
				1).open();
	}

	private void alertUserNoSelection() {
		new MessageDialog(
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), 
				"Open Changes Made In Change Set(s)",
				null, 
				"Please select a change set.", 
				MessageDialog.INFORMATION, 
				new String[]{"OK"}, 
				1).open();
	}

}
