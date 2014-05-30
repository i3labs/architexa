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
 */
package com.architexa.diagrams.relo.figures;

import java.util.List;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

import com.architexa.diagrams.draw2d.IFigureWithContents;
import com.architexa.diagrams.draw2d.NonEmptyFigure;
import com.architexa.org.eclipse.draw2d.AbstractBackground;
import com.architexa.org.eclipse.draw2d.Border;
import com.architexa.org.eclipse.draw2d.CompoundBorder;
import com.architexa.org.eclipse.draw2d.Figure;
import com.architexa.org.eclipse.draw2d.Graphics;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.Label;
import com.architexa.org.eclipse.draw2d.LineBorder;
import com.architexa.org.eclipse.draw2d.RoundedRectangle;
import com.architexa.org.eclipse.draw2d.ToolbarLayout;
import com.architexa.org.eclipse.draw2d.geometry.Dimension;
import com.architexa.org.eclipse.draw2d.geometry.Rectangle;
import com.architexa.org.eclipse.gef.handles.HandleBounds;

/**
 * @author vineet
 *
 */
public class CodeUnitFigure extends RoundedRectangle implements IFigureWithContents, HandleBounds {

	protected final IFigure hdrFigure;
	protected final IFigure contentFigure;

	protected Label nameLbl = null;
	
	public NonEmptyFigure nonEmptyFigure;
	private boolean isRounded = false;

	/* 
	 * allow overriding of functionality, however, subclass needs to initialize
	 * nameLbl and contentFigure
	 */
	public CodeUnitFigure(IFigure hdrFig, Figure contentFig, boolean showNonEmptyFigures) {
		this.setLayoutManager(new ToolbarLayout());
		
		this.hdrFigure = hdrFig;
		this.add(hdrFigure);
		this.contentFigure = contentFig;
		this.add(contentFigure);
		if (showNonEmptyFigures) {
			this.nonEmptyFigure = new NonEmptyFigure(new Label("..."));
			this.add(nonEmptyFigure);
		}
	}
	
	
	public CodeUnitFigure(Color color, Color borderColor, IFigure hdrFig, Figure contentFig, boolean showNonEmptyFigures) {
		this(hdrFig, contentFig, showNonEmptyFigures);
		
		if (borderColor != null) {
			setBorder(new LineBorder(borderColor, 1));
		}
		if (color != null) {
			setBackgroundColor(color);
			setOpaque(true);
		}
	}
	
	// used to set rounded rectangle figures
	public CodeUnitFigure(IFigure hdrFig, Color color, Color borderColor, Dimension corner) {
		this(color, borderColor, hdrFig, new Figure(), false);
		if (corner !=null) {
			isRounded = true;
			setCornerDimensions(corner);
		}
		this.nameLbl = findLbl(hdrFig); // lets guess and find nameLbl ... 
		this.contentFigure.setLayoutManager(new ToolbarLayout());
	}
	public CodeUnitFigure(IFigure hdrFig, Color color, Color borderColor) {
		this(color, borderColor, hdrFig, new Figure(), false);
		this.nameLbl = findLbl(hdrFig); // lets guess and find nameLbl ... 
		this.contentFigure.setLayoutManager(new ToolbarLayout());
	}

	public CodeUnitFigure(IFigure hdrFig, Color color, Color borderColor, boolean showNonEmptyFigures) {
		this(color, borderColor, hdrFig, new Figure(), showNonEmptyFigures);
		this.nameLbl = findLbl(hdrFig); // lets guess and find nameLbl ... 
		this.contentFigure.setLayoutManager(new ToolbarLayout());
	}

	private Label findLbl(IFigure hdrFig) {
		if (hdrFig instanceof Label) return (Label) hdrFig;

		List<?> hdrChildren = hdrFig.getChildren();
		for (Object child : hdrChildren) {
			Label lbl = findLbl((IFigure)child);
			if (lbl != null) return lbl;
		}
		
		return null;
	}
	

	public CodeUnitFigure(String labelName, Image labelImg, Color color, Color borderColor, boolean showNonEmptyFigures) {
		this(new Label(labelName, labelImg), color, borderColor, showNonEmptyFigures);
	}

	
	public Label getLabel() {
		return nameLbl;
	}

	@Override
    public String toString() {
		return "[CUF]" + nameLbl.getText();
	}
	
	public IFigure getHeaderFig() {
		return this.hdrFigure;
	}

	public IFigure getContentPane() {
		return this.contentFigure;
	}

	public IFigure getContentFig() {
		return this.contentFigure;
	}
	
	// @tag post-rearch: move to a base class
	@Override
	public void paintFigure(Graphics graphics) {
		super.paintFigure(graphics);
		paintBackroundBorder(graphics);
	}

	private void paintBackroundBorder(Graphics graphics) {
		// below commented out since the base class does this
		//if (border instanceof AbstractBackground)
		//	((AbstractBackground) border).paintBackground(this, graphics, NO_INSETS);
		Border border = getBorder();
		if (border instanceof CompoundBorder) {
			paintBackroundBorder(graphics, border);
		}
	}
	private void paintBackroundBorder(Graphics graphics, Border border) {
		if (border instanceof AbstractBackground)
			((AbstractBackground) border).paintBackground(this, graphics, NO_INSETS);
		if (border instanceof CompoundBorder) {
			CompoundBorder cb = (CompoundBorder)border;
			paintBackroundBorder(graphics, cb.getOuterBorder());
			paintBackroundBorder(graphics, cb.getInnerBorder());
		}
	}

	public Rectangle getHandleBounds() {
		// VS: would getClientArea do the same thing?
		// ignore insets so that we can support shadows
		return this.getBounds().getCropped(this.getInsets());
	}
	
	// ROUNDED RECTANGLE METHODS
	@Override
	protected void fillShape(Graphics graphics) {
		if (isRounded )
			graphics.fillRoundRectangle(getBounds(), corner.width, corner.height);
	}
	@Override
	protected void outlineShape(Graphics graphics) {
	}
	
}