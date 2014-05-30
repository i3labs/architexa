/**
 * Copyright (c) 2007-2008 Architexa, Inc
 */ 
package com.architexa.diagrams.strata.parts;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.apache.log4j.Logger;

import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.strata.StrataPlugin;
import com.architexa.diagrams.strata.figures.ContainerFigure;
import com.architexa.diagrams.strata.model.DependencyRelation;
import com.architexa.diagrams.strata.model.policy.ClosedContainerDPolicy;
import com.architexa.diagrams.strata.ui.ColorScheme;
import com.architexa.diagrams.utils.DbgRes;
import com.architexa.org.eclipse.draw2d.FigureListener;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.MouseEvent;
import com.architexa.org.eclipse.draw2d.MouseMotionListener;
import com.architexa.org.eclipse.gef.EditPart;
import com.architexa.org.eclipse.gef.EditPartListener;
import com.architexa.org.eclipse.gef.editparts.AbstractGraphicalEditPart;

public class DirectedConnectionVisibilityPolicy extends AbstractConnectionEditPolicy {
    public static final Logger logger = StrataPlugin.getLogger(DirectedConnectionVisibilityPolicy.class);
	
	public DirectedConnectionVisibilityPolicy() {}

	/*
	 * Debugging/logging support
	 */
	protected DbgRes tag = new DbgRes(DirectedConnectionVisibilityPolicy.class, this, "CDVP");
	
	@Override
    public String toString() {
		return ">" + getHost().toString() + "<" + tag;
	}

	
	public StrataRootEditPart getContentEP() {
		return (StrataRootEditPart) this.getHost().getRoot().getContents();
	}

	public StrataRootEditPart getRootController() {
	    return (StrataRootEditPart) this.getHost().getRoot().getContents();
	}
    
    protected DependencyRelation getDepRel() {
        return (DependencyRelation) getConnEP().getModel();
    }

    // for singly-directed relationships returns the source of the relation
    // (which can be different from srcFig)
    private IFigure getBegFig() {
        if (getDepRel().depCnt != 0)
            return getSrcFig();
        else
            return getTgtFig();
    }
    private IFigure getEndFig() {
        if (getDepRel().depCnt != 0)
            return getTgtFig();
        else
            return getSrcFig();
    }
	
    public boolean isArrowPointedUp() {
        if (getDepRel().depCnt != 0 && getDepRel().revDepCnt != 0) return true;

        if (getBegFig() == null || getEndFig() == null) return false;
        
		// is top of source lower than the bottom of dest
		if (getBegFig().getBounds().y > getEndFig().getBounds().bottom())
			return true;
		else
			return false;
	}
	public boolean isArrowPointedDn() {
        if (getDepRel().depCnt != 0 && getDepRel().revDepCnt != 0) return true;

        if (getBegFig() == null || getEndFig() == null) return false;
        
		// is bottom of source higher than the top of dest
		if (getBegFig().getBounds().bottom() < getEndFig().getBounds().y)
			return true;
		else
			return false;
	}
    
    private Object getPropVal(String propStr) {
    	try {
    		return getContentEP().getPropertyValue(propStr);
    	} catch (NullPointerException e) {
    		return null;
    	}
    }
	
	public boolean arrState_showOnHover() {
        // we do !down for up, because this allows us to get the flat ones as well
		if (!isArrowPointedDn()) {
			return (StrataRootEditPart.ArrState_ShowOnHover_Ndx.equals(getPropVal(StrataRootEditPart.UP_ARR_PROP)));
		}
		if (!isArrowPointedUp()) {
			return (StrataRootEditPart.ArrState_ShowOnHover_Ndx.equals(getPropVal(StrataRootEditPart.DN_ARR_PROP)));
		}
        // not down and not up, i.e. flat, then show on hover if both are show on hover
        return (StrataRootEditPart.ArrState_ShowOnHover_Ndx.equals(getPropVal(StrataRootEditPart.UP_ARR_PROP))
                 && StrataRootEditPart.ArrState_ShowOnHover_Ndx.equals(getPropVal(StrataRootEditPart.DN_ARR_PROP)));
	}
	
	public boolean arrState_dontShow() {
		if (!isArrowPointedDn()) {
			return StrataRootEditPart.ArrState_DontShow_Ndx.equals(getPropVal(StrataRootEditPart.UP_ARR_PROP));
		}
		if (!isArrowPointedUp()) {
			return StrataRootEditPart.ArrState_DontShow_Ndx.equals(getPropVal(StrataRootEditPart.DN_ARR_PROP));
		}
		// if neither then show always
		return false;
	}
	
	public boolean arrState_showAlways() {
		if (isArrowPointedUp()) {
			return StrataRootEditPart.ArrState_ShowAlways_Ndx.equals(getPropVal(StrataRootEditPart.UP_ARR_PROP));
		}
		if (isArrowPointedDn()) {
			return StrataRootEditPart.ArrState_ShowAlways_Ndx.equals(getPropVal(StrataRootEditPart.DN_ARR_PROP));
		}
		// if neither then show always
		return true;
	}

	public boolean aNodeSelected() {
		if (this.getSrcEP() != null && !(this.getSrcEP() instanceof StrataRootEditPart) && this.getSrcEP().getSelected() != EditPart.SELECTED_NONE)
			return true;
		
		if (this.getTgtEP() != null && !(this.getTgtEP() instanceof StrataRootEditPart) && this.getTgtEP().getSelected() != EditPart.SELECTED_NONE)
			return true;

		return false;
	}

	
	private boolean aNodeParentSelected() {
		TitledArtifactEditPart srcParent = getSrcEPParent();
		if (srcParent != null && !(srcParent instanceof StrataRootEditPart) && srcParent.getSelected() != EditPart.SELECTED_NONE)
			return true;

		TitledArtifactEditPart tgtParent = getTgtEPParent();
		if (tgtParent != null && !(tgtParent instanceof StrataRootEditPart) && tgtParent.getSelected() != EditPart.SELECTED_NONE)
			return true;
		
		return false;
	}
	
	public void hideConn(AbstractGraphicalEditPart hoverPart) {
		if(getPinned()) return;
		if (getHost().getSelected() != EditPart.SELECTED_NONE) return;
		getConnFig().setVisible(false);
		getHost().setSelected(EditPart.SELECTED_NONE);
        if (hoverPart == null) return;
        highlight(hoverPart.getFigure(), false, getBegFig(), true);
        highlight(hoverPart.getFigure(), false, getEndFig(), false);
        
        //System.err.println("Hiding: " + this.toString());
	}

    public void showConn(AbstractGraphicalEditPart hoverPart) {
		getConnFig().setVisible(true);

        if (hoverPart == null) return;
        highlight(hoverPart.getFigure(), true, getBegFig(), true);
        highlight(hoverPart.getFigure(), true, getEndFig(), false);
        
        //System.err.println("Showing: " + this.toString());
	}

    private void highlight(IFigure hoverFig, boolean highlight, IFigure highlightFig, boolean figAtBeg) {
    	if (!(highlightFig instanceof ContainerFigure)) return;

    	if (((ContainerFigure)highlightFig).getBackgroundColor().equals(ColorScheme.highlightBackground))
    		return;
        ContainerFigure cFig = (ContainerFigure)highlightFig;
        if (!highlight) {
            cFig.highlight(false);
            return;
        }
        
        // highlight = true
        if (getDepRel().biDirectional() || hoverFig == highlightFig) {
            cFig.highlight();
            return;
        }

        // highlight = true, hoverFig != highlightFig, single directional
        //boolean upPointing = isArrowPointedUp();
        //logger.info("highlight: " + cFig.getName() + " figAtSrc: " + figAtSrc);
        if (figAtBeg) 
            cFig.highlight(-1);
        else 
            cFig.highlight(1);
    }

    private PropertyChangeListener connPCL  = new PropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent evt) {
			updateArrows(null);
		}

	};
		
	public void updateArrows(AbstractGraphicalEditPart hoverPart) {
		if (!this.isIniitalized()) return;
		
		// always hover on, because some edges are always shown
		hoverOn(hoverPart);
		
		// even though it might not have exited, if the user moves in and out then this will get fixed
		hoverOf(hoverPart);
	}
	
	private void hoverOn(AbstractGraphicalEditPart hoverPart) {
		try {
			if (validEdge() &&
					(arrState_showOnHover() || arrState_showAlways())) 
				showConn(hoverPart);
		} catch (Throwable t) {
			logger.error("Exception when entering for: " + getConnEP(), t);
		}
	}
	
	private boolean validEdge() {
		Object srcEP = null;
		if (getSrcEP() == null) return false;
		srcEP = getSrcEP().getModel();
		if (!(srcEP instanceof ArtifactFragment)) return false;
		if (ClosedContainerDPolicy.isShowingChildren((ArtifactFragment) srcEP)) return false;

		if (getTgtEP() == null)	return false;
		Object tgtEP = getTgtEP().getModel();
		if (!(tgtEP instanceof ArtifactFragment)) return false;
		if (ClosedContainerDPolicy.isShowingChildren((ArtifactFragment)  tgtEP)) return false;
				
		// We dont want to show connections between children inside a selected parent
		if (isSibling((ArtifactFragment) tgtEP, (ArtifactFragment) srcEP)) return false;
		
		return true;
	}

	private boolean isSibling(ArtifactFragment tgtAF, ArtifactFragment srcAF) {
		if (getTgtEPParent()!= null && getTgtEPParent().getSelected()==EditPart.SELECTED_NONE) return false;
		if (getSrcEPParent()!= null && getSrcEPParent().getSelected()==EditPart.SELECTED_NONE) return false;
		
		// children of the root are not siblings
		if (tgtAF.getParentArt() == tgtAF.getRootArt() && srcAF.getParentArt() == srcAF.getRootArt()) return false;
		if (tgtAF.getParentArt() == srcAF.getParentArt()) return true;
		
		return false;
	}

	private void hoverOf(AbstractGraphicalEditPart hoverPart) {
		try {
			if (!validEdge()) hideConn(hoverPart);
			if (aNodeSelected() || aNodeParentSelected()) return; // only hide if a node is not selected
			if (arrState_showOnHover() || arrState_dontShow()) hideConn(hoverPart);
		} catch (Throwable t) {
			logger.error("Exception when exiting for: " + getConnEP(), t);
		}

	}
	
	private MouseMotionListener mml = new MouseMotionListener.Stub() {
		@Override
		public void mouseEntered(MouseEvent me) {
            IFigure evtFig = (IFigure) me.getSource();
            if (evtFig == getSrcFig()) hoverOn(getSrcEP());
            if (evtFig == getTgtFig()) hoverOn(getTgtEP());
            if (evtFig == ((AbstractGraphicalEditPart) getHost()).getFigure()) {
            	hoverOn(getSrcEP());
            	hoverOn(getTgtEP());
            }
		}
		@Override
		public void mouseExited(MouseEvent me) {
            IFigure evtFig = (IFigure) me.getSource();
            if (evtFig == getSrcFig()) hoverOf(getSrcEP());
            if (evtFig == getTgtFig()) hoverOf(getTgtEP());
            if (evtFig == ((AbstractGraphicalEditPart) getHost()).getFigure()) {
            	hoverOf(getSrcEP());
            	hoverOf(getTgtEP());
            }
		}
	};
	private FigureListener srcTgtFigListener = new FigureListener() {
		public void figureMoved(IFigure source) {
			updateArrows(null);
		}};
        
    private final class ArrowUpdaterEPL extends EditPartListener.Stub {
        @Override
        public void selectedStateChanged(EditPart part) {
            updateArrows((AbstractGraphicalEditPart) part);
        }
    }
    private EditPartListener epl = new ArrowUpdaterEPL();
		
	@Override
	public void init() {
		super.init();
		this.getRootController().addPropertyChangeListener(connPCL);
		initSrc();
		initTgt();
		((AbstractGraphicalEditPart) getHost()).getFigure().addMouseMotionListener(mml);
//		((AbstractGraphicalEditPart) getHost()).getFigure().addFigureListener(srcTgtFigListener);
//		getHost().addEditPartListener(epl);
		updateArrows(null);
	}
	
	@Override
	public void clean() {
		// check for null here, if model gets in a strange state we dont want to throw errors
		if (this.getHost().getParent() == null) return;
		this.getRootController().removePropertyChangeListener(connPCL);
		cleanSrc();
		cleanTgt();
		((AbstractGraphicalEditPart) getHost()).getFigure().removeMouseMotionListener(mml);
		super.clean();
	}

	@Override
	public void setSource(EditPart ep) {
		cleanSrc();
		super.setSource(ep);
		initSrc();
	}


	@Override
	public void setTarget(EditPart ep) {
		cleanTgt();
		super.setTarget(ep);
		initTgt();
	}
	
	private void initSrc() {
		if (getSrcEP() == null) return;
		getSrcFig().addMouseMotionListener(mml);
		getSrcFig().addFigureListener(srcTgtFigListener);
		getSrcEP().addEditPartListener(epl);
		if (getSrcEPParent() != null)
			getSrcEPParent().addEditPartListener(epl);
	}
	private void initTgt() {
		if (getTgtEP() == null) return;
		getTgtFig().addMouseMotionListener(mml);
		getTgtFig().addFigureListener(srcTgtFigListener);
		getTgtEP().addEditPartListener(epl);
		if (getTgtEPParent() != null)
			getTgtEPParent().addEditPartListener(epl);
	}

	private TitledArtifactEditPart getSrcEPParent() {
		if (this.getSrcEP() == null || !(this.getSrcEP() instanceof StrataArtFragEditPart)) return null;
		return ((StrataArtFragEditPart)this.getSrcEP()).getParentTAFEP();
	}
	
	private TitledArtifactEditPart getTgtEPParent() {
		if (this.getTgtEP() == null || !(this.getTgtEP() instanceof StrataArtFragEditPart)) return null;
		return ((StrataArtFragEditPart)this.getTgtEP()).getParentTAFEP();
	}

	private void cleanSrc() {
		if (getSrcEP() == null) return;
		getSrcFig().removeMouseMotionListener(mml);
		getSrcFig().removeFigureListener(srcTgtFigListener);
		getSrcEP().removeEditPartListener(epl);
		if (getSrcEPParent() != null)
			getSrcEPParent().removeEditPartListener(epl);
	}

	private void cleanTgt() {
		if (getTgtEP() == null) return;
		getTgtFig().removeMouseMotionListener(mml);
		getTgtFig().removeFigureListener(srcTgtFigListener);
		getTgtEP().removeEditPartListener(epl);
		if (getTgtEPParent() != null)
			getTgtEPParent().removeEditPartListener(epl);
	}

	public boolean getPinned() {
		return getDepRel().pinned;
	}
	public void setPinned(boolean pin) {
		getDepRel().pinned = pin;
	}

}
