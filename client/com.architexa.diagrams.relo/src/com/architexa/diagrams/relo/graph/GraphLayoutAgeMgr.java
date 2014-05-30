package com.architexa.diagrams.relo.graph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


import com.architexa.org.eclipse.draw2d.geometry.Point;
import com.architexa.org.eclipse.gef.EditPart;
import com.architexa.org.eclipse.gef.editparts.AbstractGraphicalEditPart;

/**
 * Old implementation - likely had bugs in it but also that the idea of aging
 * everything does not seem to be what we want.<br>
 * <br>
 * Aging only matters when we add new nodes, in which cases all the new nodes
 * have the same age.
 */
public class GraphLayoutAgeMgr implements IGraphLayoutAgeMgr {

	// store ages for every node
    private Map<AbstractGraphicalEditPart,Integer> partsToAgeMap = new HashMap<AbstractGraphicalEditPart,Integer> ();
    
    public void updateAges(Set<AbstractGraphicalEditPart> parts) {
    	// olden everything
        oldParts.addAll(parts);
    	
    	for (Map.Entry<AbstractGraphicalEditPart,Integer> ageEntry : partsToAgeMap.entrySet()) {
    		ageEntry.setValue(1 + ageEntry.getValue());
		}
    	for (AbstractGraphicalEditPart part : parts) {
    		partsToAgeMap.put(part, 0);
		}
    	// compact / re-order ages (so that no number is skipped)
    	Set<Integer> ageValues = new HashSet<Integer>(partsToAgeMap.values());
    	int prevAge = -1;
    	for (int currAge = 0; currAge < ageValues.size(); currAge++) {
    		prevAge++;
			if (ageValues.contains(prevAge)) continue;
			do {
				prevAge++;
			} while (ageValues.contains(prevAge));
			// move all prevAge -> currAge
			for (Map.Entry<AbstractGraphicalEditPart,Integer> ageEntry : partsToAgeMap.entrySet()) {
				if (ageEntry.getValue() == prevAge) ageEntry.setValue(currAge);
			}
		}
    }
    
    public void makeOld(AbstractGraphicalEditPart part) {
    	// since we are always compacted, we can just set age to size (and therefore max)
    	// add 1 just in case
    	partsToAgeMap.put(part, partsToAgeMap.size()+1);
    	//System.err.println("GLAM.Oldened: " + part);
    }
    
    public boolean hasAge(AbstractGraphicalEditPart part) {
    	return partsToAgeMap.containsKey(part);
    }
    
    public int getAge(AbstractGraphicalEditPart part) {
    	if (!partsToAgeMap.containsKey(part)) 
    		return -1;
    	else
    		return partsToAgeMap.get(part);
    }

    private Set<AbstractGraphicalEditPart> oldParts = new HashSet<AbstractGraphicalEditPart> ();

	public void anchorPart(AbstractGraphicalEditPart part, Point newLoc, Point oldLoc) {
		anchorPart(part);
	}
    public void anchorPart(AbstractGraphicalEditPart aep) {
		//System.err.println("GLAM.Anchoring: " + aep);
		oldParts.add(aep);
	}
	public boolean isPartAnchored(AbstractGraphicalEditPart agep) {
		return isOldPart(agep);
	}

    public boolean isLayedOut(AbstractGraphicalEditPart agep) {
        if (oldParts.contains(agep))
            return true;
        else
            return false;
    }
    public void setLayedOut(AbstractGraphicalEditPart agep) {
        //Vertex agepVertex = new Vertex(new Graph());
        //agepVertex.setBounds(agep.getFigure().getBounds());
        //agepVertex.data = agep;
    	
        /*
        MoreItemsEditPart aep = (MoreItemsEditPart) agep;
        if (oldPartsToCellMap.get(agep) != null) {
            Rectangle origBounds = ((Cell)oldPartsToCellMap.get(agep)).getBounds();
            Rectangle newBounds = agepVertex.getBounds();
			printMove(1, aep, getGraphID(oldPartsToCellMap.get(aep)), origBounds, -1, newBounds);
        }
		*/
        this.oldParts.add(agep);
		//System.err.println("GLAM.setLayedOut: " + agep);
    }

	public boolean isOldPart(EditPart editPart) {
		return oldParts.contains(editPart);
	}

	public void clearOldParts() {
		oldParts.clear();
	}

	public Set<AbstractGraphicalEditPart> getOldParts() {
		return oldParts;
	}

}
