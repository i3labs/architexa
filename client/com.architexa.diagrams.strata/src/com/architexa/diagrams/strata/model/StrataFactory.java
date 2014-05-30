/**
 * Copyright (c) 2007-2008 Architexa, Inc
 */ 
package com.architexa.diagrams.strata.model;

import org.apache.log4j.Logger;
import org.openrdf.model.Resource;

import com.architexa.diagrams.model.Artifact;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.model.ColorDPolicy;
import com.architexa.diagrams.strata.StrataPlugin;
import com.architexa.diagrams.strata.cache.StrataDepStrengthSummarizerProcessor;
import com.architexa.diagrams.strata.model.policy.ClosedContainerDPolicy;
import com.architexa.diagrams.strata.model.policy.ContainedClassSizeCacheDPolicy;
import com.architexa.diagrams.strata.model.policy.LayerPositionedDPolicy;
import com.architexa.diagrams.strata.model.policy.LayersDPolicy;
import com.architexa.store.ReloRdfRepository;


// @tag move-to-rootArt
public class StrataFactory {
	public static final Logger logger = StrataPlugin.getLogger(StrataFactory.class);

	private final ReloRdfRepository rdfRepo;

	public StrataFactory(ReloRdfRepository rdfRepo) {
		this.rdfRepo = rdfRepo;
	}

	public static ArtifactFragment initAF(ArtifactFragment artFrag) {
		ArtifactFragment.ensureInstalledPolicy(artFrag, ClosedContainerDPolicy.DefaultKey, ClosedContainerDPolicy.class);
		ArtifactFragment.ensureInstalledPolicy(artFrag, LayersDPolicy.DefaultKey, LayersDPolicy.class);
		ArtifactFragment.ensureInstalledPolicy(artFrag, LayerPositionedDPolicy.DefaultKey, LayerPositionedDPolicy.class);
		ArtifactFragment.ensureInstalledPolicy(artFrag, ContainedClassSizeCacheDPolicy.DefaultKey, ContainedClassSizeCacheDPolicy.class);
		ArtifactFragment.ensureInstalledPolicy(artFrag, ColorDPolicy.DefaultKey, ColorDPolicy.class);
	    return artFrag;
	}

	public ArtifactFragment createArtFrag(Artifact cuArt) {
		ArtifactFragment artFrag = null;
		if(StrataDepStrengthSummarizerProcessor.virtualContainer.equals(cuArt.queryType(rdfRepo)))
	    	artFrag = new EmbeddedFrag(cuArt);
		else
			artFrag =  new ArtifactFragment(cuArt);

	    return initAF(artFrag);
	}
	public ArtifactFragment createArtFrag(Resource _elementRes) {
		return createArtFrag(new Artifact(_elementRes));
	}
}
