/*
 * Created on Mar 4, 2005
 */
package com.architexa.diagrams;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;

import com.architexa.diagrams.model.DirectedRel;
import com.architexa.rse.RSE;
import com.architexa.store.ReloRdfRepository;
import com.architexa.store.StoreUtil;



/**
 * @author vineet
 *
 */
public class RSECore {
    static final Logger logger = Activator.getLogger(RSECore.class);

    public static final URI trueStatement = createRseUri("jdt#true");
    
//    public static boolean isExploratorySrvr = false;
    
    private final static String atxaRdfNamespace = ReloRdfRepository.atxaRdfNamespace;
	public static final String PLUGIN_ID = Activator.PLUGIN_ID;

    public static final URI createRseUri(String str) {
    	return StoreUtil.createMemURI(atxaRdfNamespace + str);
    }

    // system properties
    public static final URI id = createRseUri("core#id");
    public static final URI controller = createRseUri("core#controller");
    public static final URI model = createRseUri("core#model");
    public static final URI initialized = createRseUri("core#initialized");
    public static final URI contains = createRseUri("core#contains");
    public static final URI name = createRseUri("core#name");
    public static final URI classSig = createRseUri("core#classSig");
    public static final URI instanceName = createRseUri("core#instanceName");
    public static final URI detailsNode = createRseUri("core#detailsNode");

    // types
    public static final URI node = createRseUri("core#node");
    public static final URI link = createRseUri("core#link");

    // file saving, sharing, etc.
    public static final URI docRoot = createRseUri("core#docRoot");
    public static final URI reloFile = createRseUri("core#reloFile");
    public static final URI chronoFile = createRseUri("core#chronoFile");
    public static final URI strataFile = createRseUri("core#strataFile");
    public static final URI rdfCreator = createRseUri("core#creator");
    
    // control flow
    public static final URI controlFlowType = createRseUri("core#controlflow");
    public static final URI controlFlowName = createRseUri("core#controlflowname");
    public static final URI ifBlock = createRseUri("core#ifBlock");
    public static final URI loopBlock = createRseUri("core#loopBlock");
    public static final URI userControlBlock = createRseUri("core#userControlBlock");
    public static final URI controlFlowTopModel = createRseUri("core#topModel");
    public static final URI controlFlowRightModel = createRseUri("core#rightModel");
    public static final URI controlFlowBottomModel = createRseUri("core#bottomModel");
    public static final URI conditionalStmt = createRseUri("core#conditionalStmt");
    public static final URI loopStmt = createRseUri("core#loopStmt");
    public static final URI elseStmt = createRseUri("core#elseStmt");
    public static final URI thenStmt = createRseUri("core#thenStmt");
    public static final URI ifStmt = createRseUri("core#ifStmt");
    
    public static final URI detailLevelURI = RSECore.createRseUri("core#detailLevel");

    public static final URI sharedDiagramID = createRseUri("core#sharedDiagramID");
    public static final URI sharedGroupID = createRseUri("core#sharedGroupID");
    public static final URI sharedName = createRseUri("core#sharedName");
    public static final URI sharedDesc = createRseUri("core#sharedDesc");

    public static final URI userCreated = createRseUri("core#userCreated");
    public static final URI userCreatedNameText = createRseUri("core#userCreatedNameText");
    // annotation
    public static final URI annoRel = createRseUri("ann#annoRel");
	public static final URI namedRel = createRseUri("ann#namedRel");
	public static final URI commentType = createRseUri("ann#comment");
	public static final URI entityType = createRseUri("ann#entity");
	public static final URI anchorRel = createRseUri("ann#anchorRel");
	
	// used to be part of RJCore but are being moved into RSE (the uri's need
	// updating, but we can worry about that last)
    public static final URI pckgDirContains = createRseUri("jdt#pckgDirContains");
	
	// above properties as directed relationships
    public static final DirectedRel fwdContains = DirectedRel.getFwd(contains);
    public static final DirectedRel revContains = DirectedRel.getRev(contains);
	

	// debugging setting
    public static final boolean dumpStatementsOnMouseSelection = false;

    public static String encodeId(String key) {
	    return key.replace("_","_un").replace(":","_co").replace("/","_sl").replace("#","_sh");
	}
	public static String decodeId(String key) {
		// in rev order as last time
		return key.replace("_sh","#").replace("_sl","/").replace("_co",":").replace("_un", "_");
	}
    public static Resource idToResource(ReloRdfRepository reloRepo, String namespaceExt, String key)  {
        return reloRepo.getDefaultURI(namespaceExt, key);
	}
    // encode is called when we expect the key to have namespace delimiters: ':', '/' or '#'
    public static Resource encodedIdToResource(ReloRdfRepository reloRepo, String namespaceExt, String key) {
        return idToResource(reloRepo, namespaceExt, encodeId(key));
	}

    public static String resourceToId(ReloRdfRepository reloRepo, Resource res, boolean decodeKey) {
    	String retVal = null;
		retVal = decodeId( ((URI)res).getLocalName() );
	    if (decodeKey) retVal = encodeId(retVal);
		return retVal;
	}


    public static Resource eclipseProjectToRDFResource(ReloRdfRepository reloRepo, IProject proj) {
        if (proj==null) return null;
        return RSECore.encodedIdToResource(reloRepo, RSE.appRealTag + "-eclipseProj#", proj.getName());
    }

    public static  boolean ResourceIsEclipseProject(Resource res) {
        return res.toString().contains("-eclipseProj#");
    }

    
    public static IProject resourceToEclipseProject(ReloRdfRepository reloRepo, Resource res) {
        if (res==null) return null;
        return ResourcesPlugin.getWorkspace().getRoot().getProject(resourceToId(reloRepo, res, false));
    }

    public static Resource eclipseResourceToRDFResource(ReloRdfRepository reloRepo, IResource res) {
        if (res==null) return null;
        return RSECore.encodedIdToResource(reloRepo, RSE.appRealTag + "-eclipse#", res.getProjectRelativePath().toString());
    }
    
	// TODO: implement a version of the below that does not need a project as a
	// param (but gets the project by going up the containment of the given
	// resource)
    public static IResource resourceToEclipseResource(ReloRdfRepository reloRepo, Resource res, IProject proj) {
    	if (res==null) return null;
    	String id = resourceToId(reloRepo, res, false);
    	IFile file = proj.getFile(id);
    	return file;
    }
    
    
    // for formatting anon class names in relo and strata
    public static boolean isAnonClassName(String className) {
    	return className.contains("#");
    }
    public static String stripAnonNumber(String className) {
    	return className.substring(0, className.indexOf("#"));
    }
    public static boolean isAnonClassConstructorCall(String methodCall) {
    	return methodCall.contains("##");
    }

    
    public static boolean isInitialized(ReloRdfRepository repo, Resource elementRes) {
		Statement initStatement = repo.getStatement(elementRes, RSECore.initialized, null);
		Resource parentRes = queryParentResource(repo, elementRes);
		while (initStatement.equals(ReloRdfRepository.nullStatement) || initStatement==null) {
			if (parentRes==null) return false;
			initStatement = repo.getStatement(parentRes, RSECore.initialized, null);
			parentRes = queryParentResource(repo, parentRes);
		}
		return true;
	}
    public static boolean isUserCreated(ReloRdfRepository repo, Resource elementRes) {
    	if (elementRes == null) return false;
    	return repo.getStatement(elementRes, RSECore.userCreated, null).getObject() != null;
    }
    public static Resource queryParentResource(ReloRdfRepository repo, Resource elementRes) {
        Resource parentRes = null /*(Resource) repo.getStatement(elementRes, ReloCore.cachedRevContains, null).getObject()*/;
        if (parentRes == null) parentRes = repo.getStatement((Resource)null, RSECore.contains, elementRes).getSubject();
	    if (parentRes == null) return null;
        return parentRes;
	}

	public static String resourceWithoutWorkspace(Resource modelRes) {
		String resString = modelRes.toString();
		String resourceTail = "";
		if (resString.contains("#")) {
			resourceTail = resString.substring(resString.indexOf("#")+1);	
		}
		return resourceTail;
	}
}
