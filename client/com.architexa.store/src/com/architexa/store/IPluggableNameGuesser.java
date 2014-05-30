package com.architexa.store;

import org.openrdf.model.Resource;

public interface IPluggableNameGuesser {

	String getName(Resource elementRes, ReloRdfRepository repo);
}
