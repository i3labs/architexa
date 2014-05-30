package com.architexa.diagrams.jdt.builder.asm;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;

import com.architexa.diagrams.RSECore;
import com.architexa.diagrams.jdt.RJCore;
import com.architexa.diagrams.model.Artifact;
import com.architexa.store.ReloRdfRepository;

public class ContainmentOptimizer {

	public static Artifact getContainingPckg(ReloRdfRepository rdfRepo, Resource classOrMemberRes) {
		return new Artifact(getContainingPckgRes(rdfRepo, classOrMemberRes));
	}
	public static Resource getContainingPckgRes(ReloRdfRepository rdfRepo, Resource classOrMemberRes) {
    	// @tag optimized-for-db-perf
    	String name = ((URI) classOrMemberRes).getLocalName();
    	int packageNdx = name.indexOf('$');
		String use = "";
    	if (packageNdx == 0)
			use = "(default)";
		else
			use = name.substring(0, packageNdx);
    	return packageToResource(rdfRepo, use);
    	
    	// old way
		//// support case when parent has not yet been initialized
		////  in these cases we can't really find the containing package
		//if (givenArt == null || !rdfRepo.hasStatement(givenArt.elementRes, rdfRepo.rdfType, null)) {
		//	return null;
		//}
		//Resource givenArtType = givenArt.queryType(rdfRepo);
		//if (CUSupport.isPackage(givenArtType)) {
		//	return givenArt;
		//} else {
		//	return getContainingPckg(givenArt.queryParentArtifact(rdfRepo));
		//}
	}
	
	
    private static Resource packageToResource(ReloRdfRepository reloRepo, String pckgName) {
        return RSECore.idToResource(reloRepo, RJCore.jdtWkspcNS, pckgName);
    }

}
