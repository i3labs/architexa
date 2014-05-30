package com.architexa.diagrams.generate.search;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

/**
 * 
 * Handler for the pull-down that will simply contain the commands
 * for generating each type of diagram from the search results.
 *
 */
public class SearchPulldownHandler extends AbstractHandler {

	// Do not put override here as method is present in an interface (Possible bug in eclipse 3.3)
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		return null;
	}

}
