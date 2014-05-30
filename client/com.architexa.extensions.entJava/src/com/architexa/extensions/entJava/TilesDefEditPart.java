package com.architexa.extensions.entJava;

import org.eclipse.jface.resource.ImageDescriptor;
import org.openrdf.model.Resource;

import com.architexa.diagrams.draw2d.ColorUtilities;
import com.architexa.diagrams.draw2d.FigureUtilities;
import com.architexa.diagrams.model.Artifact;
import com.architexa.diagrams.relo.figures.CodeUnitFigure;
import com.architexa.diagrams.relo.jdt.parts.CodeUnitEditPart;
import com.architexa.diagrams.relo.jdt.services.PluggableEditPartSupport;
import com.architexa.diagrams.relo.ui.ColorScheme;
import com.architexa.diagrams.services.PluggableTypes.ImageDescriptorProvider;
import com.architexa.diagrams.ui.ImageCache;
import com.architexa.org.eclipse.draw2d.Figure;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.Label;
import com.architexa.org.eclipse.draw2d.RoundedBorder;
import com.architexa.org.eclipse.draw2d.ToolbarLayout;
import com.architexa.org.eclipse.draw2d.geometry.Dimension;
import com.architexa.store.ReloRdfRepository;

public class TilesDefEditPart extends CodeUnitEditPart implements ImageDescriptorProvider {
	final static private int PADDING = 5;
	final static private Dimension CORNER_DIM = new Dimension(20,20);

    @Override
    public ImageDescriptor getIconDescriptor(Artifact art, Resource resType) {
    	return PluggableEditPartSupport.getIconDescriptor(getRepo(), art, resType);
    }
    
	public ImageDescriptor getImageDescriptor(Artifact art, Resource typeRes, ReloRdfRepository repo) {
		return Activator.getImageDescriptor("/icons/tiles_t.png");
	}

    @Override
    protected IFigure createFigure(IFigure curFig, int newDL) {
    	Label nameLbl = new Label(getLabel(), ImageCache.calcImageFromDescriptor(this.getIconDescriptor(getCU().getArt(), getCU().queryType(getRepo()))));
	    Figure lblFig = new Figure();
	    lblFig.setLayoutManager(new ToolbarLayout(true));
	    lblFig.add(nameLbl);
	    lblFig.add(new Label("  "));
	    FigureUtilities.addPadding(lblFig, PADDING, PADDING);
	  
	    curFig = new CodeUnitFigure(lblFig, null, null, CORNER_DIM);
    	curFig.setBackgroundColor(ColorUtilities.darken(ColorScheme.packageColor));
	    
    	RoundedBorder roundBorder = new RoundedBorder(ColorScheme.classBorder, 1);
		roundBorder.setCornerDimensions(CORNER_DIM);
		curFig.setBorder(roundBorder);
    	return curFig;
    }
}
