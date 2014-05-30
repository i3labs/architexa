package com.architexa.diagrams.generate.debugger;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.architexa.diagrams.generate.GeneratePlugin;

/**
 * 
 * Handler for the pull-down that contains the commands for
 * generating a sequence diagram from a stack trace with library 
 * code either hidden, visible and grouped, or visible and ungrouped.
 *
 */
public class DebuggerPulldownHandler extends AbstractHandler {

	private static final Logger logger = GeneratePlugin.getLogger(DebuggerPulldownHandler.class);

	// Do not put override here as method is present in an interface (Possible bug in eclipse 3.3)
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			if(event.getCommand()==null) return null;
			String commandId=event.getCommand().getId();
			if(commandId==null) return null;

			// Command id indicates which dropdown menu option was selected
			// and therefore how library code should be handled in the diagram
			// (hidden, visible and grouped, or visible and ungrouped).
			DebuggerDiagramGenerator diagramGenAction = new DebuggerDiagramGenerator();
			diagramGenAction.setId(commandId);

			// If dropdown button simply pushed instead of something being 
			// selected from the dropdown menu, treating library code grouped
			// as the default and generating that type of sequence diagram.
			if("com.architexa.diagrams.generate.debugger.pulldownCommand".equals(commandId)) {
				diagramGenAction.setId("generate.OpenGroupedTraceInChronoViewerAction");
			}

			diagramGenAction.run();

		} catch(Exception e) {
			logger.error("Unexpected exception while generating diagram from stack trace", e);
		}
		return null;
	}

}
