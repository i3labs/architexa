/**
 * Copyright (c) 2007-2008 Architexa, Inc
 */ 
package com.architexa.diagrams.strata.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;

import com.architexa.diagrams.RSECore;
import com.architexa.diagrams.jdt.RJCore;
import com.architexa.diagrams.model.Artifact;
import com.architexa.diagrams.model.DirectedRel;
import com.architexa.diagrams.strata.StrataPlugin;
import com.architexa.diagrams.strata.model.processors.DepStrengthSummarizerProcessor_Old;
import com.architexa.store.ReloRdfRepository;


/**
 * TODO: move this into SS code
 * 
 * This class doesn't really work, and therefore is not really being called, but
 * we need this functionality eventually. This functionality tries to allows
 * SS-Documents to be openable and be found by the system, though the 'being
 * found by the system' doesn't happen yet, and the allow to open can be done
 * differently.
 * 
 * These SS-Documents are created as virtual containers
 * 
 * @author vineet
 * 
 */
public class StrataDepStrengthSummarizerProcessor extends DepStrengthSummarizerProcessor_Old {
    static final Logger logger = StrataPlugin.getLogger(StrataDepStrengthSummarizerProcessor.class);

    // virtualContainers are interesting in that they are arbitrary subsets and
	// may not necessarily be a tree, they are therefore typically processed
	// last (virtualContainers containment relationships cannot have cycles in
	// them)
    // virtualContainer are SS-SPECIFIC
	public static final URI virtualContainer = RSECore.createRseUri("jdt#virtualContainer");
	public static final URI virtualContainerHeirarchy = RSECore.createRseUri("jdt#virtualContainerHeirarchy");

	public void addAnnotations(Resource currProjRes) {
		processRes(currProjRes);
    	
    	buildVirtualContainers();
	}

	private void buildVirtualContainers() {
		List<Resource> virtualContainers = new LinkedList<Resource>();
		rdfRepo.getResourcesFor(virtualContainers, null, rdfRepo.rdfType, virtualContainer);
		for (Resource virtualContainer : virtualContainers) {
			buildVirtualConatiner(rdfRepo, new Artifact(virtualContainer));
		}
	}

	public static void buildVirtualConatiner(ReloRdfRepository repo, Artifact virtualContainerCU) {
		buildVirtualConatiner(repo, virtualContainerCU, RJCore.calls, RJCore.containmentBasedCalls);
		buildVirtualConatiner(repo, virtualContainerCU, RJCore.inherits, RJCore.containmentBasedInherits);
		buildVirtualConatiner(repo, virtualContainerCU, RJCore.refType, RJCore.containmentBasedRefType);
	}

	private static void buildVirtualConatiner(ReloRdfRepository repo, Artifact virtualContainerCU, URI rel, URI virtualRel) {
		// by now the base dependencies would have been built, so just use them with the virtualRel
		HashMap<Artifact, Integer> depDstStrength = buildChildren_depDstStrength(repo, virtualContainerCU, virtualRel);
		//logger.info("depDstStrength.size: " + depDstStrength.size());
		writeDepStrength(repo, virtualContainerCU, virtualRel, depDstStrength);

		HashMap<Artifact, Integer> rev_depDstStrength = rev_buildChildren_depDstStrength(repo, virtualContainerCU, virtualRel);
		//logger.info("depDstStrength.size: " + rev_depDstStrength.size());
		rev_write_depDstStrength(repo, virtualContainerCU, virtualRel, rev_depDstStrength);
	}
	protected static void rev_write_depDstStrength(ReloRdfRepository repo, Artifact currCU, URI virtualRel, HashMap<Artifact, Integer> rev_depDstStrength) {
		for (Map.Entry<Artifact, Integer> rev_depStrengthEntry : rev_depDstStrength.entrySet()) {
			Artifact srcCU = rev_depStrengthEntry.getKey();
			Integer srcCUStrength = rev_depStrengthEntry.getValue();
			repo.addStatement(srcCU.elementRes, virtualRel, currCU.elementRes);
			Statement depStmt = repo.getStatement(srcCU.elementRes, virtualRel, currCU.elementRes);
			repo.addStatement(depStmt, RJCore.containmentBasedDepStrength, srcCUStrength.toString());
		}
		// TODO: before adding the below we should double check both ways
		repo.addStatement(currCU.elementRes, RJCore.containmentCacheValid, true);
	}

	protected static HashMap<Artifact, Integer> rev_buildChildren_depDstStrength(ReloRdfRepository repo, Artifact tgtCU, URI rel) {
		HashMap<Artifact, Integer> rev_depDstStrength = new HashMap<Artifact, Integer> (100);
		
		for (Artifact childCU : getContainedCUs(repo, tgtCU)) {
			addStrengthsMap(rev_depDstStrength, rev_buildAndWithChildren_depDstStrength(repo, childCU, rel));
		}
		
		return rev_depDstStrength;
	}
	private static HashMap<Artifact, Integer> rev_buildAndWithChildren_depDstStrength(ReloRdfRepository repo, Artifact tgtCU, URI rel) {
		HashMap<Artifact, Integer> rev_depDstStrength = rev_build_depDstStrength(repo, tgtCU, rel);
		
		for (Artifact childCU : getContainedCUs(repo, tgtCU)) {
			addStrengthsMap(rev_depDstStrength, rev_buildAndWithChildren_depDstStrength(repo, childCU, rel));
		}
		
		return rev_depDstStrength;
	}
	private static HashMap<Artifact, Integer> rev_build_depDstStrength(ReloRdfRepository repo, Artifact dstCU, URI rel) {
		HashMap<Artifact, Integer> rev_depDstStrength = new HashMap<Artifact, Integer> (100);

		List<Artifact> srcCUs = dstCU.queryArtList(repo, DirectedRel.getRev(rel));
		for (Artifact srcCU : srcCUs) {
			List<Artifact> parentCUs = getContainerCUs(repo, srcCU);
			for (Artifact parentCU : parentCUs) {
				int oldStrength = 0;
				if (rev_depDstStrength.containsKey(parentCU)) oldStrength = rev_depDstStrength.get(parentCU);
				rev_depDstStrength.put(parentCU, oldStrength + 1);
			}
		}
		
		return rev_depDstStrength;
	}

	private static HashMap<Artifact, Integer> buildChildren_depDstStrength(ReloRdfRepository repo, Artifact currCU, URI rel) {
		HashMap<Artifact, Integer> depDstStrength = new HashMap<Artifact, Integer> (100);
		
		for (Artifact childCU : getContainedCUs(repo, currCU)) {
			addStrengthsMap(depDstStrength, buildAndWithChildren_depDstStrength(repo, childCU, rel));
		}
		
		return depDstStrength;
	}

	private static HashMap<Artifact, Integer> buildAndWithChildren_depDstStrength(ReloRdfRepository repo, Artifact currCU, URI rel) {
		HashMap<Artifact, Integer> depDstStrength = buildDepStrength(repo, currCU, rel);
		
		for (Artifact childCU : getContainedCUs(repo, currCU)) {
			addStrengthsMap(depDstStrength, buildAndWithChildren_depDstStrength(repo, childCU, rel));
		}
		
		return depDstStrength;
	}

	protected static List<Artifact> getContainedCUs(ReloRdfRepository repo, Artifact cu) {
		List<Artifact> listCU = null;

		listCU = cu.queryArtList(repo, DirectedRel.getFwd(virtualContainerHeirarchy));

		if (listCU.size() == 0) {
			listCU = cu.queryPckgDirContainsArtList(repo, DirectedRel.getFwd(RJCore.pckgDirContains));
		}

		if (listCU.size() == 0) {
			listCU = cu.queryArtList(repo, DirectedRel.getFwd(RSECore.contains));
		}
		return listCU;
	}
	private static List<Artifact> getContainerCUs(ReloRdfRepository repo, Artifact cu) {
		List<Artifact> parentCUs = new ArrayList<Artifact> (20);
		parentCUs.add(cu);
		getContainerCUs(repo, cu, parentCUs);
		return parentCUs;
	}
	protected static void getContainerCUs(ReloRdfRepository repo, Artifact cu, List<Artifact> collectorList) {
		List<Artifact> parentCUs = null;

		parentCUs = cu.queryArtList(repo, DirectedRel.getRev(virtualContainerHeirarchy));

		if (parentCUs.size() == 0)
			parentCUs = cu.queryPckgDirContainsArtList(repo, DirectedRel.getRev(RJCore.pckgDirContains));

		if (parentCUs.size() == 0)
			parentCUs = cu.queryArtList(repo, DirectedRel.getRev(RSECore.contains));
		
		// eliminate virtualContainer's
		/*
		Iterator<Artifact> parentCUIt = parentCUs.listIterator();
		while (parentCUIt.hasNext()) {
			Artifact parentCU = parentCUIt.next();
			if (repo.hasStatement(parentCU.elementRes, repo.rdfType, RJCore.virtualContainer)) parentCUIt.remove();
		}
		*/

		if (parentCUs.size() == 1) {
			collectorList.add(parentCUs.get(0));
			getContainerCUs(repo, parentCUs.get(0), collectorList);
			return;
		}

		if (parentCUs.size() > 1) {
			logger.error("Multiple containers of: " + cu + " :: " + parentCUs);
		}
		
		return;
	}
}