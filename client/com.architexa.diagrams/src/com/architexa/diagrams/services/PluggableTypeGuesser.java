/**
 * 
 */
package com.architexa.diagrams.services;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.openrdf.model.Resource;

import com.architexa.diagrams.Activator;
import com.architexa.store.ReloRdfRepository;

public class PluggableTypeGuesser {
    static final Logger logger = Activator.getLogger(PluggableTypeGuesser.class);

	private static Set<TypeGuesser> registeredTypeGuessers = new HashSet<TypeGuesser>();
	
	public static interface TypeGuesser {
		Resource getType(Resource elementRes, ReloRdfRepository repo); 
	}
	
	public static Resource getType(Resource elementRes, ReloRdfRepository repo) {
		Resource retVal = null;
		for (TypeGuesser tg : registeredTypeGuessers) {
			Resource type = tg.getType(elementRes, repo);
			if (type != null) 
				retVal = type;
		}
		return retVal;
	}
	
	public static TypeGuesser registerTypeGuesser(TypeGuesser tg) {
		registeredTypeGuessers.add(tg);
		return tg;
	}

}