/**
 * Copyright (c) 2007-2008 Architexa, Inc
 */ 
package com.architexa.diagrams.strata.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.openrdf.model.Resource;

import com.architexa.diagrams.Activator;
import com.architexa.diagrams.RSECore;
import com.architexa.diagrams.jdt.RJCore;
import com.architexa.diagrams.jdt.model.CUSupport;
import com.architexa.diagrams.jdt.model.ResourceDependencyRelation;
import com.architexa.diagrams.jdt.model.UserCreatedFragment;
import com.architexa.diagrams.model.Artifact;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.model.ArtifactRel;
import com.architexa.diagrams.model.Comment;
import com.architexa.diagrams.model.RootArtifact;
import com.architexa.diagrams.strata.cache.DepNdx;
import com.architexa.diagrams.strata.model.policy.ClosedContainerDPolicy;
import com.architexa.diagrams.strata.model.policy.ContainedClassSizeCacheDPolicy;
import com.architexa.diagrams.strata.model.policy.DependencyRelDPolicy;
import com.architexa.intro.AtxaIntroPlugin;
import com.architexa.intro.preferences.LibraryPreferences;
import com.architexa.intro.preferences.PreferenceConstants;
import com.architexa.store.PckgDirRepo;
import com.architexa.store.ReloRdfRepository;



public class StrataRootDoc extends RootArtifact {
    static final Logger logger = Activator.getLogger(StrataRootDoc.class);
	
    public final StrataFactory objFactory;
    
    public static boolean stretchFigures = false;
    
    // extra repositories
    private DepNdx depNdx;
    private PckgDirRepo pckgDirRepo;
    
    public StrataRootDoc(DepNdx _depNdx, PckgDirRepo _pckgDirRepo, StrataFactory _objFactory) {
		this.depNdx = _depNdx;
		this.pckgDirRepo = _pckgDirRepo;
		this.objFactory = _objFactory;
		this.setRepo(_depNdx.getRepo().getStoreRepo());
		setInstanceRes(RSECore.docRoot);
		stretchFigures = !AtxaIntroPlugin.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.StrataSizeToContentsKey);
	}
    
    public void setDepNdx(DepNdx _cacheCalculator) {
    	this.depNdx = _cacheCalculator;
    }
	
	public DepNdx getDepNdx() {
		return depNdx;
	}

	public PckgDirRepo getPckgDirRepo() {
		return pckgDirRepo;
	}
	public void setPckgDirRepo(PckgDirRepo _pckgDirRepo) {
		this.pckgDirRepo = _pckgDirRepo;
	}

	// updates maxSize and maxDep
	private PropertyChangeListener maxValCacheUpdater = new PropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent evt) {
			if (ArtifactFragment.Contents_Changed.equals(evt.getPropertyName())) {
				// TODO: make this incremental
				rescanMaxVals();
			}
		}};


	public int maxDep = 1;
	public int maxSize = 1;
	
	private void rescanMaxVals() {
		maxDep = 1;
		maxSize = 1;
		scanMaxVals(this);
	}
		
	private void scanMaxVals(ArtifactFragment af) {
		int size = ContainedClassSizeCacheDPolicy.get(af, getRepo());
		if (!ClosedContainerDPolicy.isShowingChildren(af))
			maxSize = Math.max(maxSize, size);
		List<ArtifactRel> conn;
		
		conn = af.getShownSourceConnections();
		for (ArtifactRel ar : conn) {
			if (!(ar instanceof DependencyRelation)) continue;
			DependencyRelation depRel = (DependencyRelation) ar;
			if (ClosedContainerDPolicy.isShowingChildren(depRel.getDest()) || ClosedContainerDPolicy.isShowingChildren(depRel.getSrc()))
				continue;
           	maxDep = Math.max(maxDep, depRel.depCnt);
			maxDep = Math.max(maxDep, depRel.revDepCnt);
		}

		conn = af.getShownTargetConnections();
		for (ArtifactRel ar : conn) {
			if (!(ar instanceof DependencyRelation)) continue;
			DependencyRelation depRel = (DependencyRelation) ar;
			if (ClosedContainerDPolicy.isShowingChildren(depRel.getDest()) || ClosedContainerDPolicy.isShowingChildren(depRel.getSrc())) 
				continue;   
			maxDep = Math.max(maxDep, depRel.depCnt);
			maxDep = Math.max(maxDep, depRel.revDepCnt);
		}
		
		if (!ClosedContainerDPolicy.isShowingChildren(af)) return;
		// scan children
		for (ArtifactFragment childAF : af.getShownChildren()) {
			scanMaxVals(childAF);
		}
	}

	@Override
    public ArtifactFragment getArtifact(Object src) {
		if ( src instanceof Comment) return null;
		ArtifactFragment af = null;
		if (src instanceof ArtifactFragment){
			if (RSECore.isUserCreated(getRepo(), ((ArtifactFragment)src).getArt().elementRes))
	    		af = new UserCreatedFragment((ArtifactFragment)src);
			else
				af = (ArtifactFragment) src;
		}
		if (src instanceof Artifact) af  = createArtFrag((Artifact) src);
		if (src instanceof Resource) af = createArtFrag((Resource) src);
		
		af.addPropertyChangeListener(maxValCacheUpdater);
		af = StrataFactory.initAF(af);
		return af;
	}
	
	public ArtifactFragment createArtFrag(Artifact cuArt) {
		ArtifactFragment af = objFactory.createArtFrag(cuArt);
		af.addPropertyChangeListener(maxValCacheUpdater);
		return af;
	}
	public ArtifactFragment createArtFrag(Resource _elementRes) {
		if (_elementRes == null) {
			logger.error("Unexpected: Received null rdf resource.", new Exception());
			return null;
		}
			
		ArtifactFragment af = objFactory.createArtFrag(_elementRes);
		af.addPropertyChangeListener(maxValCacheUpdater);
		return af;
	}

	public static final String summarizingDepTaskName = "Summarizing Dependencies";


	// OLD less effiecent method for finding rels to add. may be useful in the future
//	public List<DependencyRelation> getRelationshipsToAdd_org(final List<ArtifactFragment> artFragsToAdd, IProgressMonitor monitor, List<ArtifactFragment> selAFs) {
//		Set<DependencyRelation> newDepRels = new HashSet<DependencyRelation>(20);
//		
//		List<ArtifactFragment> currentAFs = ClosedContainerDPolicy.getNestedShownChildren(StrataRootDoc.this);
//		
//		//For now remove artFrags lower then class, a null type at this point would mean inside of a class
//		//something that should have gotten typed but wasn't
//		List<ArtifactFragment> classChildrenFilterList = new ArrayList<ArtifactFragment>();
//		for (ArtifactFragment af : artFragsToAdd) {
//			Resource artFragType;
//			if (af instanceof UserCreatedFragment)
//				artFragType = af.queryType(getRepo());
//			else
//				artFragType = af.getArt().queryOptionalType(getRepo());
//			if(artFragType == null || artFragType.equals(RJCore.fieldType)||
//					artFragType.equals(RJCore.methodType))
//				classChildrenFilterList.add(af);
//		}
//		artFragsToAdd.removeAll(classChildrenFilterList);
//		
//		currentAFs.addAll(artFragsToAdd);	// we will have DepRel's within the new artFrags also
//		monitor.beginTask(summarizingDepTaskName, currentAFs.size());
//		
//		for (ArtifactFragment af1 : currentAFs) {
//			if (monitor.isCanceled()) continue;
//			for (ArtifactFragment af2 : artFragsToAdd) {
//    			if (af2.equals(af1)) continue;
//    			if (monitor.isCanceled()) continue;
//    			
//    			DependencyRelation newDepRel = getDepRel(af1, af2, monitor, selAFs);
//            	if (newDepRel != null) {
//            		ArtifactRel.ensureInstalledPolicy(newDepRel, DependencyRelDPolicy.DefaultKey, DependencyRelDPolicy.class);	
//            		newDepRels.add(newDepRel);
//            	}
//			}
//			// monitor is worked when retrieving the depRel
//			// monitor.worked(1);
//		}
//		
//		monitor.done();
//		return new ArrayList<DependencyRelation>(newDepRels);
//	}
	/**
	 * Relationships should be added before nodes so that the relationships are
	 * shown when the nodes are added (and so that layering can happen)
	 * @param selAFs
	 *            Not currently used, can be used to improve visibility of
	 *            monitor/better guess number of rels to add. Examine for
	 *            removal
	 */
	public List<DependencyRelation> getRelationshipsToAdd(final List<ArtifactFragment> artFragsToAdd, IProgressMonitor monitor) {
		return getRelationshipsToAdd(artFragsToAdd, monitor, null);
	}
	public List<DependencyRelation> getRelationshipsToAdd(final List<ArtifactFragment> artFragsToAdd, IProgressMonitor monitor, List<ArtifactFragment> selAFs) {
		Set<DependencyRelation> newDepRels = new HashSet<DependencyRelation>(20);
		List<ArtifactFragment> currentAFs = ClosedContainerDPolicy.getNestedShownChildren(StrataRootDoc.this);
		
		int total = currentAFs.size() + artFragsToAdd.size() + artFragsToAdd.size() + artFragsToAdd.size();  
		if (total > 25)
			monitor.beginTask(summarizingDepTaskName + " (This may take a few minutes)", total);
		else
			monitor.beginTask(summarizingDepTaskName, artFragsToAdd.size());
		
		//For now remove artFrags lower then class, a null type at this point would mean inside of a class
		//something that should have gotten typed but wasn't
		List<ArtifactFragment> classChildrenFilterList = new ArrayList<ArtifactFragment>();
		for (ArtifactFragment af : artFragsToAdd) {
			Resource artFragType;
			if (af instanceof UserCreatedFragment)
				artFragType = af.queryType(getRepo());
			else
				artFragType = af.getArt().queryOptionalType(getRepo());
			if(artFragType == null || artFragType.equals(RJCore.fieldType)||
					artFragType.equals(RJCore.methodType))
				classChildrenFilterList.add(af);
			if (monitor.isCanceled()) return new ArrayList<DependencyRelation>();
			monitor.worked(1);
		}
		artFragsToAdd.removeAll(classChildrenFilterList);
		
		currentAFs.addAll(artFragsToAdd);	// we will have DepRel's within the new artFrags also
		
		Map<Resource, ArtifactFragment> currentResourcesToAFMap = new HashMap<Resource, ArtifactFragment>();
		for (ArtifactFragment af : currentAFs) {
			if (af instanceof StrataRootDoc) {
				for (ArtifactFragment rootChildAF : af.getShownChildren()) {
					currentResourcesToAFMap.put(rootChildAF.getArt().elementRes, rootChildAF);
					if (monitor.isCanceled()) return new ArrayList<DependencyRelation>();
				}
			}
			currentResourcesToAFMap.put(af.getArt().elementRes, af);
			monitor.worked(1);
		}
		
		Map<Resource, Resource> resToParentMap = new HashMap<Resource, Resource>();
		// build map of resources to the parents in the diagram
		// TODO: consider moving this into memory so we dont have to calculate
		// it multiple times for compound commands...
		for (Resource r : currentResourcesToAFMap.keySet()) {
			// add top level because recursive method below only adds children
			resToParentMap.put(r,null);
			buildResToParentMap(getRepo(), new Artifact(r), currentResourcesToAFMap, resToParentMap, monitor);
			if (monitor.isCanceled()) return new ArrayList<DependencyRelation>();
		}
		
		for (ArtifactFragment af1 : artFragsToAdd) {
			if (monitor.isCanceled()) continue;
			
			// get list of dependencies to/from af1
			List<ResourceDependencyRelation> depRels = getDepNdx().getFilteredDepsToAndFrom(af1, currentResourcesToAFMap, resToParentMap, monitor);
			for (ResourceDependencyRelation rdr : depRels) {
    			if (monitor.isCanceled()) continue;
    			ArtifactFragment src = currentResourcesToAFMap.get(rdr.getSrcRes());
    			ArtifactFragment dst = currentResourcesToAFMap.get(rdr.getDstRes());
    			if (src == dst) continue;
    			DependencyRelation newDepRel = null;
    			newDepRel = DependencyRelation.getDepRel(src, dst, rdr.getDepCnt(), rdr.getRevDepCnt());
    			if (newDepRel != null) {
            		ArtifactRel.ensureInstalledPolicy(newDepRel, DependencyRelDPolicy.DefaultKey, DependencyRelDPolicy.class);	
            		newDepRels.add(newDepRel);
            	}
			}
			// monitor is worked when retrieving the depRel
			monitor.worked(1);
		}
		
		monitor.done();
		return new ArrayList<DependencyRelation>(newDepRels);
	}

	
	public void buildResToParentMap(ReloRdfRepository repo, Artifact artifact, Map<Resource, ArtifactFragment> currentResourcesToAFMap, Map<Resource, Resource> resToParentMap, IProgressMonitor monitor) {
		Resource type = artifact.queryType(repo);
		// TODO: improve performance here. Should only need to check below level of GraphNodes if there is a class showing in the diagram
		// This filter can cause rels between classes and packages not to show up
		//ArtifactFragment af = currentResourcesToAFMap.get(artifact.elementRes);
		
		if (CUSupport.isType(type))
			return;
		for (Artifact child : getDepNdx().getContainedArts(repo, artifact)) {
			if (monitor.isCanceled()) return;
			resToParentMap.put(child.elementRes, artifact.elementRes);	
			buildResToParentMap(repo, child, currentResourcesToAFMap, resToParentMap, monitor);
		}
	}
	
	
	private DependencyRelation getDepRel(ArtifactFragment af1, ArtifactFragment af2, IProgressMonitor monitor, List<ArtifactFragment> selAFs) {
		int depCnt = depNdx.getDep(af1, af2, monitor, selAFs);
		int revDepCnt = 0;
		if (!monitor.isCanceled())
			revDepCnt = depNdx.getDep(af2, af1, monitor, selAFs);
        
        if (depCnt == 0 && revDepCnt == 0) return null;
        
        return DependencyRelation.getDepRel(af1, af2, depCnt, revDepCnt);
	}

	public DependencyRelation getDepRel(ArtifactFragment af1, ArtifactFragment af2, IProgressMonitor monitor) {
		return getDepRel(af1, af2, monitor, null);
	}


	public boolean isNewRelationship(ArtifactRel depRel) {
		// we assume integrity - if it exists it will be on source and
		// destination artFrags
		return ! depRel.getSrc().sourceConnectionsContains(depRel);
	}
	public void addRelationships(List<? extends ArtifactRel> newDepRels) {
		for (ArtifactRel depRel : newDepRels) {
			if (!isNewRelationship(depRel)) continue;
            depRel.getSrc().addSourceConnectionWithoutRefresh(depRel);
            depRel.getDest().addTargetConnectionWithoutRefresh(depRel);
		}
		// we do not fireproperty changes but do scan for new values at the end
		rescanMaxVals();
	}
	public void removeRelationships(List<? extends ArtifactRel> depRels) {
		for (ArtifactRel depRel : depRels) {
            depRel.getSrc().removeSourceConnection(depRel);
            depRel.getDest().removeTargetConnection(depRel);
		}
	}
	
	public List<ArtifactRel> removeRelationships(ArtifactFragment af) {
		List<ArtifactRel> depRels = new ArrayList<ArtifactRel>();
		for (ArtifactFragment afChild : af.getShownChildren()) {
			depRels.addAll(removeRelationships(afChild));
		}
		depRels.addAll(af.getSourceConnections());
		depRels.addAll(af.getTargetConnections());
		removeRelationships(depRels);
		return depRels;
	}

	@Override
	public List<ArtifactFragment> getMatchingNestedShownChildren(Artifact givenChild) {
		List<ArtifactFragment> foundChildren = super.getMatchingNestedShownChildren(givenChild);
		if(!foundChildren.isEmpty()) return foundChildren;

		foundChildren = new ArrayList<ArtifactFragment>();
		ArtifactFragment found = searchAllNestedChildren(givenChild, this);
		if(found!=null) foundChildren.add(found);
		return foundChildren;
	}

	private ArtifactFragment searchAllNestedChildren(Artifact givenChild, ArtifactFragment currFrag) {
		ArtifactFragment foundChild = currFrag.getShownChild(givenChild);
		if (foundChild!=null) return foundChild;

		for(ArtifactFragment child : currFrag.getShownChildren()) {
			ArtifactFragment found = searchAllNestedChildren(givenChild, child);
			if(found!=null) return found;
		}
		return null;
	}

	@Override
	public ArtifactFragment addVisibleArt(ArtifactFragment child, ArtifactFragment parentAF) {
   		if (parentAF != null) 
			parentAF.appendShownChild(child);
   		else this.appendShownChild(child);
		return parentAF;
	}

	@Override
	public boolean isLibCodeInDiagram() {
		return LibraryPreferences.isStrataLibCodeInDiagram();
	}

}
