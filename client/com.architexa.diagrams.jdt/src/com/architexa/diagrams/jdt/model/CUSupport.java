package com.architexa.diagrams.jdt.model;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;

import com.architexa.diagrams.jdt.RJCore;
import com.architexa.diagrams.model.Artifact;
import com.architexa.diagrams.services.PluggableTypes;
import com.architexa.diagrams.services.PluggableTypes.PluggableTypeInfo;
import com.architexa.store.ReloRdfRepository;

/**
 * Temporary class while CodeUnit cannot move here.
 * 
 * @author vineet
 */
public class CUSupport {
	public static final boolean isPackage(Resource typeRes) {
	    return RJCore.packageType.equals(typeRes);
	}
	public static final boolean isPackageFolder(Resource typeRes) {
	    return RJCore.indirectPckgDirType.equals(typeRes);
	}
	public static boolean isType(Resource typeRes) {
	    return (RJCore.classType.equals(typeRes) || RJCore.interfaceType.equals(typeRes));
	}
	public static boolean isProject(Resource typeRes){
		return RJCore.projectType.equals(typeRes);
	}
	public static boolean isGraphNode(Resource typeRes, Artifact art, ReloRdfRepository repo) {
		PluggableTypeInfo pti = PluggableTypes.getRegisteredTypeInfo(art.elementRes, repo, (URI) typeRes);
    	if (pti != null) return pti.isGraphNode; else return false;
	}

}
