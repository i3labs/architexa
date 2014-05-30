package com.architexa.diagrams.jdt;

import org.apache.commons.collections.Predicate;
import org.openrdf.model.Resource;

import com.architexa.diagrams.services.PluggableTypeGuesser;
import com.architexa.store.ReloRdfRepository;
import com.architexa.store.StoreUtil;

public class Filters {

	public static final Predicate getAccessFilter(ReloRdfRepository repo, Resource access) {
	    return StoreUtil.filterSubjectResPred(repo, RJCore.access, access);
	}
	
	public static final Predicate getTypeFilter(final ReloRdfRepository repo, final Resource typeRes) {
		final PluggableTypeGuesser guesser = new PluggableTypeGuesser();
        return new Predicate() {
        	Predicate basePredicate = StoreUtil.filterSubjectResPred(repo, repo.rdfType, typeRes);
            public boolean evaluate(Object arg0) {
            	Resource guessedTypeRes = guesser.getType((Resource)arg0, repo);
            	if (guessedTypeRes != null && guessedTypeRes.equals(typeRes)) 
            		return true;
            	return basePredicate.evaluate(arg0);
            }};
	}

}
