/**
 * 
 */
package com.architexa.diagrams.relo.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.parts.AbstractRelationPart;
import com.architexa.diagrams.parts.CommentEditPart;
import com.architexa.diagrams.parts.NamedRelationPart;
import com.architexa.diagrams.parts.PointPositionedDiagramPolicy;
import com.architexa.diagrams.relo.graph.GraphLayoutManager;
import com.architexa.diagrams.relo.parts.ArtifactEditPart;
import com.architexa.diagrams.relo.parts.ReloArtifactRelEditPart;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.LayoutManager;
import com.architexa.org.eclipse.draw2d.geometry.Point;
import com.architexa.org.eclipse.draw2d.geometry.Rectangle;
import com.architexa.org.eclipse.gef.EditPart;
import com.architexa.org.eclipse.gef.commands.Command;
import com.architexa.org.eclipse.gef.editparts.AbstractConnectionEditPart;
import com.architexa.org.eclipse.gef.editparts.AbstractGraphicalEditPart;

public class MoveCommand extends Command {
    private final List<AbstractGraphicalEditPart> moveEP;
    private final Map<AbstractGraphicalEditPart, Rectangle> origBounds;
    private final Point moveDelta;
    
	// Flag to indicate whether connections should "anchor" their source and target, 
	// ie force the target to move when the source is moved and vice versa.
	// Currently true, so only rels with a comment for a src or target are anchored
	private static final boolean disableConnectedMoving = true;

    public MoveCommand(List<AbstractGraphicalEditPart> moveEP, Map<AbstractGraphicalEditPart, Rectangle> origBounds, Point moveDelta) {
        super("Move Nodes");
        this.moveEP = moveEP;
        this.origBounds = origBounds;
        this.moveDelta = moveDelta;
    }
    @Override
    public void execute() {
        //performUndoableLayout();
        List<AbstractGraphicalEditPart> movedEPs = new ArrayList<AbstractGraphicalEditPart>();
    	for (AbstractGraphicalEditPart ep : moveEP) {
        	moveConnectedEPs(ep, movedEPs, moveDelta);
        	moveEP(ep, origBounds.get(ep), moveDelta);
        }
    }
    
	@Override
    public void undo() {
        //performUndoableLayout();
		List<AbstractGraphicalEditPart> movedEPs = new ArrayList<AbstractGraphicalEditPart>();
        Point undoMoveDelta = moveDelta.getNegated();
        for (AbstractGraphicalEditPart ep : moveEP) {
            Rectangle currentBounds = origBounds.get(ep).getCopy().translate(moveDelta);
        	moveConnectedEPs(ep, movedEPs, undoMoveDelta);
            moveEP(ep, currentBounds, undoMoveDelta);
        }
    }

    private void moveEP(AbstractGraphicalEditPart ep, Rectangle startBounds, final Point moveDelta) {
        Point oldLoc = startBounds.getCenter();
        
        Rectangle endBounds = startBounds.getCopy();
        endBounds.translate(moveDelta);
        snapToConnections(ep, endBounds);

        Object mdl = ep.getModel();
        if (mdl instanceof ArtifactFragment) {
        	PointPositionedDiagramPolicy.setLoc((ArtifactFragment)mdl, endBounds.getTopLeft());
        }
        
        // @tag review-below-code-to-delete
        if (true) return;
        
        Point newLoc = endBounds.getCenter();

        ep.getFigure().setBounds(endBounds);
        //System.err.println("moving from: " + startBounds + " to: " + endBounds);
        
        
        // TODO: is this really necessary?
        // moved: now run the layout manager

        // isn't the below simpler?
		//IFigure layoutFig = ep.getFigure().getParent();
		//LayoutManager layoutFigLM = layoutFig.getLayoutManager();
		//if (layoutFigLM instanceof GraphLayoutManager.SubgraphLayout) {
		//    layoutFig = ((AbstractGraphicalEditPart) ep.getRoot().getContents()).getFigure();
		//    layoutFigLM = layoutFig.getLayoutManager();
		//} 
		IFigure layoutFig = ((AbstractGraphicalEditPart) ep.getRoot().getContents()).getFigure();
        LayoutManager layoutFigLM = layoutFig.getLayoutManager();
        if (layoutFigLM != null) {
            if (layoutFigLM instanceof GraphLayoutManager) {
                ((GraphLayoutManager) layoutFigLM).anchorPart((ArtifactEditPart) ep, newLoc, oldLoc);
            }
            layoutFigLM.layout(layoutFig);
        }
    }

	/**
	 * When moving a class that has an inheritence relationship, check if moving
	 * within 15px of being vertically centered with the element at the opposite
	 * end of the connection. 
	 * 
	 */
	private void snapToConnections(AbstractGraphicalEditPart ep, Rectangle endBounds) {
		List<AbstractConnectionEditPart> conns = ep.getTargetConnections();
        for (AbstractConnectionEditPart conn : conns) {
        	if (!(conn instanceof ReloArtifactRelEditPart)) continue;
    		int srcX = ((AbstractGraphicalEditPart) conn.getSource()).getFigure().getBounds().getTop().x;
    		int tgtX = endBounds.getTop().x;
    		if (Math.abs(srcX-tgtX) < 15 )
    			endBounds.x = srcX-endBounds.width/2;
        }
        conns = ep.getSourceConnections();
        for (AbstractConnectionEditPart conn : conns) {
        	if (!(conn instanceof ReloArtifactRelEditPart)) continue;
    		int tgtX = ((AbstractGraphicalEditPart) conn.getTarget()).getFigure().getBounds().getTop().x;
    		int srcX = endBounds.getTop().x;
    		if (Math.abs(srcX-tgtX) < 15 )
    			endBounds.x = tgtX-endBounds.width/2;
        }
	}
	
	private void moveConnectedEPs(AbstractGraphicalEditPart ep, List<AbstractGraphicalEditPart> movedEPs, Point delta) {
    	// move anchored ArtFrags jointly with this moved AF
		List<AbstractRelationPart> srcConns = ep.getTargetConnections(); // src EPs this is the tgt of
		for (AbstractRelationPart conn : srcConns)
			movedConnectedEP(conn.getSource(), conn, movedEPs, delta);
		List<AbstractRelationPart> tgtConns = ep.getSourceConnections();  // tgt EPs this is the src of
		for (AbstractRelationPart conn : tgtConns)
			movedConnectedEP(conn.getTarget(), conn, movedEPs, delta);
	}

	private void movedConnectedEP(EditPart connectedEP, AbstractRelationPart conn, 
			List<AbstractGraphicalEditPart> movedEPs, Point delta) {
		if (disableConnectedMoving && !(connectedEP instanceof CommentEditPart)) 
			return; // only comments anchored
		if (conn instanceof NamedRelationPart && !movedEPs.contains(connectedEP)) {
			moveEP((AbstractGraphicalEditPart) connectedEP, ((AbstractGraphicalEditPart) connectedEP).getFigure().getBounds().getCopy(), delta);
			movedEPs.add((AbstractGraphicalEditPart) connectedEP);
		}
	}
}