/**
 * Copyright (c) 2007-2008 Architexa, Inc
 */ 
package com.architexa.diagrams.strata.figures;

import org.apache.log4j.Logger;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

import com.architexa.diagrams.draw2d.ColorUtilities;
import com.architexa.diagrams.draw2d.FigureUtilities;
import com.architexa.diagrams.draw2d.IFigureWithContents;
import com.architexa.diagrams.draw2d.RoundedShadowBorder;
import com.architexa.diagrams.strata.StrataPlugin;
import com.architexa.diagrams.strata.model.StrataRootDoc;
import com.architexa.diagrams.strata.ui.ColorScheme;
import com.architexa.diagrams.ui.FontCache;
import com.architexa.org.eclipse.draw2d.AbstractBorder;
import com.architexa.org.eclipse.draw2d.AbstractLayout;
import com.architexa.org.eclipse.draw2d.ColorConstants;
import com.architexa.org.eclipse.draw2d.Figure;
import com.architexa.org.eclipse.draw2d.FlowLayout;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.Label;
import com.architexa.org.eclipse.draw2d.LineBorder;
import com.architexa.org.eclipse.draw2d.RoundedBorder;
import com.architexa.org.eclipse.draw2d.ToolbarLayout;


public class ContainerFigure extends Figure implements IFigureWithContents {
    static final Logger logger = StrataPlugin.getLogger(ContainerFigure.class);

    public static final double szMultiplier = 9* (4.0/3.0);

    Color myBack = null;
    public void setMyBack(Color myBack) {
    	this.myBack = myBack;
    }
    Color currColor = null;
    
	private final IFigure contentFig; // content
	public Label icnLbl; // icon label, old label without using links
	private Figure nameFig = new Figure(); // figure containing link labels 
	private AbstractBorder abstractBorder;

	public ContainerFigure(Image icn, String name, String toolTip, int lvl, Color highlightColor) {
		Label toolTipLbl = new Label(" " + toolTip + " ");
		
		Figure bodyFig = this;

		bodyFig.setOpaque(false);
		
		if (StrataRootDoc.stretchFigures) {
			ConstrainedToolbarLayout bodyLayout = new ConstrainedToolbarLayout();
			bodyLayout.setMinorAlignment(ToolbarLayout.ALIGN_CENTER);
			bodyLayout.setMajorAlignment(ToolbarLayout.ALIGN_CENTER);
			bodyFig.setLayoutManager(bodyLayout);
		} else {
			bodyFig.setLayoutManager(new ToolbarLayout());
		}

		myBack = ColorScheme.containerColors[lvl%10];

		bodyFig.setBackgroundColor(myBack);
		
		// Figure structure
		// bodyFig( lblContainerFig( icnLbl(icon), nameFig(linkLabels) )
		//		, contentFig)
		
		// create label figures
		if (icn == null)
			icnLbl = new Label("  ");
		else;
			icnLbl = new Label("", icn);
		nameFig.setOpaque(true);
		ToolbarLayout tbLayout = new ToolbarLayout(true);
		tbLayout.setMinorAlignment(ToolbarLayout.ALIGN_CENTER);
		nameFig.setLayoutManager(tbLayout);

		AbstractLayout abstractLayout = null;
		if (StrataRootDoc.stretchFigures) {
			  abstractLayout = new ConstrainedToolbarLayout(true);
			((ConstrainedToolbarLayout) abstractLayout).setMajorAlignment(FlowLayout.ALIGN_CENTER);
			((ConstrainedToolbarLayout) abstractLayout).setMinorAlignment(FlowLayout.ALIGN_CENTER);

		} else {
			  abstractLayout = new FlowLayout(true);
			((FlowLayout) abstractLayout).setMajorAlignment(FlowLayout.ALIGN_CENTER);
			((FlowLayout) abstractLayout).setMinorAlignment(FlowLayout.ALIGN_CENTER);
		}
		// create container for label figures and add them
		
		// we need to add the icnLabel and nameFigure to a container Figure so
		// that they layout correctly
		Figure lblContainerFig = new Figure();
		lblContainerFig.setOpaque(true);
		lblContainerFig.setLayoutManager(abstractLayout );
		lblContainerFig.add(icnLbl);
		lblContainerFig.add(nameFig);
		bodyFig.add(lblContainerFig);


		if (toolTipLbl != null) bodyFig.setToolTip(toolTipLbl);
		
		// set the contents
		this.contentFig = new Figure();
		
		// content layout manager
//		contentFig.setBackgroundColor(myBack);
		FlowLayout layout = new FlowLayout( /*isHorizontal*/false);
		layout.setStretchMinorAxis(true);
		//layout.setMinorSpacing(15);
		layout.setMajorSpacing(20);
		this.contentFig.setLayoutManager(layout);

		// content details and add it to the parent figure
		this.contentFig.setOpaque(true);
		FigureUtilities.addPadding(this.contentFig, 1, 10, 1, 10);

		//if (!StrataRootDoc.stretchFigures) {
		bodyFig.add(contentFig );
		//}	
		
		if (com.architexa.diagrams.ColorScheme.SchemeV1) {
			abstractBorder = new RoundedBorder(myBack, 4);
			FigureUtilities.insertBorder(this, abstractBorder);
			FigureUtilities.insertBorder(this, new RoundedShadowBorder(ColorUtilities.darken(myBack, 0.5), 2));
		} else {
			abstractBorder = new LineBorder(ColorConstants.black, 1);
			FigureUtilities.insertBorder(this, abstractBorder);
		}
		
		// Color saved resources
		if (highlightColor!=null)
			colorFigure(highlightColor);
	}
	double sizeRatio = 1;
	
	/**
	 * @param font 
	 * @param sizeRatio - a number
	 */
	public Font setSizeRatio(double inSizeRatio) {
		this.sizeRatio = inSizeRatio;
		Font newFont = FontCache.getFontForRatio(sizeRatio);
		nameFig.setFont(newFont);
		return newFont;
	}
	
	public Color getMyBackgroundColor(){
		return myBack;
	}
	/**
	 * @return the content portion (where children are added)
	 */
	public IFigure getContentFig() {
		return contentFig;
	}
	
	public AbstractBorder getAbstractBorder() {
		return abstractBorder;
	}
	public void setAbstractBorder(AbstractBorder ab ) {
		abstractBorder = ab;
	}
    public void highlight() {
        int_highlight(1);
    }
    public void highlight(boolean highlight) {
        if (highlight)
            int_highlight(1);
        else
            int_highlight(0);
    }
    // 0=no hightlight, 1=hightlight, 2=higlight, hightlight, -1=-highlight
    private void int_highlight(int highlight) {
        Color highlightColor = getHightlightColor(highlight);

        // set border color first - since the repaint from setting the figures
		// color will update the border as well
        if (abstractBorder instanceof RoundedBorder)
        	((RoundedBorder) this.abstractBorder).setColor(highlightColor);
        
        this.setBackgroundColor(highlightColor);
    }
    
    private Color getHightlightColor(int highlight) {
    	Color color = ColorConstants.lightGray;
        if (highlight > 0)
        	return ColorUtilities.darken(color, Math.pow(0.7, highlight-1));
        else if (highlight == 0) {
        	if (currColor == null)
        		return myBack;
        	return currColor;
        }
        else
        	return ColorUtilities.darken(color, Math.pow(1.0/0.7, -highlight));
	}

    public void colorFigure(Color highlightColor) {
	    // set border color first - since the repaint from setting the figures
		// color will update the border as well
    	if (highlightColor == null)
    		highlightColor = myBack;
    	currColor = highlightColor;
    	 if (abstractBorder instanceof RoundedBorder)
         	((RoundedBorder) this.abstractBorder).setColor(highlightColor);
         if (abstractBorder instanceof LineBorder)
         	((LineBorder) this.abstractBorder).setColor(highlightColor);
         
	    this.setBackgroundColor(highlightColor);
	}
    
	public void highlight(int highlight) {
        if (highlight < 0)
            int_highlight(highlight);
        else
            int_highlight(highlight + 1);
    }

	public void setNameFig(Label nameFig) {
		this.nameFig = nameFig;
	}

	public Figure getNameFig() {
		return nameFig;
	}
    
}
