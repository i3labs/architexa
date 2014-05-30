/**
 * 
 */
package com.architexa.diagrams.strata.commands;

import java.util.ArrayList;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;

import com.architexa.diagrams.model.Artifact;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.strata.model.StrataRootDoc;

public class ShowAllParentsCommand extends ShowChildrenCommand {
	private ArrayList<ArtifactFragment> afsToShow = new ArrayList<ArtifactFragment>();
	private Map<Artifact, ArtifactFragment> addedArtToAFMap;

	/**
	 * @param addedArtToAFMap 
	 * 
	 */
	
	public ShowAllParentsCommand(ArtifactFragment af, String label, boolean state, StrataRootDoc strataRootDoc, Map<Artifact, ArtifactFragment> addedArtToAFMap) {
		super(af, label, state, strataRootDoc);
		this.addedArtToAFMap= addedArtToAFMap;
	}

	
	@Override
	public void prep(IProgressMonitor monitor) {
	}
	
	
	@Override
	public void execute() {
		ArtifactFragment thisAF = addedArtToAFMap.get(af.getArt());
		showAllParents(thisAF);
		for (ArtifactFragment af : afsToShow) {
			super.execute(af);	
		}
	}
	
	@Override
	public void undo() { 
	}

	private void showAllParents(ArtifactFragment childAF) {
		if (!(childAF instanceof StrataRootDoc) && childAF!=null && childAF.getParentArt()!=null) {
			ArtifactFragment parentArt = childAF.getParentArt();
			showAllParents(parentArt);
			afsToShow.add(childAF);
		}
	}
	
	@Override
	protected void addRels() {
		
	}
}