package com.architexa.diagrams.strata.ui;

import com.architexa.diagrams.strata.parts.StrataRootEditPart;
import com.architexa.org.eclipse.draw2d.Figure;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.LayeredPane;
import com.architexa.org.eclipse.draw2d.ScalableLayeredPane;
import com.architexa.org.eclipse.draw2d.StackLayout;
import com.architexa.org.eclipse.draw2d.geometry.Dimension;
import com.architexa.org.eclipse.draw2d.geometry.Rectangle;
import com.architexa.org.eclipse.gef.EditPartViewer;
import com.architexa.org.eclipse.gef.LayerConstants;
import com.architexa.org.eclipse.gef.editparts.ScalableRootEditPart;

/**
 * This class makes sure that the preferred size for a diagram is the minimal bounds
 * of the root figure and all the comment children.
 */
public class FreeformStackLayout extends StackLayout{

	private ScalableRootEditPart scalableRootEP;

	public FreeformStackLayout(ScalableRootEditPart rootEditPart) {
		scalableRootEP = rootEditPart;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.architexa.org.eclipse.draw2d.StackLayout#calculatePreferredSize(com.architexa.org.eclipse.draw2d.IFigure, int, int)
	 * The preferred size should be the union of the ViewportWindow and the Diagram content
	 */
	@Override
	protected Dimension calculatePreferredSize(IFigure figure, int wHint,
			int hHint) {
		
		IFigure rootFig = ((StrataRootEditPart)scalableRootEP.getContents()).getRootFig();
		if (rootFig == null)
			super.calculatePreferredSize(figure, wHint, hHint);
		
		//Strata Diagram
		Rectangle bounds = rootFig.getBounds().getCopy();
		
		// Comments
		IFigure commentLayer = scalableRootEP.getLayer(StrataEditor.COMMENT_LAYER);
		
		for (Object comment: commentLayer.getChildren()) {
			bounds.union(((Figure)comment).getBounds());
		}

		EditPartViewer viewr = ((StrataRootEditPart)scalableRootEP.getContents()).getViewer();
		// null check needed since in strange cases the layered view can cause viewer to be null??
		if (viewr == null || viewr.getControl() == null) 
			return new Dimension(bounds.width, bounds.height);
		
		org.eclipse.swt.graphics.Rectangle vB = viewr.getControl().getBounds();
		bounds.union(vB.x, vB.y, vB.width, vB.height);
		
		//Handle zooming
		ScalableLayeredPane sLayer = (ScalableLayeredPane) ((LayeredPane)figure).getLayer(LayerConstants.SCALABLE_LAYERS);
		double zoom = sLayer.getScale();
		bounds.scale(zoom);
		
		return new Dimension(bounds.width, bounds.height);
	}
	
}
