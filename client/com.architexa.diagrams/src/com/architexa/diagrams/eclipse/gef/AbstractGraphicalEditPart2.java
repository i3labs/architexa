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
/*
 * Created on Jun 13, 2004
 *
 */
package com.architexa.diagrams.eclipse.gef;

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.architexa.diagrams.Activator;
import com.architexa.diagrams.model.ArtifactRel;
import com.architexa.diagrams.utils.DbgRes;
import com.architexa.org.eclipse.draw2d.Figure;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.LayoutManager;
import com.architexa.org.eclipse.gef.ConnectionEditPart;
import com.architexa.org.eclipse.gef.EditPart;
import com.architexa.org.eclipse.gef.EditPartListener;
import com.architexa.org.eclipse.gef.commands.Command;
import com.architexa.org.eclipse.gef.editparts.AbstractGraphicalEditPart;


/**
 * Place for functionality that:
 * 1] should have been in AbstractGraphicalEditPart;
 * 
 * @author vineet
 */
public abstract class AbstractGraphicalEditPart2 extends AbstractGraphicalEditPart {
    static final Logger logger = Activator.getLogger(AbstractGraphicalEditPart2.class);

	/*
	 * Debugging/logging support
	 */
	protected DbgRes tag = new DbgRes(AbstractGraphicalEditPart2.class, this);

	@Override
    public String toString() {
	    if (getModel() == null) {
			return "{null}" + tag.getTrailer();
	    } else {
			return getModel().toString() + tag.getTrailer();
	    }
	}
	
	protected void log(String str) {
        String dbgHdr = "[" 
            + getClass().toString().substring(getClass().toString().lastIndexOf('.') + 1) 
            + " / "
            + toString() + "] ";
        logger.info(dbgHdr + str);
    }

	protected void logBeg(String str) {
        String dbgHdr = "[" 
            + getClass().toString().substring(getClass().toString().lastIndexOf('.') + 1) 
            + " / "
            + toString() + "] ";
        logger.info(dbgHdr + str);
    }

	protected void logEnd() {
		logger.info("");
    }

    
    @Override
    protected void removeChild(EditPart child) {
        // because of the below method we should not be called for moves, only 
        // for deletes
        fireDeletingChild(child);

        super.removeChild(child);
        fireRemovedChild(child);
    }

    protected void removeChildWithoutDeleting(EditPart child) {
        super.removeChild(child);
        fireRemovedChild(child);
    }
    
    @Override
    protected void addChild(EditPart child, int index) {
    	if (child == null) {
    		logger.error("child == null");
    		return;
    	}
        if (child.getParent() != null && child.getParent() != this) {
            ((AbstractGraphicalEditPart2) child.getParent()).removeChildWithoutDeleting(child);
        }
        super.addChild(child, index);
    }
	
    /**
     * Notifies <code>EditPartListeners</code> that a
     * child is being removed.
     * @param child  <code>EditPart</code> being removed.
     * @param index  Position of the child in children list.
     */
    protected void fireRemovedChild(EditPart child) {
        Iterator<?> listeners = getEventListeners(EditPartListener.class);
        while (listeners.hasNext()) {
            Object epl = listeners.next();
            if (epl instanceof EditPartListener2) ((EditPartListener2)epl).removedChild(child);
        }
    }

    /**
     * Notifies <code>EditPartListeners</code> that a
     * child is being deleted. Note: that this method needs to be explicitly called otherwise the framework will not know that an editpart is being deleted or if it is being 
     * @param child  <code>EditPart</code> being removed.
     * @param index  Position of the child in children list.
     */
    protected void fireDeletingChild(EditPart child) {
        Iterator<?> listeners = getEventListeners(EditPartListener.class);
        while (listeners.hasNext()) {
            Object epl = listeners.next();
            if (epl instanceof EditPartListener2) ((EditPartListener2)epl).deletingChild(child);
        }
    }

    protected void addChild(EditPart child) {
		//System.err.println("AVEP: adding child: " + child + " [" + child.getClass() + "]");
		//System.err.println("AVEP:");
		addChild(child, -1);
		//System.err.println("AVUEP: added child");
	}
	
	
	
	@Override
    protected abstract void createEditPolicies();
	
	@Override
    protected abstract IFigure createFigure();

	
	//protected void addChild(EditPart child) {
	//	System.err.println("AVEP: adding child");
	//	addChild(child, -1);
	//	System.err.println("AVEP: added child");
	//}

	@Override
    protected void setFigure(IFigure newFig) {
		if (figure == null) {
			figure = newFig;
			return;
		}
		if (figure == newFig) {
			return;
		}

		Figure parent = (Figure) figure.getParent();
		if (parent != null) {
			unregisterVisuals();
			int index = parent.getChildren().indexOf(figure);
			parent.remove(figure);

			LayoutManager parentLM = ((AbstractGraphicalEditPart) getParent())
					.getContentPane().getLayoutManager();
			if (parentLM!=null) {
				Object constraint = parentLM.getConstraint(figure);
				parentLM.setConstraint(newFig, constraint);
			}
			
			figure = newFig;
			parent.add(figure, index);
			figure.setParent(parent);
			registerVisuals();
		}
		
        for (EditPart aep : getSourceConnectionsAsEP()) {
            aep.refresh();
        }
        for (EditPart aep : getTargetConnectionsAsEP()) {
            aep.refresh();
        }

		List<?> epList = getChildrenAsTypedList();
		for (int i=0; i<epList.size(); i++) {
			EditPart childEP = (EditPart) epList.get(i);
			addChildVisual(childEP, i);
		}
	}
	
	public AbstractGraphicalEditPart findEditPart(Object model) {
		if (model instanceof ArtifactRel) {
			for (Object agep : getViewer().getEditPartRegistry().keySet()) {
				if (agep instanceof ArtifactRel) {
					ArtifactRel agepRel = (ArtifactRel) agep;
					ArtifactRel modelRel = (ArtifactRel) model;
					if(modelRel.getDest().equals(agepRel.getDest()) && modelRel.getSrc().equals(agepRel.getSrc()))
						return (AbstractGraphicalEditPart) getViewer().getEditPartRegistry().get(agep);
				}
		    }
		}
		return (AbstractGraphicalEditPart) getViewer().getEditPartRegistry().get(model);
	}
	protected AbstractGraphicalEditPart createOrFindEditPart(Object model) {
	    AbstractGraphicalEditPart modelEP = findEditPart(model);
        if (modelEP == null) {
            // create and add
            modelEP = (AbstractGraphicalEditPart) getViewer().getEditPartFactory().createEditPart(this, model);
            addChild(modelEP, -1);
        }
        return modelEP;
	}

	public ConnectionEditPart findConnection(Object model) {
	    return (ConnectionEditPart) getViewer().getEditPartRegistry().get(model);
	}

	public void execute(Command cmd) {
		getViewer().getEditDomain().getCommandStack().execute(cmd);
	}
	
	
    @SuppressWarnings("unchecked")
    public List<EditPart> getChildrenAsTypedList() {
        return getChildren();
    }
	
    @SuppressWarnings("unchecked")
    private static List<ConnectionEditPart> asListConnectionEditPart(List connections) {
        return connections;
    }
    public List<ConnectionEditPart> getSourceConnectionsAsEP() {
        return asListConnectionEditPart(getSourceConnections());
    }
    public List<ConnectionEditPart> getTargetConnectionsAsEP() {
        return asListConnectionEditPart(getTargetConnections());
    }

}
