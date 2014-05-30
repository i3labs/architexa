package com.architexa.diagrams.model;

import java.io.IOException;

import org.openrdf.rio.RdfDocumentWriter;

import com.architexa.store.ReloRdfRepository;



public abstract class DiagramPolicy {
	

	private Object host;
	
	public void setHost(Object host) {
		this.host = host;
	}

	public ArtifactFragment getHostAF() {
		return (ArtifactFragment) host;
	}
	public ArtifactRel getHostRel() {
		return (ArtifactRel) host;
	}


	public void writeRDF(RdfDocumentWriter rdfWriter) throws IOException {
	}

	public void readRDF(ReloRdfRepository queryRepo) {
	}
	

}
