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
 * Created on Jun 14, 2004
 */
package com.architexa.diagrams.relo.figures;

import org.eclipse.swt.graphics.Color;

import com.architexa.org.eclipse.draw2d.AbstractBorder;
import com.architexa.org.eclipse.draw2d.ColorConstants;
import com.architexa.org.eclipse.draw2d.Graphics;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.PositionConstants;
import com.architexa.org.eclipse.draw2d.geometry.Insets;
import com.architexa.org.eclipse.draw2d.geometry.Rectangle;

public class IncompleteSideLineBorder extends AbstractBorder {
	private boolean north = false;
	private boolean west = false;
	private boolean south = false;
	private boolean east = false;
	
	public int width = 1;
	public Color lineColor = ColorConstants.black;
	private final IFigure incompleteSide;
	private final int incompleteCorner;

	public IncompleteSideLineBorder(Color color, int width, int side, IFigure incompleteSide, int incompleteCorner) {
		this.lineColor = color;
		this.width = width;
		this.incompleteSide = incompleteSide;
		this.incompleteCorner = incompleteCorner;

		switch (side) {
		case PositionConstants.NORTH:
			north = true;
			break;
		case PositionConstants.WEST:
			west = true;
			break;
		case PositionConstants.SOUTH:
			south = true;
			break;
		case PositionConstants.EAST:
			east = true;
			break;
		}
	}

	public Insets getInsets(IFigure figure) {
		return new Insets(
			north ? width : 0,
			west ? width : 0,
			south ? width : 0,
			east ? width : 0);
	}
	public void paint(IFigure figure, Graphics graphics, Insets insets) {
		tempRect.setBounds(getPaintRectangle(figure, insets));
		if (width % 2 == 1) {
			tempRect.width--;
			tempRect.height--;
		}
		tempRect.shrink(width / 2, width / 2);
		graphics.setLineWidth(width);
		if (lineColor != null) {
			graphics.setForegroundColor(lineColor);
		}
		
		Rectangle incompleteBounds = null;
		if (incompleteSide != null)
			incompleteBounds = incompleteSide.getBounds();
		else
			incompleteBounds = new Rectangle();
		
		if (north) {
			if (incompleteCorner == PositionConstants.NORTH_WEST)
				graphics.drawLine(tempRect.getTopLeft().translate(incompleteBounds.width, 0), tempRect.getTopRight());
			else if (incompleteCorner == PositionConstants.NORTH_EAST)
				graphics.drawLine(tempRect.getTopLeft(), tempRect.getTopRight().translate(-incompleteBounds.width, 0));
			else
				graphics.drawLine(tempRect.getTopLeft(), tempRect.getTopRight());
		}
		if (south) {
			if (incompleteCorner == PositionConstants.SOUTH_EAST)
				graphics.drawLine(tempRect.getBottomLeft().translate(incompleteBounds.width, 0), tempRect.getBottomRight());
			else if (incompleteCorner == PositionConstants.SOUTH_WEST)
				graphics.drawLine(tempRect.getBottomLeft(), tempRect.getBottomRight().translate(-incompleteBounds.width, 0));
			else
				graphics.drawLine(tempRect.getBottomLeft(), tempRect.getBottomRight());
		}
		if (west) {
			if (incompleteCorner == PositionConstants.NORTH_WEST)
				graphics.drawLine(tempRect.getTopLeft().translate(0, incompleteBounds.height), tempRect.getBottomLeft());
			else if (incompleteCorner == PositionConstants.SOUTH_WEST)
				graphics.drawLine(tempRect.getTopLeft(), tempRect.getBottomLeft().translate(0, -incompleteBounds.height));
			else
				graphics.drawLine(tempRect.getTopLeft(), tempRect.getBottomLeft());
		}
		if (east) {
			graphics.drawLine(tempRect.getTopRight(), tempRect.getBottomRight());
		}
	}
}