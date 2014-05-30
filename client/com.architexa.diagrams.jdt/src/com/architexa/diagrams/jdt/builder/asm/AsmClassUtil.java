package com.architexa.diagrams.jdt.builder.asm;

import org.openrdf.model.Resource;

import com.architexa.diagrams.RSECore;
import com.architexa.diagrams.jdt.RJCore;
import com.architexa.store.ReloRdfRepository;

/**
 * @author vineet
 *
 */
public class AsmClassUtil {

	/**
	 * @param rdfRepo
	 * @param myName
	 *            example: className: xlapis.util.Debug$Verbose (debug is a
	 *            class, and verbose is an inner class)
	 * @param interfaceType
	 * @return
	 */
	public static Resource getType(ReloRdfRepository rdfRepo, String myName, boolean interfaceType) {
		Resource myRes = getExtType(rdfRepo, myName);

		if (interfaceType)
			rdfRepo.addStatement(myRes, RJCore.isInterface, RJCore.interfaceType);
		else
			rdfRepo.addTypeStatement(myRes, RJCore.classType);

		return myRes;
	}
	
	// When we don't know much about the type - just put the containment
	// hierarchy down
	public static Resource getExtType(ReloRdfRepository rdfRepo, String myName) {
		Resource myRes = AsmUtil.toReloClassResource(rdfRepo, myName);

		Resource parentRes;
		// see if we have a nested class
		if (myName.indexOf('$') != -1) {
			parentRes = AsmClassUtil.getExtType(rdfRepo, myName.substring(0, myName.lastIndexOf('$')));
		} else {
			parentRes = AsmPackageSupport.getExternalPackage(
					rdfRepo, 
					AsmPackageSupport.getPckgNameFromFQClassName(myName));
		}
		
		rdfRepo.addStatement(parentRes, RSECore.contains, myRes);
		
		return myRes;
	}

}
