package com.architexa.diagrams.jdt.builder.asm;

import java.util.LinkedHashMap;
import java.util.Map;

import org.objectweb.asm.Type;
import org.openrdf.model.Resource;

import com.architexa.diagrams.RSECore;
import com.architexa.diagrams.jdt.RJCore;
import com.architexa.store.ReloRdfRepository;

/**
 * The 'external' elements do not have the <b>initialized</b> field set. They
 * are the bare minimum elements that are added when a reference is being made
 * to an external object. Bare minimum means label and containment information.
 * 
 * @author vineet
 * 
 */
public class AsmPackageSupport {

	public static String getPckgNameFromFQClassName(String fqClassName) {
		int packageNdx = fqClassName.lastIndexOf('.');
		if (packageNdx == -1)
			return "(default)";
		else
			return fqClassName.substring(0, packageNdx);
	}
	public static String getPckgNameFromClassID(String classID) {
		int packageNdx = classID.indexOf('$');
		if (packageNdx == -1)
			return "(default)";
		else
			return classID.substring(0, packageNdx);
	}

	private static Map<String, Resource> pckgNameToRDF = new LinkedHashMap<String, Resource>();
	
	// external packages are those that we have a dependency to, since we are
	// not building them, we don't know what project they belong to and they are
	// therefore not 'initialized'
	private static Map<String, Resource> extPckgToRDF = new LinkedHashMap<String, Resource>();
	
	//In order to have package statements added in after repository is reinitialized
	public static void clearCaches(){
		pckgNameToRDF = new LinkedHashMap<String, Resource>();
		extPckgToRDF = new LinkedHashMap<String, Resource>();
	}

	public static Resource getPackage(ReloRdfRepository rdfRepo, Type classType, Resource projectResource) {
		return getPackage(rdfRepo, getPckgNameFromFQClassName(classType.getClassName()), projectResource);
	}
	public static Resource getPackage(ReloRdfRepository rdfRepo, String packageName, Resource projectResource) {
		Resource packageRes = pckgNameToRDF.get(packageName);
		if (packageRes != null) return packageRes;
		
		return addPackage(rdfRepo, packageName, projectResource);
	}
	public static Resource getExternalPackage(ReloRdfRepository rdfRepo, String packageName) {
		Resource packageRes = pckgNameToRDF.get(packageName);
		if (packageRes == null) 
			packageRes = extPckgToRDF.get(packageName);
		if (packageRes != null) return packageRes;

		return addExternalPackage(rdfRepo, packageName);
	}
	private static Resource addPackage(ReloRdfRepository rdfRepo, String packageName, Resource projectResource) {
		Resource packageRes = addExternalPackage(rdfRepo, packageName);

		rdfRepo.addStatement(projectResource, RSECore.contains, packageRes);
		// Do not remove this statement
		rdfRepo.addStatement(packageRes, RSECore.initialized, true);

		pckgNameToRDF.put(packageName, packageRes);
		return packageRes;
	}
	private static Resource addExternalPackage(ReloRdfRepository rdfRepo, String packageName) {
		Resource packageRes = AsmUtil.toWkspcResource(rdfRepo, packageName);
		
		rdfRepo.addNameStatement(packageRes, RSECore.name, packageName);
		rdfRepo.addTypeStatement(packageRes, RJCore.packageType);
		rdfRepo.addStatement(packageRes, RSECore.initialized, true);

		extPckgToRDF.put(packageName, packageRes);
		return packageRes;
	}


}
