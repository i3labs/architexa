/**
 * 
 */
package com.architexa.diagrams.services;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.openrdf.model.Resource;

import com.architexa.diagrams.Activator;
import com.architexa.store.IPluggableNameGuesser;
import com.architexa.store.ReloRdfRepository;

public class PluggableNameGuesser implements IPluggableNameGuesser {
    static final Logger logger = Activator.getLogger(PluggableNameGuesser.class);

	private static Set<NameGuesser> registeredNameGuessers = new HashSet<NameGuesser>();
	
	public static interface NameGuesser {
		String getName(Resource elementRes, ReloRdfRepository repo); 
	}
	
	public String getName(Resource elementRes, ReloRdfRepository repo) {
		String retVal = null;
		for (NameGuesser ng : registeredNameGuessers) {
			String name = ng.getName(elementRes, repo);
			if (name!= null)
				retVal = name;
		}
		return retVal;
	}
	
	public static NameGuesser registerNameGuesser(NameGuesser ng) {
		registeredNameGuessers.add(ng);
		return ng;
	}

}