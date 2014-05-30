package com.architexa.diagrams.strata.model.policy;

import java.io.IOException;

import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.rio.RdfDocumentWriter;

import com.architexa.diagrams.RSECore;
import com.architexa.diagrams.jdt.RJCore;
import com.architexa.diagrams.model.DiagramPolicy;
import com.architexa.diagrams.strata.model.DependencyRelation;
import com.architexa.store.ReloRdfRepository;
import com.architexa.store.StoreUtil;

public class DependencyRelDPolicy extends DiagramPolicy{
	////
	// basic setup
	////
	public static final String DefaultKey = "DependencyRelDPolicy";
	public static final DependencyRelDPolicy Type = new DependencyRelDPolicy();
	
	////
	// Policy Fields, Constructors and Methods 
	////
	
	public DependencyRelDPolicy() {}
	
	public static final URI isPinnedRelURI = RSECore.createRseUri("strata#isPinnedRel");
	
	@Override
	public void writeRDF(RdfDocumentWriter rdfWriter) throws IOException {
		if (getHostRel() instanceof DependencyRelation && ((DependencyRelation)getHostRel()).pinned == true) {
			rdfWriter.writeStatement(getHostRel().getInstanceRes(), isPinnedRelURI, StoreUtil.createMemLiteral("true"));
			URI virtualRel = RJCore.containmentBasedRefType;
			rdfWriter.writeStatement(getHostRel().getSrc().getArt().elementRes, virtualRel, getHostRel().getDest().getArt().elementRes);
		}
	}

	@Override
	public void readRDF(ReloRdfRepository queryRepo) {
		Value isPinnedRelValue = queryRepo.getStatement(getHostRel().getInstanceRes(), isPinnedRelURI, null).getObject();
		if (isPinnedRelValue == null) return;
		// we are only saving rels with a true state so this is ok
		if (getHostRel() instanceof DependencyRelation)
			 ((DependencyRelation)getHostRel()).pinned = true;	
	}
}
