/**
 * Copyright (c) 2007-2008 Architexa, Inc
 */ 
package com.architexa.diagrams.strata.parts;

import java.beans.PropertyChangeEvent;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.swt.graphics.Color;

import com.architexa.diagrams.draw2d.ColorUtilities;
import com.architexa.diagrams.draw2d.FigureUtilities;
import com.architexa.diagrams.draw2d.IFigureWithContents;
import com.architexa.diagrams.draw2d.SideGradientBorder;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.strata.StrataPlugin;
import com.architexa.diagrams.strata.commands.DeleteCommand;
import com.architexa.diagrams.strata.commands.UndoableAutoDelLayerCommand;
import com.architexa.diagrams.strata.figures.ConstrainedToolbarLayout;
import com.architexa.diagrams.strata.model.CompositeLayer;
import com.architexa.diagrams.strata.model.Layer;
import com.architexa.diagrams.strata.model.StrataRootDoc;
import com.architexa.diagrams.strata.model.policy.LayersDPolicy;
import com.architexa.org.eclipse.draw2d.FlowLayout;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.PositionConstants;
import com.architexa.org.eclipse.gef.Request;
import com.architexa.org.eclipse.gef.RequestConstants;
import com.architexa.org.eclipse.gef.commands.Command;
import com.architexa.org.eclipse.gef.commands.CompoundCommand;
import com.architexa.org.eclipse.gef.editparts.AbstractEditPart;
import com.architexa.org.eclipse.gef.requests.GroupRequest;



public class LayerEditPart extends ContainableEditPart {
	public static final Logger logger = StrataPlugin.getLogger(LayerEditPart.class);
	
	public LayerEditPart() {}
	
	public Layer getLayer() {
		return (Layer) this.getModel();
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);
		refreshVisuals();
	}
	@Override
	protected void refreshVisuals() {
		updateColors(this.getFigure(), null);
	}
	
	protected void updateColors(IFigure bodyFig, Color base) {
		int nestingLvl = this.getNestingLevel();
		if (nestingLvl<0) nestingLvl=0;

		if (base == null)
			base = this.getContainerBaseColor(nestingLvl);
		
		//base = ColorUtilities.darken(base);

	    //Color myBackBeg = base;
		//Color myBackEnd = base;
	    Color myBackBeg = ColorUtilities.darken(base);
		Color myBackEnd = base;
		//Color myBackBeg = ColorConstants.green;
		//Color myBackEnd = ColorConstants.red;
		
	    if (com.architexa.diagrams.ColorScheme.SchemeUML) {
	    	base = new Color(null, 230, 230, 230);
	    	myBackBeg = base;
	    	myBackEnd = base;
	    }

	    bodyFig.setOpaque(true);
	    bodyFig.setBackgroundColor(myBackBeg);

	    // gradient updating
		boolean insideMultiLayer = isInsideLayer();

		int gradientWidth = 20;
		if (nestingLvl == 0) gradientWidth = 60;

		SideGradientBorder inBorder = new SideGradientBorder(PositionConstants.RIGHT, gradientWidth, myBackBeg, myBackEnd);
		FigureUtilities.removeBorder(bodyFig, SideGradientBorder.class);

		if (!insideMultiLayer) {
			FigureUtilities.insertBorder(bodyFig, inBorder);
		}
	}
	
	private boolean isInsideLayer() {
		// we were before only for multilayers, but any parent layer should be
		// responsible for the gradients
		// we are only checking for parents being CompositeLayers
		if (this.getParent() == null) 
			return false;
		if (this.getParent().getModel() instanceof Layer 
				/*&& ((CompositeLayer) this.getParent().getModel()).getMultiLayer()*/
				)
			return true;
		return false;
	}
	@Override
	public String toString() {
		return super.toString() + " insideLayer: " + isInsideLayer();
	}
	
	@Override
    public Command getCommand(Request request) {
		if (request.getType().equals(RequestConstants.REQ_DELETE)) {
		    if (request instanceof GroupRequest && ((GroupRequest) request).getEditParts().contains(getParent())) return null;
		    
		    AbstractEditPart firstSelection = (AbstractEditPart) ((GroupRequest) request).getEditParts().get(0);
		    if (firstSelection.getModel() instanceof Layer) {
		    	Layer layer =  (Layer) firstSelection.getModel();
		    	ArtifactFragment parentAF = layer.getParentArtOfLayer();
		    	if (parentAF == null) return null;
		    	List<Layer> orgLayers = LayersDPolicy.getLayersCopy(parentAF, getRepo());
		    	
		    	CompoundCommand cc = new CompoundCommand("Delete Layer");
		    	addAllNestedLayerDelCommands(cc, layer);
		    	
		    	// clear org layer so undo adds Afs to an empty layer instead of adding AFs twice
		    	for (Layer layerChild : orgLayers) {
		    		if (layer.toString().equals(layerChild.toString()))
		    			layerChild.clearShownChildren();
		    	}

		    	cc.add(new UndoableAutoDelLayerCommand(getRootController(), orgLayers, parentAF));
		    	return cc;
		    }
		}
		if(RequestConstants.REQ_CONNECTION_START.equals(request.getType()) ||
				RequestConstants.REQ_CONNECTION_END.equals(request.getType())) {
			// TODO: Don't allow layers to be the source or target of General
			// Connections until figure out why connections from a package to
			// a layer has a null source, causing it to point from the corner
			return null;
		}
		return super.getCommand(request);
	}
	
	private void addAllNestedLayerDelCommands(CompoundCommand cc, Layer layer) {
		for (ArtifactFragment af : layer.getChildren()) {
			if (af instanceof Layer)
				addAllNestedLayerDelCommands(cc, (Layer) af);
			else
				cc.add(new DeleteCommand("Delete", af, getRootModel(), af.getParentArt()));
    	}
	}

	@Override
	public void colorFigure(Color color) {
		if (color == null) color = currBase;

		updateColors(this.getFigure(), color);
	}

	@Override
	protected IFigure createContainerFigure() {
	    boolean multiLayer = false;
	    Layer layer = this.getLayer();
		if ((layer  instanceof CompositeLayer) && ((CompositeLayer) layer).getMultiLayer())  multiLayer = true;
		
		boolean insideMultiLayer = isInsideLayer();
			
		//Figure contentFig = new Figure();
	    //Figure bodyFig = new GradientFigure(contentFig, false, myBackBeg, myBackEnd);
	    IFigureWithContents.Impl bodyFig = new IFigureWithContents.Impl();
		FigureUtilities.addPadding(bodyFig, 5, 5);
		updateColors(bodyFig, null);
		
		boolean isHorz = !multiLayer;
		if (!((Layer)getModel()).getLayout())
			isHorz = false;
		
		if (StrataRootDoc.stretchFigures) {
			ConstrainedToolbarLayout contentLM = new ConstrainedToolbarLayout(isHorz);
			contentLM.setStretchMinorAxis(true);
			contentLM.setStretchMajorAxis(true);
			if (multiLayer)
				contentLM.setSpacing(1);
			else
				contentLM.setSpacing(15);
			if (insideMultiLayer)
				contentLM.setMajorAlignment(FlowLayout.ALIGN_CENTER);
			contentLM.setMinorAlignment(FlowLayout.ALIGN_CENTER);
			//contentLM.setStretchMinorAxis(true);
			bodyFig.contentFig.setLayoutManager(contentLM);
			
		} else {
			FlowLayout contentLM = new FlowLayout(isHorz);
			contentLM.setStretchMinorAxis(false);
			if (multiLayer)
				contentLM.setMinorSpacing(1);
			else
				contentLM.setMinorSpacing(15);
			if (insideMultiLayer)
				contentLM.setMajorAlignment(FlowLayout.ALIGN_CENTER);
			contentLM.setMinorAlignment(FlowLayout.ALIGN_CENTER);
			//contentLM.setStretchMinorAxis(true);
			bodyFig.contentFig.setLayoutManager(contentLM);
			
		}
		//bodyFig.setLayoutManager(contentLM);
		//contentFig.setBorder(new LineBorder(myBack, 8));
		
		return bodyFig;
	}
	
	@Override
	public void performRequest(Request req) {
		if (req.getType() == RequestConstants.REQ_OPEN) {
			// @tag cool-ui-opportunity: we used to change layout of MC here to radial 
			logger.info("Opening");
		}
	}

	@Override
	public int getNestingLevel() {
		// we subtract one, since the base implementation adds 1, and we don't
		// really count
		return super.getNestingLevel()-1;
	}
	
	@Override
	public void removeEmptyLayers() {
		Layer layer = (Layer) getModel();
		if (layer.getShownChildrenCnt() == 0) {
			ArtifactFragment parentAF = (ArtifactFragment) getParent().getModel();
			if (parentAF != null)
				parentAF.removeShownChild(layer);
		}
		super.removeEmptyLayers();
	}
}
