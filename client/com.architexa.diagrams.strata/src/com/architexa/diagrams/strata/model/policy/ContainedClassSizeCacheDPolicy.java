package com.architexa.diagrams.strata.model.policy;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.openrdf.rio.RdfDocumentWriter;

import com.architexa.diagrams.model.Artifact;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.model.DiagramPolicy;
import com.architexa.diagrams.strata.StrataPlugin;
import com.architexa.diagrams.strata.cache.CUSupport;
import com.architexa.diagrams.strata.model.StrataRootDoc;
import com.architexa.store.ReloRdfRepository;


// this is a cache policy - we don't save anything
public class ContainedClassSizeCacheDPolicy extends DiagramPolicy {
	////
	// basic setup
	////
	static final Logger logger = StrataPlugin.getLogger(ContainedClassSizeCacheDPolicy.class);
	public static final String DefaultKey = "ContainedClassSizesCachePolicy";
	public static final ContainedClassSizeCacheDPolicy Type = new ContainedClassSizeCacheDPolicy();

	////
	// Policy Fields, Constructors and Methods 
	////
	int containedClassSize = -1;

	public ContainedClassSizeCacheDPolicy() {
	}

	public int getContainedClassSize(ReloRdfRepository repo) {
		if (containedClassSize == -1) {
			getContainedCUs(new HashSet<Artifact> (100), repo);
		}
		return containedClassSize;
	}
	private void getContainedCUs(Set<Artifact> containedObjs, ReloRdfRepository repo) {
		containedClassSize = CUSupport.getContainedCUs(containedObjs, repo, ((StrataRootDoc)getHostAF().getRootArt()).getDepNdx(), getHostAF().getArt());
	}
//	private static boolean isType(ReloRdfRepository repo, Artifact art) {
//		// todo: need to double check why so many artifacts don't have a Type associated with them
//		Statement s = repo.getStatement(art.elementRes, repo.rdfType, null);
//		return CodeUnit.isType(repo, (Resource) s.getObject());
//	}

	
	@Override
	public void writeRDF(RdfDocumentWriter rdfWriter) throws IOException {
	}
	@Override
	public void readRDF(ReloRdfRepository queryRepo) {
	}


	////
	// Static Helpers 
	////
	public static int get(ArtifactFragment artFrag, ReloRdfRepository repo) {
		ContainedClassSizeCacheDPolicy containedClassSizeCacheDPolicy = artFrag.getTypedDiagramPolicy(Type, DefaultKey);
		if (containedClassSizeCacheDPolicy != null) return containedClassSizeCacheDPolicy.getContainedClassSize(repo);
		return -1;
	}
}
