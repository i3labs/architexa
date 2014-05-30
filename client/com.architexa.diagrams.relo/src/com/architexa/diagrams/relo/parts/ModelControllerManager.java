package com.architexa.diagrams.relo.parts;

import org.apache.log4j.Logger;

import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.relo.ReloPlugin;
import com.architexa.org.eclipse.gef.EditPart;


public class ModelControllerManager {
    static final Logger logger = ReloPlugin.getLogger(ModelControllerManager.class);

    // model modification methods (these also modify editpart as well)
    // note: for child adding (as opposed to edit part adding) we support addition
    // based on a  given edit part, since reparenting happens often (can do for connections
    //  but not really needed)
    
    public static ArtifactEditPart appendModelAndChild(ArtifactFragment tgtAF, Object child, ReloController rc) {
    	ArtifactFragment childAF = rc.getRootArtifact().getArtifact(child);
    	tgtAF.appendShownChild(childAF);
    	return rc.findArtifactEditPart(childAF);
    }
    
    

    public static void moveModelAndChildKeepingSelection(
                ArtifactEditPart childAGEP, 
                ArtifactEditPart oldAEP, 
                ArtifactEditPart newAEP) {
        
    	// maintain childs selection
    	boolean childSel = false;
    	boolean childFoc = false;
    	if (childAGEP.getSelected() != EditPart.SELECTED_NONE) childSel = true;
    	if (childAGEP.hasFocus()) childFoc = true;
    	
    	ArtifactFragment childAF = childAGEP.getArtifact();
    	ReloController rc = newAEP.getRootController();

        moveModelAndChild(childAF, oldAEP.getArtifact(), newAEP.getArtifact());
        
        childAGEP = rc.findArtifactEditPart(childAF); 
        
        //childAGEP.setSelected(sel);
    	if (childSel) childAGEP.getRoot().getViewer().appendSelection(childAGEP);
    	if (childFoc) childAGEP.getRoot().getViewer().setFocus(childAGEP);
    }
    
    public static void moveModelAndChild(
            ArtifactFragment childAF, 
            ArtifactFragment oldAF, 
            ArtifactFragment newAF) {
    
    //// append really does the move as well
    //appendModelAndChild(newAEP, childAGEP.getArtifact(), childAGEP);
	//  ... move is done by appending to newEP
    newAF.appendShownChild(childAF);
}

}
