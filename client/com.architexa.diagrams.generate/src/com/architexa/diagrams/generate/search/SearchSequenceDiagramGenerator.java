/**
 * 
 */
package com.architexa.diagrams.generate.search;

import java.util.List;

import org.eclipse.ui.IWorkbenchWindow;

import com.architexa.diagrams.chrono.ui.OpenInChronoAction;
import com.architexa.diagrams.model.ArtifactFragment;

public class SearchSequenceDiagramGenerator extends SearchDiagramGenerator {

	private static int maxToInclude = 6;

	@Override
	protected void openViz(IWorkbenchWindow activeWorkbenchWindow,
			List<ArtifactFragment> fragList) {
		OpenInChronoAction.openChronoViz(activeWorkbenchWindow, fragList, null, null, null);
	}

	@Override
	protected int getMaxToInclude() {
		return maxToInclude;
	}

}