package com.architexa.diagrams.jdt.builder.asm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;

import com.architexa.diagrams.RSECore;
import com.architexa.diagrams.jdt.Activator;
import com.architexa.diagrams.jdt.RJCore;
import com.architexa.diagrams.jdt.builder.asm.DepRepoReplacement.DepStrength;
import com.architexa.diagrams.jdt.model.CUSupport;
import com.architexa.diagrams.jdt.model.ResourceDependencyRelation;
import com.architexa.diagrams.model.Artifact;
import com.architexa.diagrams.model.DirectedRel;
import com.architexa.store.PckgDirRepo;
import com.architexa.store.ReloRdfRepository;

/**
 * This class works by providing caching support of dependencies at two
 * different (and separate) levels, and calculating for the rest.<br>
 * <br>
 * Data at sub class level is added here explicitly calling static methods here,
 * this allows us to not need to read the repository as often.<br>
 * This class is a RDFBuildProcessor to do the package processing.<br>
 * <br>
 * Caching at n levels as before and providing n*n data was likely causing
 * performance problems due to the data.<br>
 * <br>
 * Sampling of some of the Dependency Types:<br>
 * Input Types:<br>
 * c -> c (inheritance)<br>
 * m -> m (calls)<br>
 * m -> c (references)<br>
 * f -> c (references)<br>
 * <br>
 * <br>
 * Query Types:<br>
 * p -> p<br>
 * p -> c<br>
 * c -> p<br>
 * c -> c<br>
 * <br>
 * TODO: Support for having fields and methods in Strata will need to come
 * later, we will mainly need to test and implement the appropriate portions in
 * readDepStrength.<br>
 * 
 * @author vineet
 */
public class DepAndChildrenStrengthSummarizer {
    private static final Logger logger = Activator.getLogger(DepAndChildrenStrengthSummarizer.class);
    
    private static boolean runDebugAssertions = false;

	private static Map<URI, URI> realToVirtual = new HashMap<URI, URI>(5);
    private static Map<URI, URI> virtualToReal = new HashMap<URI, URI>(5);
    public static void addContainmentRelMapping(URI real, URI virtual) {
    	realToVirtual.put(real, virtual);
    	virtualToReal.put(virtual, real);
    }
    private static Set<URI> relForWriting = new HashSet<URI>(3);
    public static void setRelForWriting(URI real) {
    	// we don't write by default as we want to optimize for speed
    	relForWriting.add(real);
    }
    static {
    	addContainmentRelMapping(RJCore.calls, RJCore.containmentBasedCalls);
    	addContainmentRelMapping(RJCore.inherits, RJCore.containmentBasedInherits);
    	addContainmentRelMapping(RJCore.refType, RJCore.containmentBasedRefType);
    	setRelForWriting(RJCore.refType);
    };
    protected static class RelDst {
		public URI rel;
    	public Resource dst;
    	public RelDst(URI _rel, Resource _dst) {
    		this.rel = _rel;
    		this.dst = _dst;
    		if (this.rel == null || this.dst == null)
    			logger.error("Initialized with null", new Exception());
    	}
    	@Override
		public int hashCode() {
			final int prime = 31;
			return rel.hashCode() + prime * dst.hashCode();
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			RelDst other = (RelDst) obj;
			if (!dst.equals(other.dst))
				return false;
			if (!rel.equals(other.rel))
				return false;
			return true;
		}
		@Override
	    public String toString() {
			return "--[" + rel.toString() + "]--> " + dst.toString();
		}
    }
    
	// the Dependency Repo Replacement can be switched to an rdfRepo to restore
	// original functionality and write all statements to the repo. Except where
	// getStatement(s) is used  
    private final DepRepoReplacement depRepo;

	public boolean hasBeenInit = false;
	public DepRepoReplacement getDepRepo() {
		return depRepo;
	}

	public int processed = 0;
	public int total = 0;
    protected ReloRdfRepository rdfRepo;
    
	// Vineet: will be helpful to move the field and the two methods below to
	// DepRepoReplacement - that way all the repo's will have the same signature

    private static Map<IPath, DepAndChildrenStrengthSummarizer> activeDACSS = new HashMap<IPath, DepAndChildrenStrengthSummarizer>();

	/**
	 * Used when trying to get the already open DACSS. Usually you want to get
	 * this from the RootArt, but this method is used by the builder to see what
	 * is open.
	 * @param path 
	 */
	public static DepAndChildrenStrengthSummarizer getDACSS(ReloRdfRepository _repo, IPath _path, PckgDirRepo _pdr) {
		if (_path == null) _path = Path.EMPTY;
		if (!activeDACSS.containsKey(_path)) {
			activeDACSS.put(_path, new DepAndChildrenStrengthSummarizer(_repo, _path));
		}
		return activeDACSS.get(_path);
	}
	public static void resetDACSS() {
		activeDACSS.remove(Path.EMPTY);
	}

	// using an private constructor to ensure that no only our method can create it 
	private DepAndChildrenStrengthSummarizer(ReloRdfRepository _rdfRepo, IPath _path) {
    	this.rdfRepo = _rdfRepo;
    	this.depRepo = new DepRepoReplacement(_rdfRepo, _path);
    }

	public void ensureInitDepRepo() {
		if (!hasBeenInit) getDepRepo().readFile();
		hasBeenInit = true;
	}
	

    ///////////////////////////////////////
    // some of the caching support
    ///////////////////////////////////////
    
    private Resource cachedSrcClass = null;
    private Map<RelDst, Integer> cachedDstClassDep = null;

    private Resource cachedSrcPackage = null;
    private Map<RelDst, Integer> cachedDstPackageDep = null;

 	private final void cacheToTypeAndPckg(URI rel, Resource tgtClass) {
		// source is always cached*
		
		// update class dep
		int depCnt;
		RelDst classDst = new RelDst(rel, tgtClass);
		
		//See if there is a stored statement of this resource and relation, if so get the number and remove to
		//avoid inaccurate over count
		int toLose = 0;
		Statement depStmt = depRepo.createStmt(cachedSrcClass, realToVirtual.get(rel), tgtClass);
		int depStrengthToLose = depRepo.getStrengthOfRelStatement(depStmt, RJCore.containmentBasedDepStrength, null);
		if (depStrengthToLose != -1)
			toLose = depStrengthToLose;
		
		depRepo.removeStatements(depStmt, RJCore.containmentBasedDepStrength, null);
		
		depCnt = 1;
		if (cachedDstClassDep.containsKey(classDst))
			depCnt += cachedDstClassDep.get(classDst);
		cachedDstClassDep.put(classDst, depCnt);
		
		// problem with the optimizer is that we don't know if the package on file has been initialized at all
		// TODO: should some of the above be in AsmPackageSupport?
		// get package
		//Resource tgtPackage = rdfRepo.getStatement((Resource)null, RSECore.contains, tgtClass).getSubject();
		Resource tgtPackage = ContainmentOptimizer.getContainingPckgRes(rdfRepo, tgtClass);
		if (tgtPackage == null) {
			// package not found
			//s_logger.error("Package not found for: " + tgtClass);
			String classID = ((URI)tgtClass).getLocalName();
			tgtPackage = AsmPackageSupport.getExternalPackage(rdfRepo, AsmPackageSupport.getPckgNameFromClassID(classID));
		}

		// update package dep
		depCnt = 1-toLose;
		RelDst pckgDst = new RelDst(rel, tgtPackage);
		if (cachedDstPackageDep.containsKey(pckgDst))
			depCnt += cachedDstPackageDep.get(pckgDst);
		cachedDstPackageDep.put(pckgDst, depCnt);
	}
	
	public void updateSrcClassCache(Resource srcClass) {
		if (cachedSrcClass != srcClass) {
			if (cachedSrcClass != null && !cachedDstClassDep.isEmpty()) writeDepStrength(cachedSrcClass, cachedDstClassDep);
			cachedSrcClass = srcClass;
			cachedDstClassDep = new HashMap<RelDst, Integer> (20);
		}
	}
	public void updateSrcFldrCache(Resource srcPackage) {
		if (cachedSrcPackage != srcPackage) {
			updateSrcClassCache(null);	// make sure to write class cache
			if (cachedSrcPackage != null) {
				if(!cachedDstPackageDep.isEmpty()){
					writeDepStrength(cachedSrcPackage, cachedDstPackageDep);
				}
				writeNumberOfChildren(cachedSrcPackage);
			}
			cachedSrcPackage = srcPackage;
			cachedDstPackageDep = new HashMap<RelDst, Integer> (20);
			
			if(cachedSrcPackage!=null)
				setCachedDep(cachedSrcPackage, cachedDstPackageDep);
		}
	}
	public void finishSummarizing() {
		updateSrcFldrCache(null); // make sure to write package cache
		depRepo.writeToFile();
	}
	public void storeNCacheTypeType(Resource srcClass, URI rel, Resource tgtClass) {
		rdfRepo.addStatement(srcClass, rel, tgtClass);
		updateSrcClassCache(srcClass);
		cacheToTypeAndPckg(rel, tgtClass);
	}
	public void storeNCacheMethMeth(Resource srcMethRes, URI rel, Resource tgtMeth, Resource tgtClass) {
		rdfRepo.addStatement(srcMethRes, rel, tgtMeth);
		cacheToTypeAndPckg(rel, tgtClass);
	}
	public void storeNCacheMethType(Resource srcMeth, URI rel, Resource tgtClass) {
		rdfRepo.addStatement(srcMeth, rel, tgtClass);
		cacheToTypeAndPckg(rel, tgtClass);
	}
	public void storeNCacheMethTypeWeak(Resource methodRes, URI rel, Resource tgtClass) {
		rdfRepo.addStatement(methodRes, rel, tgtClass);
		// we don't do anything else here (atleast for now) otherwise diagram
		// will get ugly
	}
	public void storeNCacheFieldType(Resource fieldRes, URI rel, Resource tgtClass) {
		rdfRepo.addStatement(fieldRes, rel, tgtClass);
		cacheToTypeAndPckg(rel, tgtClass);
	}
	
	private void setCachedDep(Resource currRes, Map<RelDst, Integer> dstWithDepStrength){
		for(URI vRel : virtualToReal.keySet()){
			List<Resource> si = depRepo.getFwdStatements(currRes, vRel, null);
			for (Resource s : si) {
				try {
					Resource dest = s;
					if(dstWithDepStrength.containsKey(new RelDst(virtualToReal.get(vRel), dest)))
						continue;
					Statement depStmt = depRepo.createStmt(currRes, vRel, dest);
					Integer theStrength = 1;
					int depStrength = depRepo.getStrengthOfRelStatement(depStmt,RJCore.containmentBasedDepStrength,null);
					if (depStrength!=-1)
						theStrength = depStrength;
					dstWithDepStrength.put(new RelDst(virtualToReal.get(vRel), dest), theStrength);
					
				} catch (Exception e) {
					rdfRepo.commitTransaction();
					rdfRepo.startTransaction();
					logger.error("Unexpected: namespace must not be null, restart workspace");
				} 
			}
		}
	}
	
    protected void writeDepStrength(Resource currRes, Map<RelDst, Integer> dstWithDepStrength) {
    	for (Map.Entry<RelDst, Integer> depStrengthEntry : dstWithDepStrength.entrySet()) {
        	RelDst dst = depStrengthEntry.getKey();
        	if (currRes.equals(dst.dst)) continue; // we are not keeping any dependencies to self
        	
        	// we are not writing relationships that we don't need
        	if (!relForWriting.contains(dst.rel)) continue;

        	Integer dstArtStrength = depStrengthEntry.getValue();
            URI vRel = realToVirtual.get(dst.rel);
            depRepo.addRelStatement(currRes, vRel, dst.dst);
            Statement depStmt = depRepo.createStmt(currRes, vRel, dst.dst);
            //Make sure to remove olderStatements before adding new ones to side step multiple statement issues
            depRepo.removeStatements(depStmt, RJCore.containmentBasedDepStrength, null);
            depRepo.addRelStrengthStatement(depStmt, RJCore.containmentBasedDepStrength, dstArtStrength.toString());

    	}
        //rdfRepo.addStatement(currRes, RJCore.containmentCacheValid, dstArt.rel);
    }

	/**
	 * Works for dependencies from packages to other packages and classes to
	 * other classes, i.e. p->p and c->c
	 */
    private int readLvlDepStrength(ReloRdfRepository repo, Resource srcRes, Resource dstRes, URI rel) {
    	if (rel == null) {
    		// check all relationships
    		int depStrength = 0;
    		for (URI realRel : realToVirtual.keySet()) {
				depStrength += readLvlDepStrength(repo, srcRes, dstRes, realRel);
			}
    		return depStrength;
    	}
    	URI virtualRel = realToVirtual.get(rel);
    	
    	ensureInitDepRepo();
    		
    	if (!depRepo.hasStatement(srcRes, virtualRel, dstRes)) 
    		return 0;
    	Statement depStmt = depRepo.createStmt(srcRes, virtualRel, dstRes);
    	int depStrength = depRepo.getStrengthOfRelStatement(depStmt, RJCore.containmentBasedDepStrength, null);
    	if (depStrength == -1) 
    		return 1;
    	return depStrength;
    }
    public int readDepStrength(ReloRdfRepository repo, Artifact srcArt, Artifact dstArt, URI rel, IProgressMonitor monitor, int srcChildrenSize , int dstChildrenSize) {
    	total = (dstChildrenSize + srcChildrenSize) * dstChildrenSize;
        
    	int depStr = readDepStrength(repo, srcArt, dstArt, rel, monitor);
		return depStr;
    }
    
    private int readDepStrength(ReloRdfRepository repo, Artifact srcArt, Artifact dstArt, URI rel, IProgressMonitor monitor) {
    	// we need to support lots of combinations {using ip for indirect packages}
    	ensureInitDepRepo();
    	if (monitor.isCanceled()) return 0;
    	
    	Resource srcType = srcArt.queryType(repo);
    	Resource dstType = dstArt.queryType(repo);
    	if (runDebugAssertions) {
        	if (!CUSupport.isProject(srcType) &&
        			!CUSupport.isPackageFolder(srcType) &&
        			!CUSupport.isPackage(srcType) &&
        			!CUSupport.isGraphNode(srcType, srcArt, repo)) {
        		logger.error("Reading dep strength on unexpected type: " + srcType);
        	}
        	if (!CUSupport.isProject(dstType) &&
        			!CUSupport.isPackageFolder(dstType) &&
        			!CUSupport.isPackage(dstType) &&
        			!CUSupport.isGraphNode(dstType, dstArt, repo)) {
        		logger.error("Reading dep strength on unexpected type: " + dstType);
        	}
    	}
    	
    	// we basically have to break down the srcRes and the dstRes to p->p and c->c and use the lvl method 
    	// support ip->* and *->ip
    	if (CUSupport.isPackageFolder(srcType) || CUSupport.isProject(srcType)) {
    		return readDepStrengthWithSrcChildren(repo, srcArt, dstArt, rel, monitor);
    	}
    	if (CUSupport.isPackageFolder(dstType) || CUSupport.isProject(dstType)) {
    		return readDepStrengthWithDstChildren(repo, srcArt, dstArt, rel, monitor);
    	}
    	// support p->* and *->p
    	if (CUSupport.isPackage(srcType) && CUSupport.isPackage(dstType)) {
    		if (monitor.isCanceled()) return 0;
    		monitor.subTask("processing...");
        	processed++;
        	monitor.worked(1);
        	return readLvlDepStrength(repo, srcArt.elementRes, dstArt.elementRes, rel);
    	} else if (CUSupport.isPackage(srcType)) {
    		if (parentAisRelToB(repo, dstArt, srcArt, rel, monitor))
    			return readDepStrengthWithSrcChildren(repo, srcArt, dstArt, rel, monitor);
    	} else if (CUSupport.isPackage(dstType)) {
    		if (parentAisRelToB(repo, srcArt, dstArt, rel, monitor))
    			return readDepStrengthWithDstChildren(repo, srcArt, dstArt, rel, monitor);
    	}
    	// support c->c
    	if (CUSupport.isGraphNode(srcType, srcArt, repo)) {
    		if (CUSupport.isGraphNode(dstType, dstArt, repo))
    			return readLvlDepStrength(repo, srcArt.elementRes, dstArt.elementRes, rel);
    		// all cases should have been supported
    		//else
    		//	return readDepStrengthWithSrcChildren(repo, srcArt, dstArt, rel);
    	}
    	
//		logger.info("Reading dep strength on unexpected types: " + srcType + " --> " + dstType);
    	return 0;
    }

    // utility methods
    
    private boolean parentAisRelToB(ReloRdfRepository repo, Artifact artA, Artifact artB, URI rel, IProgressMonitor monitor) {
//    	StatementIterator si = repo.getStatement(null, RSECore.contains, artA.elementRes);
    	String packageString = RSECore.resourceToId(repo, artA.elementRes, true);
    	if (!packageString.contains("$")) return true;
    	int index = packageString.indexOf("$");
    	packageString = packageString.substring(0, index);
    	//    	Resource parentResPackage = RJCore.idToResource(repo, RJMapToId.pckgIDToPckgFldrID( RJMapToId.getId(ije).substring(0,index) ));
    	Resource pckgRes = RJCore.idToResource(repo, packageString);
    	Artifact parentArt = new Artifact(pckgRes);
    	if (monitor.isCanceled()) return false;
    	return readDepStrength(repo, parentArt, artB, rel, monitor) + readDepStrength(repo, artB, parentArt, rel, monitor) > 0 ;
    	
	}

	private int readDepStrengthWithSrcChildren(ReloRdfRepository repo, Artifact srcArt, Artifact dstArt, URI rel, IProgressMonitor monitor) {
		int depCnt = 0;
		List<Artifact> children = getContainedArts(repo, srcArt);
		for (Artifact child : children) {
			if (monitor.isCanceled()) break;
			depCnt += readDepStrength(repo, child, dstArt, rel, monitor);
		}
		return depCnt;
	}
    private int readDepStrengthWithDstChildren(ReloRdfRepository repo, Artifact srcArt, Artifact dstArt, URI rel, IProgressMonitor monitor) {
		int depCnt = 0;
		List<Artifact> children = getContainedArts(repo, dstArt);
		for (Artifact child : children) {
			if (monitor.isCanceled()) break;
			depCnt += readDepStrength(repo, srcArt, child, rel, monitor);
		}
		return depCnt;
	}
	public List<Artifact> getContainedArts(ReloRdfRepository repo, Artifact codeArt) {
        List<Artifact> listArt = null;

        listArt = codeArt.queryPckgDirContainsArtList(repo, DirectedRel.getFwd(RJCore.pckgDirContains));

        if (listArt.size() == 0) {
            listArt = codeArt.queryArtList(repo, RSECore.fwdContains);
        }
        return listArt;
    }
	
	//sees if amount of classes/interfaces in thePackage is same as stored in the repository,
	//if different updates the repository
	private void writeNumberOfChildren(Resource thePackage){
		int prior = 0;
		String storedClasses = depRepo.getContainedClassesStatement(thePackage, RJCore.containmentBasedClassesInside, null);
		if (storedClasses !=null) prior = Integer.parseInt(storedClasses);
		
		Artifact toArt = new Artifact(thePackage);
		List<Artifact> packageChildren = getContainedArts(rdfRepo, toArt);
		
		int theSize = 0;
		for(Artifact art: packageChildren){
			if (isType(rdfRepo, art))
				theSize++;
		}
		
		if(prior!=theSize){
			depRepo.removeStatements(thePackage, RJCore.containmentBasedClassesInside, null);
			depRepo.addContainmentStatement(thePackage, RJCore.containmentBasedClassesInside, ((Integer)theSize).toString());
		}
	}
	
//	public static int getChildrenSize(Set<Artifact> containedObjs, ReloRdfRepository repo, DepCalculator depCalculator, Artifact art, URI virtualcontainerheirarchy) {
//		// TODO Auto-generated method stub
//		return 0;
//	}
	//This should only calculate amount the first time a project is opened in a strata diagram, otherwise only reads them from repo
	public int getChildrenSize(Set<Artifact> containedObjs, ReloRdfRepository repo, Artifact art, URI virtualContainerHeirarchy) {
		ensureInitDepRepo();
		String storedClasses = depRepo.getContainedClassesStatement(art.elementRes, RJCore.containmentBasedClassesInside, null);
		if (storedClasses !=null){
			return Integer.parseInt(storedClasses);
		}
		
		containedObjs.add(art);
		List<Artifact> listArt;
		int theSize = 0;
		
		listArt = art.queryArtList(repo, DirectedRel.getFwd(virtualContainerHeirarchy));
		for (Artifact childCU : listArt) {
			theSize += getChildrenSize(containedObjs, repo, childCU, virtualContainerHeirarchy);
		}
		
		if (listArt.size() == 0) {
			listArt = art.queryPckgDirContainsArtList(repo, RJCore.fwdPckgDirContains);
			for (Artifact childArt : listArt) {
				theSize += getChildrenSize(containedObjs, repo, childArt, virtualContainerHeirarchy);
			}
		}

		if (listArt.size() == 0) {
			listArt = art.queryArtList(repo, RSECore.fwdContains);
			for (Artifact childArt : listArt) {
				theSize += getChildrenSize(containedObjs, repo, childArt, virtualContainerHeirarchy);
			}
		}
		if (isType(repo, art)) theSize++;
		
		return theSize;
	}
    
	private static boolean isType(ReloRdfRepository repo, Artifact art) {
		return CUSupport.isGraphNode(art.queryType(repo), art, repo);
	}

	
	public List<ResourceDependencyRelation> readFilteredDepStrength(ReloRdfRepository repo, Artifact srcArt, List<Resource> currentResources, Map<Resource, Resource> resToParentMap, IProgressMonitor monitor) {
		Resource srcType = srcArt.queryType(repo);
		List<ResourceDependencyRelation> retList = new ArrayList<ResourceDependencyRelation>();
    	
		// should not need to do for package folders once they are added to the cache
		Set<Artifact> children = new HashSet<Artifact>();
		
		// May want to cache package folder rels when building so we dont need
		// to get children of package folders here
		if (CUSupport.isPackageFolder(srcType)) {  // get package children if we are a package folder
    		children.addAll(getAllPackageChildren(repo, srcArt)); 
    	} else if (CUSupport.isType(srcType)) {  // get children if we are a class (to support anon inner classes etc)
    		children.addAll(getAllChildren(repo, srcArt));
    	} else if (!CUSupport.isProject(srcType)) {
    		children.add(srcArt);
    	} else
    		return retList;
		Map<Resource, DepStrength> dependenciesMap = new HashMap<Resource, DepRepoReplacement.DepStrength>(); 
		
		Map<Resource, DepStrength> srcArtDeps = depRepo.cachedContainmentBasedRefTypeRelMap.get(srcArt.elementRes);
		if (srcArtDeps  != null)
			dependenciesMap.putAll(srcArtDeps);
		for (Artifact art : children) {
			Map<Resource, DepStrength> childDepMap = depRepo.cachedContainmentBasedRefTypeRelMap.get(art.elementRes);
			if (childDepMap!=null)
				dependenciesMap.putAll(childDepMap);	
			//resToParentMap.put(art.elementRes, srcArt.elementRes);
		}
    	
		// filter deps
		if (dependenciesMap != null) {
			for (Resource destRes : new HashSet<Resource>(dependenciesMap.keySet())) {
				dependenciesMap = filter(repo, destRes, dependenciesMap, resToParentMap);
			}
		}
		
		// aggregate deps
		// create dependencies to return
		retList = aggregateDeps(srcArt, dependenciesMap, currentResources, resToParentMap, monitor);
		
		return retList;
	}
	
	private Collection<? extends Artifact> getAllPackageChildren(ReloRdfRepository repo, Artifact srcArt) {
		if (CUSupport.isPackage(srcArt.queryType(repo))) return new ArrayList<Artifact>();
		
		List<Artifact> children = getContainedArts(repo, srcArt);
		for (Artifact child : new ArrayList<Artifact>(children)) {
			// only recurse for package folders
			if (CUSupport.isPackage(child.queryType(repo))) continue;
			
			children.addAll(getAllPackageChildren(repo, child));
		}
		return children;
	}
	
	private static List<ResourceDependencyRelation> aggregateDeps(Artifact srcArt, Map<Resource, DepStrength> depMap, List<Resource> currentResources, Map<Resource, Resource> resToParentMap, IProgressMonitor monitor) {
		List<ResourceDependencyRelation> depList = new ArrayList<ResourceDependencyRelation>();
		List<ResourceDependencyRelation> finalDepList = new ArrayList<ResourceDependencyRelation>();
		if (depMap!=null) {
			for (Resource fwdRes : depMap.keySet()) {
				if (srcArt.elementRes.equals(resToParentMap.get(fwdRes))) continue; // do not create dep if self conn
				DepStrength fwdStrength = depMap.get(fwdRes);	
				if (fwdStrength == null) continue;
				monitor.subTask("processing: " + RSECore.resourceWithoutWorkspace(fwdRes));
				while (fwdRes!=null) {
					ResourceDependencyRelation fwdRDR = new ResourceDependencyRelation(srcArt.elementRes, fwdRes, fwdStrength.fwd, fwdStrength.bck);
					int ndxOfRDR = getNdxOfRDR(fwdRDR, depList);
					if (ndxOfRDR == -1) {
						depList.add(fwdRDR);
						if (currentResources.contains(fwdRes) && currentResources.contains(srcArt.elementRes)) {
							finalDepList.add(fwdRDR);
						}
					} else {
						fwdRDR = depList.get(ndxOfRDR);
						fwdRDR.setDepCnt(fwdStrength.fwd + depList.get(ndxOfRDR).getDepCnt());
						fwdRDR.setRevDepCnt(fwdStrength.bck + depList.get(ndxOfRDR).getRevDepCnt());
					}
					Resource tmpRes = fwdRes;
					fwdRes = resToParentMap.get(fwdRes);
					if (tmpRes == fwdRes) break;
				}
					
			}
		}
		
		return finalDepList;
	}	
	private static int getNdxOfRDR(ResourceDependencyRelation rdrToFind, List<ResourceDependencyRelation> retList) {
		for (ResourceDependencyRelation rdr : retList) {
			if (rdr.equals(rdrToFind) && rdr.equals(rdrToFind))
				return retList.indexOf(rdr);
		}
		return -1;
	}
	
	private static Map<Resource, DepStrength> filter(ReloRdfRepository repo, Resource res, Map<Resource, DepStrength> deps, Map<Resource, Resource> resToParentMap) {
		// if destRes is in the diagram or being added then we want to keep it
		if (resToParentMap.keySet().contains(res))
			return deps;
		// if it has a parent in the diagram or being added we want to keep it
		if (resToParentMap.values().contains(res))
			return deps;
		
		// otherwise remove it
		deps.remove(res);
		return deps;
	}
	
	private List<Artifact> getAllChildren(ReloRdfRepository repo, Artifact srcArt) {
		List<Artifact> children = getContainedArts(repo, srcArt);
		// Doing something like the below may increase performance here: but we
		// need to make sure connections at all levels are maintained. i.e. anon
		// class to packagefolder etc
		//Resource srcType = srcArt.queryType(repo);
		//if (!CUSupport.isPackage(srcType))
		//	return children;
		for (Artifact child : new ArrayList<Artifact>(children)) {
			children.addAll(getAllChildren(repo, child));
		}
		return children;
	}
}
