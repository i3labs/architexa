/**
 * Copyright (c) 2007-2008 Architexa, Inc
 */ 
package com.architexa.diagrams.strata.parts;

import org.apache.log4j.Logger;
import org.eclipse.swt.graphics.Color;

import com.architexa.diagrams.draw2d.FigureUtilities;
import com.architexa.diagrams.draw2d.IFigureWithContents;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.strata.StrataPlugin;
import com.architexa.diagrams.strata.figures.ConstrainedToolbarLayout;
import com.architexa.diagrams.strata.model.CompositeLayer;
import com.architexa.diagrams.strata.model.Layer;
import com.architexa.diagrams.strata.model.StrataRootDoc;
import com.architexa.org.eclipse.draw2d.FlowLayout;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.gef.Request;
import com.architexa.org.eclipse.gef.RequestConstants;



public class CompositeLayerEditPart extends LayerEditPart {
	public static final Logger logger = StrataPlugin.getLogger(CompositeLayerEditPart.class);
	
	public CompositeLayerEditPart() {}
	
	Color currBase;

	@Override
	protected IFigure createContainerFigure() {
	    boolean multiLayer = false;
	    if ((getLayer() instanceof CompositeLayer) && ((CompositeLayer) getLayer()).getMultiLayer())  
			multiLayer = true;
			
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
			if (getModel() instanceof CompositeLayer && ((CompositeLayer) getModel()).getMultiLayer())
				contentLM.setMajorAlignment(FlowLayout.ALIGN_CENTER);
			contentLM.setMinorAlignment(FlowLayout.ALIGN_CENTER);
			bodyFig.contentFig.setLayoutManager(contentLM);

		} else {
			FlowLayout contentLM = new FlowLayout(isHorz);
			contentLM.setStretchMinorAxis(true);
			if (getModel() instanceof CompositeLayer && ((CompositeLayer) getModel()).getMultiLayer())
				contentLM.setMajorAlignment(FlowLayout.ALIGN_CENTER);
			contentLM.setMinorAlignment(FlowLayout.ALIGN_CENTER);
			bodyFig.contentFig.setLayoutManager(contentLM);

		}
				
		return bodyFig;
	}
	
	@Override
	protected void refreshVisuals() {
		updateColors(this.getFigure(), null);
	}
	
	@Override
	public void performRequest(Request req) {
		if (req.getType() == RequestConstants.REQ_OPEN) {
			// @tag cool-ui-opportunity: we used to change layout of MC here to radial 
			logger.info("Opening");
		}
	}

	@Override
	public void removeSingleCompositeLayers() {
		CompositeLayer layer = (CompositeLayer) getModel();
		if (layer.getShownChildrenCnt() == 1) {
			ArtifactFragment parentAF = (ArtifactFragment) getParent().getModel();
			if (parentAF != null && parentAF instanceof CompositeLayer) {
				int ndx = parentAF.getShownChildren().indexOf(layer);
				if (ndx == -1) return;
				parentAF.removeShownChild(layer);
				parentAF.getShownChildren().addAll(ndx, layer.getChildren());
			}
		}
		super.removeSingleCompositeLayers();
	}
}
