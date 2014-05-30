/**
 * Copyright (c) 2007-2008 Architexa, Inc
 */ 
package com.architexa.diagrams.strata.cache;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;

import com.architexa.diagrams.jdt.builder.asm.DepAndChildrenStrengthSummarizer;
import com.architexa.diagrams.jdt.builder.asm.DepRepoReplacement;
import com.architexa.diagrams.jdt.model.ResourceDependencyRelation;
import com.architexa.diagrams.model.Artifact;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.model.DerivedArtifact;
import com.architexa.diagrams.model.DirectedRel;
import com.architexa.diagrams.strata.StrataPlugin;
import com.architexa.diagrams.strata.commands.ModelUtils;
import com.architexa.store.PckgDirRepo;
import com.architexa.store.ReloRdfRepository;
import com.architexa.store.RepositoryMgr;

/**
 * This class is not the store of the dependencies - that is the
 * ReloRdfRepository. This class stores an index to the rdf repo - it is
 * implemented using summarization information (dacss) and the extension to the
 * rdf repo (depRepo). The class likely needs to be merged with DACSS.
 */
public class DepNdx implements DepSource {
	public static final Logger logger = StrataPlugin.getLogger(DepNdx.class);

	private RepositoryMgr reloRdfRepository;

	private DepRepoReplacement depRepo;

	private DepAndChildrenStrengthSummarizer dacss;

	public DepNdx(RepositoryMgr _repo, PckgDirRepo _pdr) {
		this(_repo, null, _pdr);
	}

	public DepNdx(RepositoryMgr _repo, IPath _path, PckgDirRepo _pdr) {
		this.reloRdfRepository = _repo;
		this.dacss = DepAndChildrenStrengthSummarizer.getDACSS(reloRdfRepository, _path, _pdr);
		this.depRepo = dacss.getDepRepo();
		this.dacss.ensureInitDepRepo();
	}

	// TODO: Make sure partitioner gets the same deps from here as the DepRels get from 'getFilteredDepsToAndFrom()' 
	public int getDep(ArtifactFragment src, ArtifactFragment dst, IProgressMonitor monitor, List<ArtifactFragment> selAFs) {
		return readDepFromCache(src, dst, monitor, selAFs);
	}
	
	private int readDepFromCache(ArtifactFragment srcAF, ArtifactFragment dstAF, IProgressMonitor monitor, List<ArtifactFragment> selAFs) {
		int depSrcDep = -1;
		if (monitor.isCanceled()) return 0;
		if (srcAF == dstAF) depSrcDep = 0;
		if (srcAF instanceof DerivedArtifact) depSrcDep = 0;
		if (dstAF instanceof DerivedArtifact) depSrcDep = 0;
		
		int srcChildren = 0;
		int dstChildren = 0;
		if (selAFs != null) {
			List<ArtifactFragment> srcChildrenLst = getAllChildrenFromSelection(selAFs, srcAF);
			List<ArtifactFragment> dstChildrenLst = getAllChildrenFromSelection(selAFs, dstAF);
			srcChildren = srcChildrenLst.size();
			dstChildren = dstChildrenLst.size();
		} else {
			srcChildren = ModelUtils.getAllNestedChildren(srcAF).size();
			dstChildren = ModelUtils.getAllNestedChildren(dstAF).size();
		}
		
		String srcName = getRepo().queryName(srcAF.getArt().elementRes);
		String dstName = getRepo().queryName(dstAF.getArt().elementRes);
		
		// skip comparing to the root
		if (srcName.equals("") || dstName.equals("")) {
			return 0;
		}
	
		// skip comparing parents to children
		if (srcName.contains(".*")) {
				srcName = srcName.replace(".*", "");
				if (dstName.startsWith(srcName))
					return 0;
		}
		if (dstName.contains(".*")) {
			dstName = dstName.replace(".*", "");
			if (srcName.startsWith(dstName))
				return 0;
		}
		if (monitor.isCanceled()) return 0;
    	// TODO: Try to find a more accurate estimate of number of tasks here
		monitor.beginTask("Calculate Dependencies - " + srcName + " --> " + dstName, (srcChildren+dstChildren)*dstChildren/**DepAndChildrenStrengthSummarizer.realToVirtual.size()*/);
		dacss.processed = 0;
		if (depSrcDep == -1)
			depSrcDep = dacss.readDepStrength(getRepo(), srcAF.getArt(), dstAF.getArt(), null,monitor, srcChildren,dstChildren/**DepAndChildrenStrengthSummarizer.realToVirtual.size()*/);
		
		return depSrcDep;
	}
	

	/////////////////
	// Utils //
	/////////////////
	public RepositoryMgr getRepo() {
		return reloRdfRepository;
	}
	
	private List<ArtifactFragment> getAllChildrenFromSelection(List<ArtifactFragment> selAFs, ArtifactFragment srcAF) {
		List<ArtifactFragment> retList = new ArrayList<ArtifactFragment>();
		for (ArtifactFragment child : selAFs) {
			if (child.getArt().elementRes.equals(srcAF.getArt().elementRes)) {
				retList.addAll(ModelUtils.getAllNestedChildren(child));
				break;
			} else
				retList.addAll(getAllChildrenFromSelection(child.getShownChildren(), srcAF));
		}
		
		return retList;
	}
	
	public int getSize() {
		// this is used for debugging, not sure if it matters that the size is now likely to be different
		return depRepo.cachedContainmentBasedRefTypeRelMap.size();
	}

	public List<ResourceDependencyRelation> getFilteredDepsToAndFrom(ArtifactFragment artBeingAdded, Map<Resource, ArtifactFragment> currentResourcesToAFMap, Map<Resource, Resource> resToParentMap, IProgressMonitor monitor) {
		String srcName = getRepo().queryName(artBeingAdded.getArt().elementRes);
		// skip comparing to the root
		if (srcName.equals("")) return new ArrayList<ResourceDependencyRelation>();
		return dacss.readFilteredDepStrength(getRepo(), artBeingAdded.getArt(), new ArrayList<Resource>(currentResourcesToAFMap.keySet()), resToParentMap, monitor);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Methods added to make sure that there is a single interface to DACSS - and they all go via this class
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public int getChildrenSize(Set<Artifact> containedObjs, ReloRdfRepository repo, Artifact art, URI virtualContainerHeirarchy) {
		return dacss.getChildrenSize(containedObjs, repo, art, virtualContainerHeirarchy);
	}

	public List<Artifact> getContainedArts(ReloRdfRepository repo, Artifact codeArt) {
		return dacss.getContainedArts(repo, codeArt);
	}

	public List<Artifact> queryContainedList(Artifact art, DirectedRel rel, Predicate filter) {
        List<Resource> retValAsResource = new LinkedList<Resource> ();
        if (rel.isFwd)
        	retValAsResource =  dacss.getDepRepo().getFwdStatements(art.elementRes, rel.res, null);
        else
        	retValAsResource =  dacss.getDepRepo().getRevStatements(null, rel.res, art.elementRes);
        CollectionUtils.filter(retValAsResource, filter);
        return Artifact.transformResourcesToArtifacts(new ArrayList<Resource>(retValAsResource));
	}

	public Set<Artifact> queryContainedSet(Artifact art, DirectedRel rel, Predicate filter) {
        return new HashSet<Artifact>(queryContainedList(art, rel, filter));
	}
	
}