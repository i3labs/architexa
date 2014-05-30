package com.architexa.diagrams.generate.team;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;

import com.architexa.diagrams.generate.GeneratePlugin;

/**
 * 
 * Handler for the pull-down in the History view that contains the commands
 * for generating a diagram from the changes made since the selected revision.
 *
 */
public class SyncPulldownHandler extends AbstractHandler {

	private static final Logger logger = GeneratePlugin.getLogger(SyncPulldownHandler.class);

	// Do not put override here as method is present in an interface (Possible bug in eclipse 3.3)
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			if(event.getCommand()==null) return null;
			String commandId=event.getCommand().getId();
			if(commandId==null) return null;

			List<IAction> uncommittedChangeActions = TeamMenuAction.getTeamActions();

			// Alert user if no revision history is open in the view
			if(uncommittedChangeActions==null || uncommittedChangeActions.size()==0) {
				alertUserNoHistory();
				return null;
			}

			// Determine the diagram type selected from the drop down and open
			// in that diagram the changes made since the selected revision
			for (IAction uncommittedChangeAction : uncommittedChangeActions) {
				if (!(uncommittedChangeAction instanceof UncommittedChangesDiagramGenerator)) continue;
				if(commandId.equals(((UncommittedChangesDiagramGenerator) uncommittedChangeAction).getDiagramEngineId()+".team")) {
					uncommittedChangeAction.run();
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
				"No Changes Currently Selected",
				null, 
				"Please select the java files below you would like to open in a diagram.", 
				MessageDialog.INFORMATION, 
				new String[]{"OK"}, 
				1).open();
	}

}
