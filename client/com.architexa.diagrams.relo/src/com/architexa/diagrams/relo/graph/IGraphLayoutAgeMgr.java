package com.architexa.diagrams.relo.graph;

import java.util.Set;


import com.architexa.org.eclipse.draw2d.geometry.Point;
import com.architexa.org.eclipse.gef.EditPart;
import com.architexa.org.eclipse.gef.editparts.AbstractGraphicalEditPart;

/**
 * Center point for dealing with user selections<br>
 * <br>
 * TODO: work towards removing old concepts
 */
public interface IGraphLayoutAgeMgr {
	
	// we need selections to also update ages

    // The item with the greatest age will not be moved 
    public int getAge(AbstractGraphicalEditPart part);
    public boolean hasAge(AbstractGraphicalEditPart part);
    public void updateAges(Set<AbstractGraphicalEditPart> parts);    
    public void makeOld(AbstractGraphicalEditPart part);
    
    // first attempt: nodes could be anchored by the user and therefore have a controlled layout
    // currently all parts are effectively anchored
	public void anchorPart(AbstractGraphicalEditPart aep);
	public void anchorPart(AbstractGraphicalEditPart part, Point newLoc, Point oldLoc);
	public boolean isPartAnchored(AbstractGraphicalEditPart agep);

	// currently all layed out nodes are anchored
	public boolean isLayedOut(AbstractGraphicalEditPart agep);
	public void setLayedOut(AbstractGraphicalEditPart agep);
	
	// accessors - really the same as anchoring and layed out
	public boolean isOldPart(EditPart editPart);
	public void clearOldParts();
	public Set<AbstractGraphicalEditPart> getOldParts();
}