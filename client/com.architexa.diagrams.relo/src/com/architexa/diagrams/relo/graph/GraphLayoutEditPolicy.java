/* 
 * Copyright (c) 2004-2005 Massachusetts Institute of Technology. This code was
 * developed as part of the Haystack (http://haystack.lcs.mit.edu/) research 
 * project at MIT. Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation files (the 
 * "Software"), to deal in the Software without restriction, including without 
 * limitation the rights to use, copy, modify, merge, publish, distribute, 
 * sublicense, and/or sell copies of the Software, and to permit persons to whom
 * the Software is furnished to do so, subject to the following conditions: 
 * 
 * The above copyright notice and this permission notice shall be included in 
 * all copies or substantial portions of the Software. 
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER  
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, 
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE. 
 */

package com.architexa.diagrams.relo.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


import com.architexa.diagrams.relo.commands.MoveCommand;
import com.architexa.diagrams.relo.parts.ReloArtifactRelEditPart;
import com.architexa.org.eclipse.draw2d.geometry.Rectangle;
import com.architexa.org.eclipse.gef.EditPart;
import com.architexa.org.eclipse.gef.EditPolicy;
import com.architexa.org.eclipse.gef.Request;
import com.architexa.org.eclipse.gef.commands.Command;
import com.architexa.org.eclipse.gef.commands.CompoundCommand;
import com.architexa.org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import com.architexa.org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
import com.architexa.org.eclipse.gef.editparts.ZoomManager;
import com.architexa.org.eclipse.gef.editpolicies.LayoutEditPolicy;
import com.architexa.org.eclipse.gef.editpolicies.NonResizableEditPolicy;
import com.architexa.org.eclipse.gef.requests.ChangeBoundsRequest;
import com.architexa.org.eclipse.gef.requests.CreateRequest;




public class GraphLayoutEditPolicy extends LayoutEditPolicy {

    @Override
    protected EditPolicy createChildEditPolicy(EditPart child) {
        return new NonResizableEditPolicy();
    }
    
    @Override
    public Command getCommand(Request request) {
    	if (REQ_RESIZE_CHILDREN.equals(request.getType()))
    		return getMoveChildrenCommand(request);
    	
        return super.getCommand(request);
    }
    @Override
    protected Command getCreateCommand(CreateRequest request) {
        return null;
    }
    
    @Override
    protected Command getDeleteDependantCommand(Request request) {
        return null;
        //System.err.println("x");
        //return new Command() {};
    }
    @Override
    protected Command getOrphanChildrenCommand(Request request) {
        return getMoveChildrenCommand(request);
    }
    
    @Override
    protected Command getMoveChildrenCommand(Request request) {
    	if(!(request instanceof ChangeBoundsRequest)) return null;
        ChangeBoundsRequest req = (ChangeBoundsRequest) request;
        List<AbstractGraphicalEditPart> moveEP = getAGEPTypedList(req.getEditParts());
        List<AbstractGraphicalEditPart> moveEPCopy = new ArrayList<AbstractGraphicalEditPart>(moveEP);
        for(AbstractGraphicalEditPart ep : moveEPCopy) {
        	// Class figs are in a different layer than package figs (they aren't children 
        	// of package figures), so when a package is moved the classes won't be moved 
        	// with it. Since packages are layed out to surround their classes, if want to
        	// move a package edit part, need to move its children edit parts (the classes)

        	// Make sure to move children of classes too since we need to move any attached comments
        	addAllChildren(moveEP, ep);
        }

        // we store origBounds because it might be needed for undoing
        Map<AbstractGraphicalEditPart, Rectangle> origBounds = new HashMap<AbstractGraphicalEditPart, Rectangle>(moveEP.size());
        for (AbstractGraphicalEditPart ep : moveEP) {
            origBounds.put(ep, ep.getFigure().getBounds().getCopy());
        }
        ZoomManager zoomManager = ((ScalableFreeformRootEditPart) getHost().getRoot()).getZoomManager();
        CompoundCommand cmd = new CompoundCommand("Move Cmd");
        cmd.add(new MoveCommand(moveEP, origBounds, req.getMoveDelta().scale(1/zoomManager.getZoom())));
        Set<ReloArtifactRelEditPart> relSet = new HashSet<ReloArtifactRelEditPart>();
        for(AbstractGraphicalEditPart ep: moveEP) {
        	for(Object conn: ep.getSourceConnections()) {
        		if (conn instanceof ReloArtifactRelEditPart) {
        			EditPart target = ((ReloArtifactRelEditPart)conn).getTarget();
        			if (moveEP.contains(target)) { // moving both
        				relSet.add((ReloArtifactRelEditPart) conn);
        				continue;
        			}
        			((ReloArtifactRelEditPart)conn).addBendPointRemoveCmd(cmd, req.getMoveDelta(), true);
        		}
        	}

        	for(Object conn: ep.getTargetConnections()) {
        		if (conn instanceof ReloArtifactRelEditPart) {
        			EditPart src = ((ReloArtifactRelEditPart)conn).getSource();
        			if (moveEP.contains(src)) { // moving both
        				relSet.add((ReloArtifactRelEditPart) conn);
        				continue;
        			}
        			((ReloArtifactRelEditPart)conn).addBendPointRemoveCmd(cmd, req.getMoveDelta(), false);
        		}	
        	}
        }
        
        for (ReloArtifactRelEditPart rel : relSet) {
        	if (rel.getSource().getParent().equals(rel.getTarget().getParent())) continue;
        		rel.addMoveAllBendpointCmd(cmd, req.getMoveDelta());
        }
        
        return cmd;
    }	

    private void addAllChildren(List<AbstractGraphicalEditPart> moveEP, AbstractGraphicalEditPart ep) {
    	List<AbstractGraphicalEditPart> children = getAGEPTypedList(ep.getChildren());
    	moveEP.addAll(children);
    	for (AbstractGraphicalEditPart child : children) {
    		addAllChildren(moveEP, child);
    	}
	}

	@SuppressWarnings("unchecked")
    private final List<AbstractGraphicalEditPart> getAGEPTypedList(List in) {
        return in;
    };

}