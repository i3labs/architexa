/**
 * 
 */
package com.architexa.diagrams.relo.commands;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.openrdf.model.Resource;

import com.architexa.diagrams.RSECore;
import com.architexa.diagrams.model.Artifact;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.model.ArtifactRel;
import com.architexa.diagrams.model.Comment;
import com.architexa.diagrams.model.RootArtifact;
import com.architexa.diagrams.parts.PointPositionedDiagramPolicy;
import com.architexa.diagrams.relo.graph.GraphLayoutManager;
import com.architexa.diagrams.relo.modelBridge.ReloDoc;
import com.architexa.diagrams.relo.parts.ArtifactEditPart;
import com.architexa.diagrams.relo.parts.ReloController;
import com.architexa.diagrams.utils.ErrorUtils;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.LayoutManager;
import com.architexa.org.eclipse.draw2d.geometry.Point;
import com.architexa.org.eclipse.gef.commands.Command;
import com.architexa.org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import com.architexa.org.eclipse.gef.requests.CreateRequest;

public final class CreateCommand extends Command {
	/**
	 * 
	 */
	private final ReloController rc;
	private final Point createLoc;
	private Map<ArtifactFragment,ArtifactFragment> addedAFsToParentAFs = new LinkedHashMap<ArtifactFragment,ArtifactFragment>();
	private List<Resource> addedResources = new ArrayList<Resource>();
	public boolean doLayout = false;

	private CreateCommand(ReloController rc, Object newObj, Point createLoc) {
		super("Add Elements from Package Explorer");
		this.rc = rc;
		this.createLoc = createLoc;
		addedAFsToParentAFs.clear();
		
		// Creates list of AFs that have been created so that
		// showIncludedRelationships can know about them
		createAFs(newObj);
	}
	
	public CreateCommand(ReloController rc, CreateRequest req) {
		this(rc, req.getNewObject(), req.getLocation());
	}
	
	public CreateCommand(ReloController rc, Object newObj) {
		this(rc, newObj, null);
	}
	
	private void createAFs(Object newObj) {
		
		if (newObj instanceof List<?>){
			Iterator<?> listIt = getIterator(newObj).iterator();
			boolean errorShown = false;
			while (listIt.hasNext()) {
		        ArtifactFragment newAF = rc.getRootArtifact().getArtifact(listIt.next());
		        
		        boolean isValidType = ErrorUtils.isValidType(newAF, rc.getRepo(), errorShown);
		        if (!isValidType) {
		        	errorShown = !isValidType;
		        	continue;
		        }
		        
		      	// Do Layout if we are dragging in multiple items or 1 or more packages
		    	boolean isContainer = rc.getReloDoc().getBrowseModel().containerArtifact(newAF.getArt(), newAF.getArt().queryType(rc.getRepo()));
		  
				// find closest possible parent if one exists, null otherwise
		    	ArtifactFragment closestParentAF = findClosestParent(newAF, rc, createLoc);
		    	
		    	if (!addedResources.contains(newAF.getArt().elementRes) && !isContainer) {
		    		addedAFsToParentAFs.put(newAF, closestParentAF);
		        	addedResources.add(newAF.getArt().elementRes);
		        } else {
		        	// ArtifactFragment with same Resource may already exist, 
		        	// but the children and connections of the two ArtFrags may 
		        	// not match. So, adding any children and connections of newAF 
		        	// to its matching ArtFrag that the matching ArtFrag doesn't 
		        	// already have
		        	for(ArtifactFragment addedAF : addedAFsToParentAFs.keySet() ) {
		        		if(!addedAF.getArt().elementRes.equals(newAF.getArt().elementRes)) continue;
		        		for(ArtifactFragment newChild : new ArrayList<ArtifactFragment>(newAF.getShownChildren())) {
		        			if(addedAF.getShownChildren().contains(newChild)) continue;
		        			addedAF.appendShownChild(newChild);
		        		}
		        		for(ArtifactRel newSrcRel : new ArrayList<ArtifactRel>(newAF.getSourceConnections())) {
		        			if(addedAF.getSourceConnections().contains(newSrcRel)) continue;
		        			addedAF.addSourceConnection(newSrcRel);
		        		}
		        		for(ArtifactRel newTgtRel : new ArrayList<ArtifactRel>(newAF.getTargetConnections())) {
		        			if(addedAF.getTargetConnections().contains(newTgtRel)) continue;
		        			addedAF.addTargetConnection(newTgtRel);
		        		}
		        	}
		        }
		    	
		        
		    	// add children AFs if we are creating a package
		    	if (isContainer) {
		    		for (Artifact childAF : newAF.getArt().queryChildrenArtifacts(rc.getRepo())) {
						if (rc.getRootArtifact().getMatchingNestedShownChildren(childAF).isEmpty() && !addedAFsToParentAFs.keySet().contains(childAF))
								createAFs(childAF);
					}
		    		
				}
		    	if (isContainer || ((List<?>) newObj).size() > 1) 
					doLayout = true;
			}
		    
			
		    IFigure layoutFig = ((AbstractGraphicalEditPart) rc.getRoot().getContents()).getFigure();
	        LayoutManager layoutFigLM = layoutFig.getLayoutManager();
	        layoutFigLM.layout(layoutFig);
		} else {
			ArtifactFragment newAF = rc.getRootArtifact().getArtifact(newObj);
			ArtifactFragment closestParentAF = findClosestParent(newAF, rc, createLoc);
			if (!addedResources.contains(newAF.getArt().elementRes)) {
				addedAFsToParentAFs.put(newAF, closestParentAF);
	        	addedResources.add(newAF.getArt().elementRes);
	        }
		}
	}
	
	// find closest possible parent if one exists, null otherwise
	public static ArtifactFragment findClosestParent(ArtifactFragment newAF, ReloController rc, Point createLoc) {
		Artifact parentArt =  newAF.getArt().queryParentArtifact(rc.getRepo());
		// return null if parent af is not in index
		// however if parent is in library code return that
		if (parentArt == null) { 
			if (newAF.getParentArt()!=null) return newAF.getParentArt();
			else if(RSECore.isUserCreated(rc.getRepo(), newAF.getArt().elementRes))
				return rc.getBrowseModel().getUserCreatedEnclosingFrag(newAF);
			else 
				return null;
		}
		List<ArtifactFragment> parentAFs = rc.getRootArtifact().getMatchingNestedShownChildren(parentArt);
		if (parentAFs.isEmpty()) return null;
		
		ArtifactFragment closestParent = null;
		double tmpDist = Double.MAX_VALUE;
		for (ArtifactFragment parent : parentAFs) {
			Point tmpLoc = new Point(createLoc);
	    	ArtifactEditPart aep = rc.findArtifactEditPart(parent);
	    	aep.getFigure().translateToRelative(tmpLoc);
	    	aep.getFigure().translateFromParent(tmpLoc);
	    	
	    	// ignore centers if dropping within the bounds of a figure
	    	if (aep.getFigure().getBounds().contains(createLoc)) return parent;

	    	Point center = aep.getFigure().getBounds().getCenter();
			double dist = Point2D.distance(center.x, center.y, tmpLoc.x, tmpLoc.y);
			if (dist < tmpDist) {
				closestParent = parent;
				tmpDist = dist;
			}
		}
		return closestParent;
	}

	@SuppressWarnings("unchecked")
	private List<ArtifactFragment> getIterator(Object reqNewObj) {
		return ((List<ArtifactFragment>) reqNewObj);
	}

	@Override
	public void execute() {
		addedAFsToParentAFs = sortByComparator(addedAFsToParentAFs);
	    for (ArtifactFragment af : addedAFsToParentAFs.keySet()) {
			createEP(af, addedAFsToParentAFs.get(af));
		}
	}
	
	 private static Map sortByComparator(Map unsortMap) {
	    List list = new LinkedList(unsortMap.entrySet());
	        //sort list based on comparator
        Collections.sort(list, new Comparator() {
             public int compare(Object o1, Object o2) {
 				return ((Comparable) ((Map.Entry) (o1)).getKey().toString().length()).compareTo(((Map.Entry) (o2)).getKey().toString().length());
 			}
		});
	    //put sorted list into map again
		Map sortedMap = new LinkedHashMap();
		for (Iterator it = list.iterator(); it.hasNext();) {
		     Map.Entry entry = (Map.Entry)it.next();
		     sortedMap.put(entry.getKey(), entry.getValue());
		}
		return sortedMap;
	 }	
	
	@Override
	public void undo() {
		for(ArtifactFragment af : addedAFsToParentAFs.keySet() ){
			for (ArtifactRel rel : new ArrayList<ArtifactRel>(af.getSourceConnections())) {
				RootArtifact.hideRel(rel);
			}
	        for (ArtifactRel rel : new ArrayList<ArtifactRel>(af.getShownTargetConnections())) {
				RootArtifact.hideRel(rel);
			}
	        
	        RootArtifact ra = rc.getRootArtifact(); 
	        if (af instanceof Comment)
	        	ra.removeShownChild((Comment)af);
	        else 
	        	ra.removeVisibleArt(af);
		}
	}
	
	private ArtifactFragment createEP(ArtifactFragment af, ArtifactFragment parentAF) {
		//logger.debug("Trying to create artifact of class: " + art.getClass());
	    // TODO: createArtifactEditPart executes a add command, we need to refactor in order to avoid this
		HashMap<Artifact, ArtifactFragment> addedMap = new HashMap<Artifact, ArtifactFragment>();
		if (parentAF == null)
			parentAF = af.getParentArt();
		if (parentAF != null)
			addedMap.put(parentAF.getArt(), parentAF);
		
		// If no parent is found the Root Artifact creates one in the call below
		AbstractGraphicalEditPart aep = 
	    	(af instanceof Comment) ? rc.createOrFindCommentEditPart(af) : rc.createArtifactEditPart(af, addedMap);
	    	
	    if (createLoc!=null && aep!= null) {
	    	aep.getFigure().translateToRelative(createLoc);
	    	aep.getFigure().translateFromParent(createLoc);

	    	PointPositionedDiagramPolicy.setLoc(af, createLoc);
	    	if (!doLayout) {
	 	    	IFigure layoutFig = ((AbstractGraphicalEditPart) aep.getRoot().getContents()).getFigure();
	 	        LayoutManager layoutFigLM = layoutFig.getLayoutManager();
	 	        if (layoutFigLM != null) 
	 	            if (layoutFigLM instanceof GraphLayoutManager && aep instanceof ArtifactEditPart) 
	 	                ((GraphLayoutManager) layoutFigLM).anchorPart((ArtifactEditPart) aep);
	 	    }
	    	
	    	// New Parent has been created for a user created method or field, set its location
	    	if (parentAF == null) {
		    	parentAF = af.getParentArt();
		    	if (parentAF != null && !(parentAF instanceof ReloDoc))
		    		PointPositionedDiagramPolicy.setLoc(parentAF, createLoc);
	    	}
	    } 
	   
	    return af;
	}
	
	public List<ArtifactFragment> getAddedAFs(){
		return new ArrayList<ArtifactFragment>(addedAFsToParentAFs.keySet());
	}
}