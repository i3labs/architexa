package com.architexa.diagrams.strata.commands;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;

import com.architexa.diagrams.model.Artifact;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.strata.model.DependencyRelation;
import com.architexa.diagrams.strata.model.StrataRootDoc;
import com.architexa.diagrams.ui.LongCommand;

public class AddAllRelsCommand extends LongCommand {

	private StrataRootDoc rootDoc;
	private Map<Artifact, ArtifactFragment> addedArtToAFMap;
	//private List<Artifact> allArts;
	private List<List<DependencyRelation>> allRels;
	private List<DependencyRelation> newRels;
	private List<ArtifactFragment> selAFs;

	public AddAllRelsCommand(StrataRootDoc strataRootDoc, Map<Artifact, ArtifactFragment> addedArtToAFMap, List<Artifact> allArts) {
		super("Add Relationships");
		this.rootDoc = strataRootDoc;
		this.addedArtToAFMap= addedArtToAFMap;
		//this.allArts = allArts;
		allRels = new ArrayList<List<DependencyRelation>>();
	}

	public AddAllRelsCommand(StrataRootDoc strataRootDoc, Map<Artifact, ArtifactFragment> addedArtToAFMap, List<Artifact> allArts, List<ArtifactFragment> artFragList) {
		this(strataRootDoc, addedArtToAFMap, allArts);
		selAFs = artFragList;
	}

	@Override
	public void prep(IProgressMonitor monitor) {
		final Set<ArtifactFragment> afs = new HashSet<ArtifactFragment>();
//		for (Artifact art : allArts) {
		for (Artifact art : addedArtToAFMap.keySet()) {
			ArtifactFragment artFrag = getAF(art);
			if (artFrag!=null)
				afs.add(artFrag);
		}
		// TODO: improve strata performance for adding single packages/projects
		// with numerous nested children
		newRels = rootDoc.getRelationshipsToAdd(new ArrayList<ArtifactFragment>(afs), monitor, selAFs);
	}
	
	@Override
	public void execute() {
		allRels.add(newRels);
		rootDoc.addRelationships(newRels);
	}

	private ArtifactFragment getAF(Artifact art) {
		return addedArtToAFMap.get(art);
	}

}
