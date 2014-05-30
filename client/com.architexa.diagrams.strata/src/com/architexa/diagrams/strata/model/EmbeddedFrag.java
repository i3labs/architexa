/**
 * Copyright (c) 2007-2008 Architexa, Inc
 */ 
package com.architexa.diagrams.strata.model;

import com.architexa.diagrams.RSECore;
import com.architexa.diagrams.model.Artifact;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.store.ReloRdfRepository;


// used during persistance
public class EmbeddedFrag extends ArtifactFragment {

	public EmbeddedFrag(Artifact _node) {
		super(_node);
	}

	public String getLabel(ReloRdfRepository repo) {
		String docID = RSECore.resourceToId(repo, this.getArt().elementRes, false);
		int lastNdx = docID.lastIndexOf("/");
		if (lastNdx == -1) lastNdx = 0;
		docID = docID.substring(lastNdx + 1);
		docID = docID.substring(0, docID.length() - ".strata".length());
		return ":" + docID;
	}

}
