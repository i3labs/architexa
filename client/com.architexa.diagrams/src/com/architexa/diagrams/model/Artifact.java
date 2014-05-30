package com.architexa.diagrams.model;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.Transformer;
import org.apache.log4j.Logger;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.sesame.sail.StatementIterator;
import org.openrdf.sesame.sailimpl.memory.BNodeNode;

import com.architexa.diagrams.Activator;
import com.architexa.diagrams.RSECore;
import com.architexa.diagrams.services.PluggableTypeGuesser;
import com.architexa.diagrams.utils.DbgRes;
import com.architexa.store.PckgDirRepo;
import com.architexa.store.ReloRdfRepository;


/**
 * @author vineet
 * 
 * Should be identical to an RDF Resource, except that the term Resource is
 * ambiguous.
 * 
 * This class also plays the role of the common ancestor for ArtFragments and
 * DerArts. Since DerArts need to be stored and loaded read/writeRDF is located
 * here.
 * 
 * TODO: instanceRes is stored here. It should not.
 * 
 * Classes should not be inheriting from this this class, unless to provide an
 * extension to the basic repository based functionality. Extensions are still
 * instantiated from the store.
 * 
 */
public class Artifact {
    static final Logger logger = Activator.getLogger(Artifact.class);

    //////////////////////////
	// basic resource wrapping
	//////////////////////////

	public /*final*/ Resource elementRes = null;

	public Artifact(Resource _elementRes) {
		elementRes = _elementRes;
		if (_elementRes == null) 
			throw new IllegalArgumentException();
	}

    /**
     * Used when a default constructor is used to create the class
     */
    public void init(Resource modelRes) {
        elementRes = modelRes;
        if (elementRes == null) throw new IllegalArgumentException();
    }

	// automatically generated
	// TODO: optimize (and test) because for us elementRes is always non-null
    
    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((elementRes == null) ? 0 : elementRes.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Artifact other = (Artifact) obj;
		if (elementRes == null) {
			if (other.elementRes != null)
				return false;
		} else if (!elementRes.equals(other.elementRes))
			return false;
		return true;
	}

	////////////////////
	// debugging support
	////////////////////

    DbgRes tag = new DbgRes(Artifact.class, this);
	public String getTrailer() {
	    return tag.getTrailer();
	}


	@Override
    public String toString() {
	    return elementRes.toString();
	}

    
    ////////////////////
	// rdf access support
	////////////////////

	/**
	 * Support cases where we still need to make code robus - we just don't
	 * raise an error, but flag a warning
	 */
	public Resource queryWarnedType(ReloRdfRepository repo) {
	    return (Resource)queryType(repo);
	}
	public Resource queryOptionalType(ReloRdfRepository repo) {
		Resource guessedType = PluggableTypeGuesser.getType(elementRes, repo);
		if  (guessedType!= null){
			return guessedType;	
    	}
    	else {
    		Statement typeStmt = repo.getWarnedRequiredProperty(elementRes, repo.rdfType);
    		if (typeStmt != null)
    			return (Resource) typeStmt.getObject();
    		return null;
    	}
	}
	public Resource queryType(ReloRdfRepository repo) {
	    try {
	    	Resource type = PluggableTypeGuesser.getType(elementRes, repo);
	    	if  (type!= null)
	    		return type;	
	    	else if (elementRes instanceof BNodeNode)
	    		return null;
	    	else 
	    		return (Resource) repo.getRequiredProperty(elementRes, repo.rdfType).getObject();
	    } catch (Exception e) {
	        dumpProperties(repo, elementRes);
	        logger.error("Element: " + elementRes + " does not have a type.", e);
	        return null;
	    }
	}

    public static void dumpProperties(ReloRdfRepository rdfModel, Resource elementRes) {
        logger.info("Properties for: " + elementRes);
        StatementIterator si = rdfModel.getStatements(elementRes, null, (Value) null);
	    while (si.hasNext()) {
	        org.openrdf.model.Statement stmt = si.next();
	        logger.info("   " + stmt.getPredicate() + "  " + stmt.getObject());
	    }
        si.close();
    }

    
    // @tag design-issue: should we deal with containment hierarchy
	public String queryName(ReloRdfRepository repo) {
		return repo.queryName(elementRes);
	}


	// Note the input collection is changed by the method
	@SuppressWarnings("unchecked")
    public static List<Artifact> transformResourcesToArtifacts(List<Resource> in) {
        CollectionUtils.transform(in, new Transformer() {
            public Object transform(Object arg0) {
                return new Artifact((Resource)arg0);
            }});
        return (List<Artifact>) (List<?>) in;
	}
	
	public Artifact queryParentArtifact(ReloRdfRepository repo) {
		Resource parentRes = RSECore.queryParentResource(repo, elementRes);
		if (parentRes == null) return null;
		return new Artifact(parentRes);
	}
	
	// @tag design-issue: It might be nice to filter here to make sure that all
	// the return types are creatable - but then what about those that want to
	// just 'browse the model'
	public List<Artifact> queryChildrenArtifacts(ReloRdfRepository repo) {
		return queryChildrenArtifacts(repo, null);
	}

    public List<Artifact> queryChildrenArtifacts(ReloRdfRepository repo, Predicate filterPred) {
        return queryArtList(repo, DirectedRel.getFwd(RSECore.contains), filterPred);
	}
    
    public List<Artifact> queryArtList(ReloRdfRepository repo, DirectedRel rel, Predicate filter) {
        List<Resource> retValAsResource = new LinkedList<Resource> ();
        if (rel.isFwd)
            repo.getResourcesFor(retValAsResource, elementRes, rel.res, null);
        else
            repo.getResourcesFor(retValAsResource, null, rel.res, elementRes);
        CollectionUtils.filter(retValAsResource, filter);
        return transformResourcesToArtifacts(retValAsResource);
    }

    public List<Artifact> queryArtList(ReloRdfRepository repo, DirectedRel rel) {
        return queryArtList(repo, rel, null);
    }

    public List<Artifact> queryPckgDirContainsArtList(ReloRdfRepository repo, DirectedRel rel) {
    	return queryPckgDirContainsArtList(PckgDirRepo.getPDR(repo, RSECore.pckgDirContains), rel);
    }
    public List<Artifact> queryPckgDirContainsArtList(PckgDirRepo repo, DirectedRel rel) {
    	List<Resource> retValAsResource = repo.queryArtList(elementRes, rel.isFwd, rel.res);
    	return transformResourcesToArtifacts(retValAsResource);
    }

    public List<Artifact> queryConnectedArtifactsList(ReloRdfRepository repo, Set<URI> filteredPredicates) {
        List<Artifact> retValArt = new LinkedList<Artifact> ();
        StatementIterator si;

        si = repo.getStatements(elementRes, null, null);
        while (si.hasNext()) {
            Statement stmt = si.next();
            
            if (filteredPredicates.contains(stmt.getPredicate())) continue;
            if (!(stmt.getObject() instanceof Resource)) continue;
                
            retValArt.add(new Artifact((Resource) stmt.getObject()));
        }
        si.close();

        si = repo.getStatements(null, null, elementRes);
        while (si.hasNext()) {
            Statement stmt = si.next();
            
            if (filteredPredicates.contains(stmt.getPredicate())) continue;
            if (!(stmt.getSubject() instanceof Resource)) continue;
                
            retValArt.add(new Artifact((Resource) stmt.getSubject()));
        }
        si.close();

        return retValArt;
    }

	public boolean isInitialized(ReloRdfRepository repo) {
		return RSECore.isInitialized(repo, elementRes);
	}

    
}
