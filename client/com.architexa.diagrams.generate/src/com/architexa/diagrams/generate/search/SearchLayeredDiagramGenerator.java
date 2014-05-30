/**
 * 
 */
package com.architexa.diagrams.generate.search;

import java.util.List;

import org.eclipse.ui.IWorkbenchWindow;

import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.strata.ui.OpenStrata;

public class SearchLayeredDiagramGenerator extends SearchDiagramGenerator {

	@Override
	protected void openViz(IWorkbenchWindow activeWorkbenchWindow,
			List<ArtifactFragment> fragList) {
		OpenStrata.buildAndOpenStrataDoc(activeWorkbenchWindow, fragList, null, null, null, null);
	}

	@Override
	protected int getMaxToInclude() {
		return noLimit;
	}

}