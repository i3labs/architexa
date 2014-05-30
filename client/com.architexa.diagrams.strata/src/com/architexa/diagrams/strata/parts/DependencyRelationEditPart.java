/**
 * Copyright (c) 2007-2008 Architexa, Inc
 */ 
package com.architexa.diagrams.strata.parts;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.IMenuManager;
import org.openrdf.model.URI;

import com.architexa.diagrams.eclipse.gef.MenuButton;
import com.architexa.diagrams.parts.NamedRelationPart;
import com.architexa.diagrams.parts.NavAidsRelEditPart;
import com.architexa.diagrams.relo.parts.AbstractReloRelationPart;
import com.architexa.diagrams.strata.StrataPlugin;
import com.architexa.diagrams.strata.figures.BrokenPolylineConnection;
import com.architexa.diagrams.strata.model.DependencyRelation;
import com.architexa.diagrams.strata.ui.ReLayerAction;
import com.architexa.diagrams.ui.ImageCache;
import com.architexa.diagrams.utils.DbgRes;
import com.architexa.org.eclipse.draw2d.ColorConstants;
import com.architexa.org.eclipse.draw2d.Graphics;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.Label;
import com.architexa.org.eclipse.draw2d.MidpointLocator;
import com.architexa.org.eclipse.draw2d.PolygonDecoration;
import com.architexa.org.eclipse.draw2d.PositionConstants;
import com.architexa.org.eclipse.draw2d.Shape;
import com.architexa.org.eclipse.draw2d.Triangle2;
import com.architexa.org.eclipse.draw2d.geometry.Dimension;
import com.architexa.org.eclipse.draw2d.geometry.Point;
import com.architexa.org.eclipse.draw2d.geometry.Rectangle;
import com.architexa.org.eclipse.gef.EditPart;
import com.architexa.store.ReloRdfRepository;



public class DependencyRelationEditPart extends AbstractConnectionEditPart2 implements NavAidsRelEditPart {
	public static final Logger logger = StrataPlugin.getLogger(DependencyRelationEditPart.class);
	private MenuButton pinBtn;
	static Dimension iconDim = new Dimension(24,24);
	
	private int currWidth;

	public DependencyRelationEditPart() {}
	
	public DependencyRelation getDepRel() {
		return (DependencyRelation) getModel();
	}
	
	public StrataRootEditPart getContentEP() {
		return (StrataRootEditPart) this.getRoot().getContents();
	}

	public StrataRootEditPart getRootController() {
	    return (StrataRootEditPart) this.getRoot().getContents();
	}

	public ReloRdfRepository getRepo() {
		return getContentEP().getRepo();
	}
	
	public static final String VisibilityRole = "Visibility_Role"; 

    @Override
    protected void createEditPolicies() {
    	installEditPolicy(VisibilityRole, new DirectedConnectionVisibilityPolicy());
    }
	
	
	@Override
    protected IFigure createFigure() {
        //BrokenPolylineConnection conn = new BrokenPolylineConnection(21, true);
	    BrokenPolylineConnection conn = new BrokenPolylineConnection(21, false);
        conn.setToolTip(new Label( "" + getDepRel().getLabel(getRepo()) ));
        
        if (getDepRel().depCnt > 0) {
            conn.setTargetDecorations(getSizedArrow(getDepRel().depCnt), getSizedArrow(getDepRel().depCnt));
        }

        if (getDepRel().revDepCnt > 0) {
            conn.setSourceDecorations(getSizedArrow(getDepRel().revDepCnt), getSizedArrow(getDepRel().revDepCnt));
        }

        // 1 -> 1
        // DepCache.maxDep -> 10
        //double ratio = 1;
        double ratio = (1.0 * (getDepRel().depCnt + getDepRel().revDepCnt)) / (2 * getContentEP().getRootModel().maxDep);
        if (ratio > 1.0) {
            logger.info("Unexpectedly high ratio (adjusting): " 
                    + ratio + " " 
                    + getDepRel().depCnt + "+" 
                    + getDepRel().revDepCnt + "/" 
                    + getContentEP().getRootModel().maxDep);
            ratio = 1.0;
            ReLayerAction.adjust(this);
        }
        currWidth = (int) (ratio * 19 + 1);
        conn.setLineWidth(currWidth);
        conn.setForegroundColor(ColorConstants.darkGray);
        //conn.setForegroundColor(ColorConstants.lightGray);
        
        return conn;
	}
	
	@Override
	public void activate() {
		super.activate();
		if (pinBtn==null)
			getFigure().add(createPinBtn(), new MidpointLocator(getConnectionFigure(), 0));
	}
	
	
    private MenuButton createPinBtn() {
        MenuButton pinBtn = new MenuButton(new Label(ImageCache.getImage("push_pin.png")), getViewer()) {
			@Override
			public void buildMenu(IMenuManager menu) {
	            switchPinnedState(this, getDepRel().pinned);
			}
		};
		
        pinBtn.setBounds(new Rectangle(new Point(0,0),iconDim));
        pinBtn.setVisible(false);
        this.pinBtn = pinBtn;
        if (getDepRel().pinned)
        	pin(pinBtn);
        return pinBtn;
	}

	private PolygonDecoration getSizedArrow(int depCnt) {
        double ratio = (1.0 * depCnt) / getContentEP().getRootModel().maxDep;
//        logger.info("Using ratio: " + ratio);
        if (ratio > 1.0) {
        	logger.info("Unexpectedly high ratio (adjusting): " + ratio + " " + depCnt + "/" + getContentEP().getRootModel().maxDep);
        	ratio = 1.0;
            ReLayerAction.adjust(this);
        }

        Dimension arrowSize = NamedRelationPart.defaultArrowSize.getCopy();
        arrowSize.scale(ratio*1 + 1);
        return NamedRelationPart.getSizedArrow(arrowSize);
    }

	protected DbgRes tag = new DbgRes(DependencyRelationEditPart.class, this, "DREP");
	@Override
    public String toString() {
		return "" + this.getDepRel() + tag;
	}
    @Override
    public void setSelected(int value) {
		super.setSelected(value);
		Shape relation = (Shape) getFigure();
		if (pinBtn==null)
        	activate();
		if (value != EditPart.SELECTED_NONE) {
            relation.setLineWidth(currWidth + 2);
            relation.setVisible(true);
            pinBtn.setEnabled(true);
            pinBtn.setVisible(true);
		} else {
            if (!getDepRel().pinned && currWidth >2) 
            	relation.setLineWidth(currWidth - 2);
            pinBtn.setVisible(false);
            pinBtn.setEnabled(false);
		}
	}

	public IFigure getArrow() {
        Triangle2 arrowHead = new Triangle2();
        arrowHead.setFill(true);
        arrowHead.setClosed(true);
        return AbstractReloRelationPart.getArrow(PositionConstants.SOUTH, arrowHead);
    }

    public String getRelationLabel(ReloRdfRepository repo, URI res) {
        return "depends";
    }

    
    public void switchPinnedState(MenuButton menuButton, boolean pinned) {
		if (pinned) 
			unpin(menuButton);
		else
			pin(menuButton);
	}


	private void unpin(MenuButton menuButton) {
		Shape relation = (Shape) getFigure();
		relation.setVisible(false);
        relation.setLineStyle(Graphics.LINE_SOLID);
		pinBtn.setVisible(false);
        getDepRel().pinned = false;
        // currWidth-=4;
        // relation.setLineWidth(currWidth);
        if ( (currWidth - 2)>0)
        	relation.setLineWidth(currWidth - 2);
        else
        	relation.setLineWidth(1);
        
        Label pin_out = new Label(ImageCache.getImage("push_pin.png"));
        pin_out.setBounds(new Rectangle(relation.getBounds().getCenter(), iconDim));
        menuButton.add(pin_out);
        
	}


	private void pin(MenuButton menuButton) {
		Shape relation = (Shape) getFigure();
		relation.setLineStyle(Graphics.LINE_DASHDOTDOT);
		Label pin_in = new Label(ImageCache.getImage("push_pin_in.png"));
		pin_in.setBounds(new Rectangle(relation.getBounds().getCenter(), iconDim));
        menuButton.add(pin_in);
        // currWidth+=4;
        // relation.setLineWidth(currWidth);
        getDepRel().pinned = true;
	} 
}
