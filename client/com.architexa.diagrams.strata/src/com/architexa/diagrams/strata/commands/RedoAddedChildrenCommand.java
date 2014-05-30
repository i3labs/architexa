/**
 * 
 */
package com.architexa.diagrams.strata.commands;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;

import com.architexa.diagrams.Activator;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.strata.model.DependencyRelation;
import com.architexa.diagrams.strata.model.StrataRootDoc;
import com.architexa.diagrams.ui.LongCommand;

public final class RedoAddedChildrenCommand extends LongCommand {
    static final Logger logger = Activator.getLogger(AddChildrenCommand.class);

    private final List<ArtifactFragment> childrenAF;
	private final ArtifactFragment parentAF;
	private List<DependencyRelation> newRels = null;
	private final StrataRootDoc rootDoc;

	public RedoAddedChildrenCommand(ArtifactFragment parentAF, StrataRootDoc rootDoc) {
		super("Re-Add Children");
		this.childrenAF = new ArrayList<ArtifactFragment>(parentAF.getShownChildren());
		this.parentAF = parentAF;
		this.rootDoc = rootDoc;		
	}

	@Override
	public void prep(IProgressMonitor monitor) {
		newRels = rootDoc.getRelationshipsToAdd(childrenAF, monitor);
	}

	@Override
	public void execute() { 
		if (newRels == null) {
			logger.error("Unexpected Error - not prepped");
			return;
		}
		for (ArtifactFragment childAF : childrenAF) {
			parentAF.removeShownChild(childAF);
		}
		rootDoc.addRelationships(newRels);
		parentAF.appendShownChildren(childrenAF);
		
	}

	@Override
	public void undo() {
		rootDoc.removeRelationships(newRels);
	}
}