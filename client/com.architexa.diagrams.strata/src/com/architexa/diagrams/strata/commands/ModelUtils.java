package com.architexa.diagrams.strata.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.ui.PlatformUI;
import org.openrdf.model.Resource;

import com.architexa.diagrams.RSECore;
import com.architexa.diagrams.jdt.RJCore;
import com.architexa.diagrams.model.Artifact;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.model.ArtifactRel;
import com.architexa.diagrams.model.DirectedRel;
import com.architexa.diagrams.strata.StrataPlugin;
import com.architexa.diagrams.strata.model.DependencyRelation;
import com.architexa.diagrams.strata.model.StrataFactory;
import com.architexa.diagrams.strata.model.StrataRootDoc;
import com.architexa.diagrams.strata.model.policy.ClosedContainerDPolicy;
import com.architexa.diagrams.strata.parts.ContainableEditPart;
import com.architexa.diagrams.strata.parts.StrataArtFragEditPart;
import com.architexa.diagrams.strata.ui.SelectionCollector;
import com.architexa.diagrams.ui.LongCommand;
import com.architexa.diagrams.utils.ErrorUtils;
import com.architexa.org.eclipse.draw2d.geometry.Point;
import com.architexa.org.eclipse.gef.commands.CommandStack;
import com.architexa.org.eclipse.gef.commands.CompoundCommand;
import com.architexa.store.ReloRdfRepository;

public class ModelUtils {
	public static final Logger logger = StrataPlugin.getLogger(ModelUtils.class);

	public static List<ArtifactRel> removeAF(StrataRootDoc rootArt, ArtifactFragment parentAF, ArtifactFragment childAF) {
		if (parentAF ==null || parentAF.getParentArt() == null) 
			return Collections.emptyList();
	
		StrataArtFragEditPart.logger.info("Removing: " + childAF + " of parent: " + parentAF);
	
		List<ArtifactRel> removedRels = rootArt.removeRelationships(childAF);
	
		parentAF.removeShownChild(childAF);
		
		return removedRels;
	}

	public static void addAF(ArtifactFragment parentAF, ArtifactFragment childAF, List<ArtifactRel> relationshipsToAdd) {
		StrataArtFragEditPart.logger.info("Adding: " + childAF + " to parent: " + parentAF);
	
		StrataRootDoc rootDoc = (StrataRootDoc) parentAF.getRootArt();
	
		rootDoc.addRelationships(relationshipsToAdd);
	
		parentAF.appendShownChild(childAF);
	}

	public static void toggleOpenArtFrag(ArtifactFragment artFrag, CommandStack cmdStack) {
	        if (ClosedContainerDPolicy.isShowingChildren(artFrag))
	            hideArtFragChildren(artFrag, cmdStack);
	        else 
	            showArtFragChildren(artFrag, cmdStack);
	}

	public static void showArtFragChildren(ArtifactFragment artFrag, CommandStack cmdStack) {
			StrataArtFragEditPart.logger.info("Showing: " + artFrag);
			CompoundCommand showAFEPChildrenCmd = new CompoundCommand("Show Children");
	        ClosedContainerDPolicy.queryAndShowChildren(showAFEPChildrenCmd, artFrag);
	        ClosedContainerDPolicy.showAllSingleChildren(showAFEPChildrenCmd,artFrag);
	        
	        LongCommand.checkForSchedulingPrepAndExecute(cmdStack, showAFEPChildrenCmd);
		}

	public static void hideArtFragChildren(ArtifactFragment artFrag, CommandStack cmdStack) {
		StrataArtFragEditPart.logger.info("Hiding: " + artFrag);
		CompoundCommand hideAFEPChildrenCmd = new CompoundCommand("Hide Children");
		ClosedContainerDPolicy.hideChildren(hideAFEPChildrenCmd, artFrag);
		LongCommand.checkForSchedulingPrepAndExecute(cmdStack, hideAFEPChildrenCmd);
	}

	public static void showArtFragChild(ArtifactFragment child, ArtifactFragment parentAF, CommandStack cmdStack) {
	    	CompoundCommand showAFEPChildCmd = new CompoundCommand("Show Child");
	    	
	    	ClosedContainerDPolicy.addAndShowChild(showAFEPChildCmd, parentAF, child);
	
	    	LongCommand.checkForSchedulingPrepAndExecute(cmdStack, showAFEPChildCmd);
	    }

	
	
	// gets all the model children
	public static List<ArtifactFragment> getAllNestedChildren(ArtifactFragment parentArtFrag) {
		List<ArtifactFragment> allShownChildren = new ArrayList<ArtifactFragment>();
				
		allShownChildren.add(parentArtFrag);
		for (ArtifactFragment afChild : parentArtFrag.getShownChildren()) {
			allShownChildren.addAll(getAllNestedChildren(afChild));	
		}
		return allShownChildren;
	}

	// TODO move below to a Query Utils package?
	
	// get the children of this art whether packageDirectory, package, or class
	// returns all children including methods and fields
	public static List<Artifact> queryChldren(Artifact pckgCU, ReloRdfRepository repo) {
		List<Artifact> pckgChildren = pckgCU.queryPckgDirContainsArtList(repo, DirectedRel.getFwd(RJCore.pckgDirContains));
		if (pckgChildren.size() == 0)
			pckgChildren = pckgCU.queryArtList(repo, DirectedRel.getFwd(RSECore.contains));
		return pckgChildren;
	}

	public static List<Artifact> getAllPackageChildrenFromRepo(Artifact art, ReloRdfRepository repo) {
		List<Artifact> allChildren = new ArrayList<Artifact>(queryChldren(art, repo));
		for (final Artifact childArt : queryChldren(art, repo)) {
			if (childArt.isInitialized(repo) && (childArt.queryType(repo).equals(RJCore.indirectPckgDirType)))
				allChildren.addAll(getAllPackageChildrenFromRepo(childArt, repo));
		}
		return allChildren;
	}

	
//	public static CompoundCommand collectAndOpenItems(List<?> objectsToCollectAndOpen, StrataRootDoc rootModel) {
//		return collectAndOpenItems(objectsToCollectAndOpen, rootModel, null, null);
////		return collectAndOpenItemsNew(objectsToCollectAndOpen, rootModel, null, null);
//	}

	/**
	 * Used by the exploration server to open a diagram based on a given id.
	 * This needs to be merged with the method below
	 */
	public static CompoundCommand collectAndOpenExplCodeItems(List<?> objectsToCollectAndOpen, StrataRootDoc rootModel, Point createDropLoc, ContainableEditPart epParent) {
		Map<Artifact, ArtifactFragment> addedArtToAFMap = new HashMap<Artifact, ArtifactFragment>();
		List<Artifact> allArts = new ArrayList<Artifact>();
		for (Object obj : objectsToCollectAndOpen) {
			ArtifactFragment pckg = (ArtifactFragment) obj;
			StrataFactory.initAF(pckg);
			allArts.add(pckg.getArt());
			addedArtToAFMap.put(pckg.getArt(), pckg);
			List<Artifact> children = pckg.getArt().queryChildrenArtifacts(rootModel.getRepo());
			for(Artifact child : children) {
				allArts.add(child);
				addedArtToAFMap.put(child, rootModel.createArtFrag(child));
			}
		}
		CompoundCommand cc = new CompoundCommand();
		for (Artifact relArt : allArts) {
			StrataArtFragEditPart.addArtAndContainers(cc, addedArtToAFMap, relArt, false, rootModel, null, new HashMap<Resource, Resource>());
		}
		cc.add(new AddAllRelsCommand(rootModel, addedArtToAFMap, allArts, new ArrayList<ArtifactFragment>()));
		return cc;
	}
	
	/**
	 * Runs primarily on IJE's. It interferes with pure forms above (this needs fixing)
	 */
	public static CompoundCommand collectAndOpenItems(List<?> objectsToCollectAndOpen, StrataRootDoc rootModel, Point createDropLoc, ContainableEditPart epParent) {
		
		SelectionCollector selColl = new SelectionCollector(rootModel);
		selColl.collectMultiType((List<?>)objectsToCollectAndOpen );
		selColl.removeSingleChildArtFrags();
		
		if (selColl.getArtFragList().isEmpty()) {
			//logger.error("Not enough dependencies");
			String msg = "Architexa has encountered a problem while attempting to open this Diagram \n\n" +
				"Not enough dependencies available to create a diagram \n" +
				"Consider Rebuilding your Architexa Index \n" +
				"File -> Architexa -> Rebuild Complete Index";
				
			ErrorUtils.openBuildError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), msg);
			return new CompoundCommand();
		}
		CompoundCommand cc = new CompoundCommand();
		Map<Artifact, ArtifactFragment> addedArtToAFMap = new HashMap<Artifact, ArtifactFragment>();
		List<Artifact> allArts = new ArrayList<Artifact>();
		
		// remove hierarchy structure from selection collector to find
		// the number of actual nodes being added
		for (ArtifactFragment af : selColl.getArtFragList()) {
			// Instead of trying to show here, Show only originally selected children at the end of addition
			// ClosedContainerDPolicy.setShowingChildren(af, true);
			allArts.addAll(getAllArts(af));	
			allArts.add(af.getArt());
		}
		// If Only creating one item move it to the correct spot within drop target
		if (epParent!=null && selColl.getArtFragList().size() == 1 && getAllArts(selColl.getArtFragList().get(0)).size() == 1) {
			StrataArtFragEditPart.addSingleArt(cc, createDropLoc, epParent, selColl.getArtFragList().get(0), rootModel);
		} else {
			List<ArtifactFragment> orgSelectedAFs = selColl.getSelectedAFs();
			for (Artifact relArt : allArts) {
				if (addedArtToAFMap.containsKey(relArt)) continue;
				StrataArtFragEditPart.addArtAndContainers(cc, addedArtToAFMap, relArt, false, rootModel, null, selColl.mapRDFtoProjRes);
			}
			for (ArtifactFragment af: orgSelectedAFs) {
				cc.add(new ShowAllParentsCommand(af, "Collect and Open", true, rootModel, addedArtToAFMap));
			}
			cc.add(new AddAllRelsCommand(rootModel, addedArtToAFMap, allArts, selColl.getArtFragList()));
		}
		return cc;
	}
	
	private static List<Artifact> getAllArts(ArtifactFragment afParent) {
    	List<Artifact> childrenArts = new ArrayList<Artifact>();
    	if (afParent.getArt().elementRes == null) return null;
    	childrenArts.add(afParent.getArt());
    	for (ArtifactFragment afChild : afParent.getShownChildren()) {
			childrenArts.addAll(getAllArts(afChild));
    	}
    	return childrenArts;
	}

	public static int getDep(ArtifactFragment srcAF, ArtifactFragment dstAF) {
		int total = 0;
		List<ArtifactRel> srcDeps = srcAF.getSourceConnections();
		for (ArtifactRel srcRel : srcDeps) {
			if (!(srcRel instanceof DependencyRelation)) continue;
			if (srcRel.getDest().getArt().elementRes.equals(dstAF.getArt().elementRes))
				total += ((DependencyRelation)srcRel).depCnt;
		}
		List<ArtifactRel> dstDeps = dstAF.getSourceConnections();
		for (ArtifactRel dstRel : dstDeps) {
			if (!(dstRel instanceof DependencyRelation)) continue;
			if (dstRel.getDest().getArt().elementRes.equals(srcAF.getArt().elementRes))
				total += ((DependencyRelation)dstRel).revDepCnt;
		}
		return total;
	}

}
