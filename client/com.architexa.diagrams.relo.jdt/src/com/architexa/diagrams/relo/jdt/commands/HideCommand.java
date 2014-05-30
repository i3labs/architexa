package com.architexa.diagrams.relo.jdt.commands;

import java.util.ArrayList;
import java.util.List;


import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.model.DerivedArtifact;
import com.architexa.diagrams.parts.PointPositionedDiagramPolicy;
import com.architexa.diagrams.relo.graph.GraphLayoutManager;
import com.architexa.diagrams.relo.jdt.parts.CodeUnitEditPart;
import com.architexa.diagrams.relo.jdt.parts.PackageEditPart;
import com.architexa.diagrams.relo.parts.ArtifactEditPart;
import com.architexa.diagrams.relo.parts.ModelControllerManager;
import com.architexa.diagrams.relo.parts.ReloController;
import com.architexa.org.eclipse.draw2d.Figure;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.LayoutManager;
import com.architexa.org.eclipse.gef.EditPart;
import com.architexa.org.eclipse.gef.commands.Command;
import com.architexa.org.eclipse.gef.editparts.AbstractGraphicalEditPart;


// Combined anonymous hide commands from package and class edit parts. 
// This is different from delete since the children remain showing. 

// EPs are needed inside this command because we need to make sure the layout for them happens correctly: See anchorChildren()

public class HideCommand extends Command {
        private ReloController rc = null;
        private ArtifactEditPart parentAEP = null;
		private CodeUnitEditPart myCUEP;
		private ArtifactFragment packageAF;
        
        
        public HideCommand(CodeUnitEditPart myCUEP) {
			super("Hide Package");
			this.myCUEP = myCUEP;
		}
        
		@Override
        public void execute() { 
            if (rc == null) rc = myCUEP.getRootController();
            parentAEP = hideAEP(myCUEP );
            if(myCUEP instanceof PackageEditPart){
            	  packageAF = myCUEP.getArtifact();
            }
        }
        @Override
        public void undo() {        	
           if (myCUEP instanceof PackageEditPart){
        		parentAEP.getArtifact().appendShownChild(packageAF);
            	ArtifactEditPart packageEP = rc.findArtifactEditPart(packageAF);
            	packageEP.assertParenthood();
            	myCUEP = (CodeUnitEditPart) packageEP;
            	
            	anchorChildren(packageEP);
            	 
           } else {
        	   // Hide Class Command
        	   // Not Used?
        	   addNode(myCUEP, parentAEP);
        	   myCUEP.assertParenthood();
           }
          
           
        }
        // Used to make sure layout doesnt happen for new EPs:
        // if we hide a package and then add methods to a class we dont want the classes to be layed out.
        // we must inform the layout engine that these EPs are 'old'
        private void anchorChildren(ArtifactEditPart packageEP) {
        	 for (EditPart aep : packageEP.getChildrenAsTypedList()) {
             	IFigure layoutFig = ((AbstractGraphicalEditPart) rc.getRoot().getContents()).getFigure();
      	        LayoutManager layoutFigLM = layoutFig.getLayoutManager();
      	        if (layoutFigLM != null) 
      	            if (layoutFigLM instanceof GraphLayoutManager && aep instanceof ArtifactEditPart) 
      	                ((GraphLayoutManager) layoutFigLM).anchorPart((ArtifactEditPart) aep);
             }
		}

		// Also Not Used?
        public void addNode(ArtifactEditPart nodeAEP, ArtifactEditPart parentAEP) {
            parentAEP.addModelAndChild(nodeAEP);
        }
        /**
         * hides given node, moves all children to parent, and returns the parent
         * @return my parent before hiding
         */
        public ArtifactEditPart hideAEP(ArtifactEditPart myCUEP) {
        	
            ArtifactFragment myCUAF = myCUEP.getArtifact();
            ArtifactEditPart myParent = (ArtifactEditPart) myCUEP.getParent();
            ReloController rc = myCUEP.getRootController();
            
            List<EditPart> myChildren = new ArrayList<EditPart>(myCUEP.getChildrenAsTypedList());
            for (EditPart childEP : myChildren) {
            	ArtifactFragment childAF = ((ArtifactEditPart) childEP).getArtifact();
            	ModelControllerManager.moveModelAndChild(childAF, myCUAF, myParent.getArtifact());
            	
            	// try to fix the childs position since it is now getting a new edit part
            	ArtifactEditPart childAFEP = rc.findArtifactEditPart(childAF);
            	PointPositionedDiagramPolicy.getLocToFig(childAF, (Figure) childAFEP.getFigure());
            	// ...and anchor part
        		IFigure layoutFig = ((AbstractGraphicalEditPart) childAFEP.getRoot().getContents()).getFigure();
                LayoutManager layoutFigLM = layoutFig.getLayoutManager();
                if (layoutFigLM != null && layoutFigLM instanceof GraphLayoutManager) {
                    ((GraphLayoutManager) layoutFigLM).anchorPart(childAFEP);
                }
                
                // deal with Derived AEP (and therefore recursive)
                if (childEP instanceof ArtifactEditPart
                        && childAF instanceof DerivedArtifact) {
                    //logger.info("hiding DerivedArtifact: " + ((ArtifactEditPart) childEP).getArtifact() + " " + childEP + " " + childEP.getClass());
                    hideAEP(((ArtifactEditPart) childEP));
                }
            }
            myCUEP.getModelChildren().clear();
            myCUEP.refresh();
            myCUAF.getParentArt().removeShownChild(myCUAF);
            
            anchorChildren(myCUEP);            
            return myParent;
        }
        
        
        
}
