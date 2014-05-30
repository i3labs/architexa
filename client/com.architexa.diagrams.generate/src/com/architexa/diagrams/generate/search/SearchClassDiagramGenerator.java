/**
 * 
 */
package com.architexa.diagrams.generate.search;

import java.util.List;

import org.eclipse.ui.IWorkbenchWindow;

import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.relo.jdt.actions.OpenForBrowsingAction;

public class SearchClassDiagramGenerator extends SearchDiagramGenerator {

	private static int maxToInclude = 20;

	@Override
	protected void openViz(IWorkbenchWindow activeWorkbenchWindow,
			List<ArtifactFragment> fragList) {
		OpenForBrowsingAction.openReloViz(activeWorkbenchWindow, fragList, null, null, null, null, null);
	}

	@Override
	protected int getMaxToInclude() {
		return maxToInclude;
	}

}