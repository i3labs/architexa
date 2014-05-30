/**
 * 
 */
package com.architexa.diagrams.utils;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.rio.RdfDocumentWriter;
import org.openrdf.vocabulary.RDF;

import com.architexa.rse.RSE;
import com.architexa.store.ReloRdfRepository;

/**
 * Helper class to pull out all the namespaces in the beginning. This makes the
 * output alot more readable.
 * 
 * @author vineet
 */
public class NamespaceDeclaratorWriter implements RdfDocumentWriter {
	private final RdfDocumentWriter rdfWriter;
	Set<String> declaredNamespaces = new HashSet<String>(5);

	public NamespaceDeclaratorWriter(RdfDocumentWriter rdfWriter) throws IOException {
		this.rdfWriter = rdfWriter;
		
		// some defaults to start with 
	    setNamespace("rdf", RDF.NAMESPACE);
	    setNamespace("atxa", ReloRdfRepository.atxaRdfNamespace);
	}

	private void checkURIForNSDeclaration(URI newURI) throws IOException {
		String ns = newURI.getNamespace();
		if (declaredNamespaces.contains(ns) || ns.length() == 0)
			return;
		if (ns.startsWith(ReloRdfRepository.atxaRdfNamespace)) {
			String prefixTrailer = ns.substring(
					ReloRdfRepository.atxaRdfNamespace.length(),
					ns.length() - 1);
			setNamespace(RSE.appShortTag + "-" + prefixTrailer, ns);
		}
	}

	public void setNamespace(String prefix, String name) throws IOException {
		declaredNamespaces.add(name);
		rdfWriter.setNamespace(prefix, name);
	}

	public void writeStatement(Resource subject, URI predicate, Value object) throws IOException {
		if (subject instanceof URI)
			checkURIForNSDeclaration((URI) subject);
		
		checkURIForNSDeclaration(predicate);
		
		if (object instanceof URI)
			checkURIForNSDeclaration((URI) object);
	}

	public void writeComment(String comment) throws IOException {
	}

	public void startDocument() throws IOException {
	}

	public void endDocument() throws IOException {
	}
}