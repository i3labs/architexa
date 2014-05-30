package com.architexa.diagrams.strata.cache;

import java.util.Set;

import com.architexa.diagrams.model.Artifact;
import com.architexa.store.ReloRdfRepository;


public class CUSupport {

	public static int getContainedCUs(Set<Artifact> containedObjs, ReloRdfRepository repo, DepNdx depCalculator, Artifact art) {
		int children = depCalculator.getChildrenSize(containedObjs, repo, art, StrataDepStrengthSummarizerProcessor.virtualContainerHeirarchy);
		return children;
	}

}
